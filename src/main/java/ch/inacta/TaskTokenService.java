package ch.inacta;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.CustomFixedFee;
import com.hedera.hashgraph.sdk.CustomRoyaltyFee;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.NftId;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenSupplyType;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TransferTransaction;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static ch.inacta.Config.ACCOUNT1;
import static ch.inacta.Config.ACCOUNT2;
import static ch.inacta.Config.ACCOUNT3;

public class TaskTokenService {

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        try (final var client = ClientFactory.getClient()) {

            // Setup: Create a token
            var tokenId = createToken(client);

            // mint nfts, associate Account 3 with the token, send nftNr2 to it
            mintNFTs(tokenId, client);
            associateAccount(ACCOUNT3, tokenId, client);
            sendNFTToAccount(ACCOUNT3, 2, tokenId, client);

        }
    }

    private static void sendNFTToAccount(Account recipientAccount, int NFTNumber, TokenId tokenId, Client client) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        TransferTransaction tokenTransferTx = new TransferTransaction().addNftTransfer(new NftId(tokenId, NFTNumber), ACCOUNT1.id(), recipientAccount.id()).freezeWith(client).sign(ACCOUNT1.privateKey());
        var tokenTransferSubmit = tokenTransferTx.execute(client);
        var tokenTransferRx = tokenTransferSubmit.getReceipt(client);
        System.out.println("NFT transfer from account1 to account3: " + tokenTransferRx.status);
        System.out.println("Transaction details: " + tokenTransferSubmit);
    }

    private static void mintNFTs(TokenId tokenId, Client client)
            throws ReceiptStatusException, PrecheckStatusException, TimeoutException {

        var transaction = new TokenMintTransaction().setTokenId(tokenId)
                .setMetadata(List.of("NFT 1".getBytes(StandardCharsets.UTF_8),
                        "NFT 2".getBytes(StandardCharsets.UTF_8),
                        "NFT 3".getBytes(StandardCharsets.UTF_8),
                        "NFT 4".getBytes(StandardCharsets.UTF_8),
                        "NFT 5".getBytes(StandardCharsets.UTF_8)));
        var txResponse = transaction.freezeWith(client).sign(ACCOUNT1.privateKey()).execute(client);
        var receipt = txResponse.getReceipt(client).validateStatus(true);
        var transactionStatus = receipt.status;
        System.out.println("The mint transaction consensus status is " + transactionStatus);
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

        var fee = new CustomRoyaltyFee().setFeeCollectorAccountId(ACCOUNT2.getId())
                .setNumerator(10).setDenominator(100).setFallbackFee(new CustomFixedFee().setHbarAmount(Hbar.from(200)));
        var createTokenTransaction = new TokenCreateTransaction()
                .setTokenName("My Certification NFT").setTokenSymbol("MCN").setDecimals(2)
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setSupplyType(TokenSupplyType.FINITE).setDecimals(0).setMaxSupply(5).setInitialSupply(0)
                .setAdminKey(ACCOUNT1.getPublicKey()).setSupplyKey(ACCOUNT1.getPublicKey())
                .setTreasuryAccountId(ACCOUNT1.getId())
                .setCustomFees(List.of(fee)).freezeWith(client).sign(ACCOUNT1.getPrivateKey()).execute(client);


        var createTokenReceipt = createTokenTransaction.getReceipt(client).validateStatus(true);

        var tokenId = Objects.requireNonNull(createTokenReceipt.tokenId);

        System.out.printf("Created token with id %s%n", tokenId);
        return tokenId;
    }
}
