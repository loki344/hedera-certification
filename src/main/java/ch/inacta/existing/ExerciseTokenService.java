package ch.inacta.existing;

import ch.inacta.Account;
import ch.inacta.ClientFactory;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenGrantKycTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenSupplyType;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TransferTransaction;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static ch.inacta.Config.ACCOUNT2;
import static ch.inacta.Config.ACCOUNT3;
import static ch.inacta.Config.MY_ACCOUNT;

public class ExerciseTokenService {

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        try (final var client = ClientFactory.getClient()) {

            // Setup: Create a token, associate & kyc account2, fund it
            var tokenId = createToken(client);
            associateAccount(ACCOUNT2, tokenId, client);
            performKycForAccount(ACCOUNT2, tokenId, client);
            transfer(MY_ACCOUNT, ACCOUNT2.getId(), tokenId, 1299, client);

            // Associate account 3 with the token
            associateAccount(ACCOUNT3, tokenId, client);

            try {
                // Try to transfer from account 2 to account 3, this will fail because it is not kyc'd yet
                transfer(ACCOUNT2, ACCOUNT3.getId(), tokenId, 1299, client);

            } catch (Exception e) {
                System.out.printf("Transfer failed with exception %s%n", e);
                // Now perform the kyc and try again
                performKycForAccount(ACCOUNT3, tokenId, client);
                transfer(ACCOUNT2, ACCOUNT3.getId(), tokenId, 1299, client);
            }

        }
    }

    private static void performKycForAccount(Account account, TokenId tokenId, Client client)
            throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        var tokenGrantKycTransaction = new TokenGrantKycTransaction().setAccountId(account.getId()).setTokenId(tokenId).freezeWith(client)
                .execute(client);

        var tokenGrantKycReceipt = tokenGrantKycTransaction.getReceipt(client).validateStatus(true);

        System.out.printf("Granted kyc to account %s tokenId %s. Status: %s%n", account.getId(), tokenId, tokenGrantKycReceipt.status);
    }

    private static void transfer(Account sender, AccountId recipientAccountId, TokenId tokenId, int amount, Client client)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {

        var tokenTransaction = new TransferTransaction().addTokenTransfer(tokenId, sender.getId(), -amount)
                .addTokenTransfer(tokenId, recipientAccountId, amount).freezeWith(client).sign(sender.getPrivateKey()).execute(client);

        var tokenTransactionReceipt = tokenTransaction.getReceipt(client).validateStatus(true);

        System.out.printf("Sent %d from account %s to account %s. tokenId %s. Status: %s%n", amount, sender.getId(), recipientAccountId, tokenId,
                tokenTransactionReceipt.status);
    }

    private static void associateAccount(Account account, TokenId tokenId, Client client)
            throws TimeoutException, PrecheckStatusException, ReceiptStatusException {

        var accountIdToAssociate = account.getId();
        var tokenAssociateTransaction = new TokenAssociateTransaction().setAccountId(accountIdToAssociate).setTokenIds(List.of(tokenId))
                .freezeWith(client).sign(account.getPrivateKey()).execute(client);

        var tokenAssociateReceipt = tokenAssociateTransaction.getReceipt(client).validateStatus(true);

        System.out.printf("Associated Account %s with token %s. Status: %s%n", accountIdToAssociate, tokenId,
                tokenAssociateReceipt.status.toString());
    }

    private static TokenId createToken(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {

        var createTokenTransaction = new TokenCreateTransaction().setTokenName("My Cool Token").setTokenSymbol("MCT").setDecimals(2)
                .setTokenType(TokenType.FUNGIBLE_COMMON).setSupplyType(TokenSupplyType.FINITE).setMaxSupply(100000).setInitialSupply(100000)
                .setTreasuryAccountId(MY_ACCOUNT.getId()).setAdminKey(MY_ACCOUNT.getPublicKey()).setFreezeKey(MY_ACCOUNT.getPublicKey())
                .setWipeKey(MY_ACCOUNT.getPublicKey()).setKycKey(MY_ACCOUNT.getPublicKey()).setSupplyKey(MY_ACCOUNT.getPublicKey()).freezeWith(client)
                .sign(MY_ACCOUNT.getPrivateKey()).execute(client);

        var createTokenReceipt = createTokenTransaction.getReceipt(client).validateStatus(true);

        var tokenId = Objects.requireNonNull(createTokenReceipt.tokenId);

        System.out.printf("Created token with id %s%n", tokenId);
        return tokenId;
    }
}
