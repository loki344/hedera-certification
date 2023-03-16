package ch.inacta;

import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HbarUnit;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.ScheduleCreateTransaction;
import com.hedera.hashgraph.sdk.ScheduleId;
import com.hedera.hashgraph.sdk.ScheduleSignTransaction;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static ch.inacta.Config.ACCOUNT1;
import static ch.inacta.Config.ACCOUNT2;

public class TaskScheduledTransaction {

    public static void main(String[] args) throws TimeoutException, ReceiptStatusException, PrecheckStatusException {

        try (final var client = ClientFactory.getClient()) {

            System.out.printf("Balance of account %s before script: %s hbar%n", ACCOUNT1.getId(),
                    getBalanceOfAccount(ACCOUNT1.getId(), client).toString(HbarUnit.HBAR));
            System.out.printf("Balance of account %s before script: %s hbar%n", ACCOUNT2.getId(),
                    getBalanceOfAccount(ACCOUNT2.getId(), client).toString(HbarUnit.HBAR));

            // Create scheduled transaction, save it to file
            var scheduleId = createScheduledTransaction(client);
            saveBase64EncodedIdToFile(scheduleId);

            // Read the file and sign the transaction
            var deserializedScheduleId = readBase64EncodedIdFromFile(scheduleId);
            signTransaction(client, deserializedScheduleId, ACCOUNT1);

            //TODO fetch the transaction and show that it's executed

            // Check if the balance is affected
            System.out.printf("Balance of account %s after script: %s hbar%n", ACCOUNT1.getId(),
                    getBalanceOfAccount(ACCOUNT1.getId(), client).toString(HbarUnit.HBAR));
            System.out.printf("Balance of account %s after script: %s hbar%n", ACCOUNT2.getId(),
                    getBalanceOfAccount(ACCOUNT2.getId(), client).toString(HbarUnit.HBAR));

        }
    }

    private static void signTransaction(Client client, ScheduleId scheduleId, Account signer) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        var receipt = new ScheduleSignTransaction().setScheduleId(scheduleId).freezeWith(client).sign(signer.getPrivateKey()).execute(client).getReceipt(client).validateStatus(true);
        System.out.printf("Signed scheduleId: %s with account id: %s, Status: %s%n", scheduleId, signer.getId(), receipt.status);
    }

    private static ScheduleId readBase64EncodedIdFromFile(ScheduleId scheduleId) {

        String encodedContent = "";
        try {
            encodedContent = Files.readString(Path.of(getFileName(scheduleId)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        var decodedContent = new String(Base64.getDecoder().decode(encodedContent));
        return ScheduleId.fromString(decodedContent);
    }

    private static void saveBase64EncodedIdToFile(ScheduleId scheduleId) {

        var fileName = getFileName(scheduleId);
        try (var writer = new BufferedWriter(new FileWriter(fileName))) {
            var encodedContent = Base64.getEncoder().encodeToString(scheduleId.toString().getBytes(StandardCharsets.UTF_8));
            writer.write(encodedContent);
        } catch (IOException e) {
            System.out.printf("Writing File failed!");
            e.printStackTrace();
        }
        System.out.printf("Created %s%n", fileName);
    }

    @NotNull
    private static String getFileName(ScheduleId scheduleId) {
        return "schedule_id_" + scheduleId + ".txt";
    }

    private static Hbar getBalanceOfAccount(AccountId id, Client client) throws TimeoutException, PrecheckStatusException {

        return new AccountBalanceQuery().setAccountId(id).execute(client).hbars;
    }

    private static ScheduleId createScheduledTransaction(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {

        final var transaction = new TransferTransaction().addHbarTransfer(ACCOUNT1.getId(), Hbar.from(10).negated())
                .addHbarTransfer(ACCOUNT2.getId(), Hbar.from(10));

        final var scheduledTransaction = new ScheduleCreateTransaction().setScheduledTransaction(transaction).execute(client);

        var receipt = scheduledTransaction.getReceipt(client).validateStatus(true);
        System.out.printf("Scheduled transaction with id: %s created%n", receipt.scheduleId);
        return Objects.requireNonNull(receipt.scheduleId);
    }

}
