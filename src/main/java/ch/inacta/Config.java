package ch.inacta;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

public class Config {

    // How to find your operator id?
    // 1. Log into the Hedera portal: https://portal.hedera.com/?network=testnet
    // 2. Copy your Account ID and paste it here
    public static final String OPERATOR_ID = Dotenv.load().get("OPERATOR_ID");

    // How to find your operator key?
    // 1. Log into the Hedera portal: https://portal.hedera.com/?network=testnet
    // 2. Copy your "Private Key" and paste it here
    public static final String OPERATOR_KEY = Dotenv.load().get("OPERATOR_KEY");

    // Optional: the name of the network to use
    public static final String NETWORK_NAME = Dotenv.load().get("NETWORK_NAME", "testnet");

    public static final Account MY_ACCOUNT = new Account(AccountId.fromString(OPERATOR_ID), PrivateKey.fromString(OPERATOR_KEY));

    // The test accounts created in "Slide1"
    public static final Account ACCOUNT1 = !Objects.equals(Dotenv.load().get("ACCOUNT1_ID"), "TODO")
            ? new Account(AccountId.fromString(Dotenv.load().get("ACCOUNT1_ID")), PrivateKey.fromString(Dotenv.load().get("ACCOUNT1_KEY")))
            : null;

    public static final Account ACCOUNT2 = !Objects.equals(Dotenv.load().get("ACCOUNT2_ID"), "TODO")
            ? new Account(AccountId.fromString(Dotenv.load().get("ACCOUNT2_ID")), PrivateKey.fromString(Dotenv.load().get("ACCOUNT2_KEY")))
            : null;

    public static final Account ACCOUNT3 = !Objects.equals(Dotenv.load().get("ACCOUNT3_ID"), "TODO")
            ? new Account(AccountId.fromString(Dotenv.load().get("ACCOUNT3_ID")), PrivateKey.fromString(Dotenv.load().get("ACCOUNT3_KEY")))
            : null;

    public static final Account ACCOUNT4 = !Objects.equals(Dotenv.load().get("ACCOUNT4_ID"), "TODO")
            ? new Account(AccountId.fromString(Dotenv.load().get("ACCOUNT4_ID")), PrivateKey.fromString(Dotenv.load().get("ACCOUNT4_KEY")))
            : null;

    public static final Account ACCOUNT5 = !Objects.equals(Dotenv.load().get("ACCOUNT5_ID"), "TODO")
            ? new Account(AccountId.fromString(Dotenv.load().get("ACCOUNT5_ID")), PrivateKey.fromString(Dotenv.load().get("ACCOUNT5_KEY")))
            : null;

}
