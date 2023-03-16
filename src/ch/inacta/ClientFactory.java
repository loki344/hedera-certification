package ch.inacta;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;

public class ClientFactory {

    private static final AccountId operatorId = AccountId.fromString(Config.OPERATOR_ID);
    private static final PrivateKey operatorKey = PrivateKey.fromString(Config.OPERATOR_KEY);

    private ClientFactory() {

    }

    private static final Client client = createClient();

    private static Client createClient() {

        var client = Client.forName(Config.NETWORK_NAME);
        client.setOperator(operatorId, operatorKey);
        return client;
    }

    public static Client getClient() {

        return client;
    }

}
