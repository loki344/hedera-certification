package ch.inacta;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;

public record Account(AccountId id, PrivateKey privateKey) {

    public AccountId getId() {

        return id;
    }

    public PrivateKey getPrivateKey() {

        return privateKey;
    }

    public PublicKey getPublicKey() {

        return privateKey.getPublicKey();
    }

    @Override
    public String toString() {

        return id.toString();
    }
}
