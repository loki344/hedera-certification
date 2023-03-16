package ch.inacta;

import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HbarUnit;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.ScheduleCreateTransaction;
import com.hedera.hashgraph.sdk.ScheduleDeleteTransaction;
import com.hedera.hashgraph.sdk.ScheduleId;
import com.hedera.hashgraph.sdk.TransferTransaction;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static ch.inacta.Config.ACCOUNT1;
import static ch.inacta.Config.ACCOUNT2;
import static ch.inacta.Config.MY_ACCOUNT;

public class Slide4 {

    public static void main(String[] args) throws TimeoutException, ReceiptStatusException, PrecheckStatusException {

        try (final var client = ClientFactory.getClient()) {

            System.out.printf("Balance of account %s before script: %s hbar%n", ACCOUNT1.getId(),
                    getBalanceOfAccount(ACCOUNT1.getId(), client).toString(HbarUnit.HBAR));
            System.out.printf("Balance of account %s before script: %s hbar%n", ACCOUNT2.getId(),
                    getBalanceOfAccount(ACCOUNT2.getId(), client).toString(HbarUnit.HBAR));

            var scheduleId = createScheduledTransaction(client);

            deleteScheduledTransaction(client, scheduleId);

            System.out.printf("Balance of account %s after script: %s hbar%n", ACCOUNT1.getId(),
                    getBalanceOfAccount(ACCOUNT1.getId(), client).toString(HbarUnit.HBAR));
            System.out.printf("Balance of account %s after script: %s hbar%n", ACCOUNT2.getId(),
                    getBalanceOfAccount(ACCOUNT2.getId(), client).toString(HbarUnit.HBAR));

        }
    }

    private static void deleteScheduledTransaction(Client client, ScheduleId scheduleId)
            throws ReceiptStatusException, TimeoutException, PrecheckStatusException {

        var receipt = new ScheduleDeleteTransaction().setScheduleId(scheduleId).freezeWith(client).sign(ACCOUNT1.getPrivateKey()).execute(client)
                .getReceipt(client).validateStatus(true);
        System.out.printf("Scheduled transaction deleted. Transaction id: %s%n", receipt.transactionId);
    }

    private static Hbar getBalanceOfAccount(AccountId id, Client client) throws TimeoutException, PrecheckStatusException {

        return new AccountBalanceQuery().setAccountId(id).execute(client).hbars;
    }

    private static ScheduleId createScheduledTransaction(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {

        final var transaction = new TransferTransaction().addHbarTransfer(ACCOUNT1.getId(), Hbar.fromTinybars(11).negated())
                .addHbarTransfer(ACCOUNT2.getId(), Hbar.fromTinybars(11));

        final var scheduledTransaction = new ScheduleCreateTransaction().setScheduledTransaction(transaction)
                .setAdminKey(MY_ACCOUNT.getPublicKey()).execute(client);

        var receipt = scheduledTransaction.getReceipt(client).validateStatus(true);
        System.out.printf("Scheduled transaction with id: %s created%n", receipt.scheduleId);
        return Objects.requireNonNull(receipt.scheduleId);
    }

}
