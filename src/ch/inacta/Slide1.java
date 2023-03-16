package ch.inacta;

import com.hedera.hashgraph.sdk.*;

import java.util.concurrent.TimeoutException;

public class Slide1 {

    // How much each account should be initially funded with.
    public static final Hbar INITIAL_FUNDING = Hbar.from(10);

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        try (final var client = ClientFactory.getClient()) {

            int accountsToCreate = 5;
            System.out.printf("Generating %d accounts...%n", accountsToCreate);

            for (int i = 1; i <= accountsToCreate; i++) {

                final var privateKey = PrivateKey.generateED25519();
                System.out.printf("ACCOUNT%d_KEY=%s%n", i, privateKey);

                final var publicKey = privateKey.getPublicKey();
                System.out.printf("ACCOUNT%d_PUBLIC_KEY=%s%n", i, publicKey);

                final var createAccountResponse = new AccountCreateTransaction().setKey(publicKey).setInitialBalance(INITIAL_FUNDING).execute(client);

                final var receipt = createAccountResponse.getReceipt(client).validateStatus(true);

                System.out.printf("ACCOUNT%d_ID=%s%n", i, receipt.accountId);
                System.out.println();
            }
        }
    }
}
