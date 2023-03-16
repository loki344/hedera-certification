package ch.inacta;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.ScheduleId;
import com.hedera.hashgraph.sdk.ScheduleInfoQuery;
import com.hedera.hashgraph.sdk.ScheduleSignTransaction;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionRecordQuery;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static ch.inacta.Config.ACCOUNT1;
import static ch.inacta.Config.ACCOUNT2;
import static ch.inacta.Config.ACCOUNT3;

public class Slide5 {

    public static void main(String[] args) throws TimeoutException, ReceiptStatusException, PrecheckStatusException {

        try (final var client = ClientFactory.getClient()) {

            var multiSigAccountId = createMultiSigAccount(client);

            var scheduleId = createScheduledTransaction(client, multiSigAccountId);

            signSchedule(client, ACCOUNT1, scheduleId);
            logScheduleInfo(client, scheduleId);

            signSchedule(client, ACCOUNT2, scheduleId);
            logScheduleInfo(client, scheduleId);

            logTransactionInfo(client, scheduleId);
        }
    }

    private static void logTransactionInfo(Client client, ScheduleId scheduleId) throws TimeoutException, PrecheckStatusException {
        var scheduleInfo = new ScheduleInfoQuery().setScheduleId(scheduleId).execute(client);

        var transactionInfo = new TransactionRecordQuery().setTransactionId(scheduleInfo.scheduledTransactionId).execute(client);
        System.out.printf("Transaction info: %s%n", transactionInfo);
    }

    private static void logScheduleInfo(Client client, ScheduleId scheduleId) throws TimeoutException, PrecheckStatusException {

        var scheduleInfo = new ScheduleInfoQuery().setScheduleId(scheduleId).execute(client);
        System.out.printf("Schedule info: %s%n", scheduleInfo);
    }

    private static TransactionReceipt signSchedule(Client client, Account account, ScheduleId scheduleId)
            throws TimeoutException, PrecheckStatusException, ReceiptStatusException {

        TransactionReceipt transactionReceipt = new ScheduleSignTransaction().setScheduleId(scheduleId).freezeWith(client).sign(account.privateKey())
                .execute(client).getReceipt(client).validateStatus(true);
        System.out.printf("Signed schedule id %s with account id %s, transaction status: %s%n", scheduleId, account.getId(),
                transactionReceipt.status);
        return transactionReceipt;
    }

    private static ScheduleId createScheduledTransaction(Client client, AccountId multiSigAccountId)
            throws TimeoutException, PrecheckStatusException, ReceiptStatusException {

        TransactionResponse transaction = new TransferTransaction().addHbarTransfer(multiSigAccountId, Hbar.fromTinybars(23).negated())
                .addHbarTransfer(ACCOUNT2.getId(), Hbar.fromTinybars(23)).setTransactionMemo("This is a scheduled Tx").schedule().execute(client);
        return transaction.getReceipt(client).validateStatus(true).scheduleId;
    }

    private static AccountId createMultiSigAccount(Client client) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        final var keyList = KeyList.withThreshold(2);
        keyList.add(ACCOUNT1.getPublicKey());
        keyList.add(ACCOUNT2.getPublicKey());
        keyList.add(ACCOUNT3.getPublicKey());

        TransactionResponse transactionResponse = new AccountCreateTransaction().setKey(keyList).setInitialBalance(Hbar.fromTinybars(1_000))
                .setAccountMemo("2-of-3 multi-sig account").execute(client);

        var txAccountCreateReceipt = transactionResponse.getReceipt(client);
        var multiSigAccountId = Objects.requireNonNull(txAccountCreateReceipt.accountId);

        System.out.printf("2-of-3 multi-sig account ID: %s%n", multiSigAccountId);
        return multiSigAccountId;
    }
}
