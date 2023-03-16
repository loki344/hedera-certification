package ch.inacta.existing;

import ch.inacta.ClientFactory;
import ch.inacta.Config;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicMessageSubmitTransaction;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class ExerciseConsensusService {

    public static void main(String[] args) throws TimeoutException, ReceiptStatusException, PrecheckStatusException {

        try (final var client = ClientFactory.getClient()) {

            final var topicCreationResponse = new TopicCreateTransaction().freezeWith(client).sign(Config.ACCOUNT1.getPrivateKey()).execute(client);
            final var topicCreationReceipt = topicCreationResponse.getReceipt(client).validateStatus(true);
            final var topicId = Objects.requireNonNull(topicCreationReceipt.topicId);

            System.out.println("Topic ID is: " + topicId);

            final var submissionDate = LocalTime.now().format(DateTimeFormatter.ISO_TIME);
            final var messageSubmitResponse = new TopicMessageSubmitTransaction().setTopicId(topicId).setMessage(submissionDate).execute(client);
            messageSubmitResponse.getReceipt(client).validateStatus(true);

            System.out.println("Submission time: " + submissionDate);
        }
    }
}
