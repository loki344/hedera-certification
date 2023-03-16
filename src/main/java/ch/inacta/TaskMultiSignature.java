package ch.inacta;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionRecordQuery;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static ch.inacta.Config.ACCOUNT1;
import static ch.inacta.Config.ACCOUNT2;
import static ch.inacta.Config.ACCOUNT3;
import static ch.inacta.Config.ACCOUNT4;

public class TaskMultiSignature {

    public static void main(String[] args) throws TimeoutException, ReceiptStatusException, PrecheckStatusException {

        try (final var client = ClientFactory.getClient()) {

            var multiSigAccountId = createMultiSigAccount(client);

            try {
                // this transaction will fail because only one signature is given
                var transactionId = createTransaction(client, multiSigAccountId, List.of(ACCOUNT1));
                logTransactionInfo(client, transactionId);
            } catch (Exception e){
                System.out.printf("Transaction failed with exception %s%n", e);
            }

            var transactionId = createTransaction(client, multiSigAccountId, List.of(ACCOUNT1, ACCOUNT2));
            logTransactionInfo(client, transactionId);

        }
    }

    private static void logTransactionInfo(Client client, TransactionId transactionId) throws TimeoutException, PrecheckStatusException {
        var transactionInfo = new TransactionRecordQuery().setTransactionId(transactionId).execute(client);

        System.out.printf("Transaction info: %s%n", transactionInfo);
    }

    private static TransactionId createTransaction(Client client, AccountId senderAccountId, List<Account> signers)
            throws TimeoutException, PrecheckStatusException, ReceiptStatusException {

        var transaction = new TransferTransaction().addHbarTransfer(senderAccountId, Hbar.from(10).negated())
                .addHbarTransfer(ACCOUNT4.getId(), Hbar.from(10)).freezeWith(client);

        for (Account signer : signers) {
            System.out.printf("Signing transaction with account id: %s%n", signer.getId());
            transaction.sign(signer.getPrivateKey());
        }

        return transaction.execute(client).getReceipt(client).validateStatus(true).transactionId;
    }

    private static AccountId createMultiSigAccount(Client client) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        final var keyList = KeyList.withThreshold(2);
        keyList.add(ACCOUNT1.getPublicKey());
        keyList.add(ACCOUNT2.getPublicKey());
        keyList.add(ACCOUNT3.getPublicKey());

        TransactionResponse transactionResponse = new AccountCreateTransaction().setKey(keyList).setInitialBalance(Hbar.from(20))
                .setAccountMemo("2-of-3 multi-sig account").execute(client);

        var txAccountCreateReceipt = transactionResponse.getReceipt(client).validateStatus(true);
        var multiSigAccountId = Objects.requireNonNull(txAccountCreateReceipt.accountId);

        System.out.printf("2-of-3 multi-sig account ID: %s%n", multiSigAccountId);
        return multiSigAccountId;
    }
}
