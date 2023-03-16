package ch.inacta.existing;

import ch.inacta.ClientFactory;
import ch.inacta.ContractHelper;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCreateFlow;
import com.hedera.hashgraph.sdk.ContractDeleteTransaction;
import com.hedera.hashgraph.sdk.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static ch.inacta.Config.MY_ACCOUNT;

public class ExerciseSmartContractService {

    public static void main(String[] args) throws TimeoutException, IOException, ReceiptStatusException, PrecheckStatusException {

        try (final Client client = ClientFactory.getClient()) {

            var contractId = createContract(client);
            invokeContract(client, contractId);
            deleteContract(client, contractId);
        }

    }

    private static void deleteContract(Client client, ContractId contractId)
            throws ReceiptStatusException, TimeoutException, PrecheckStatusException {

        new ContractDeleteTransaction().setContractId(contractId).setTransferAccountId(MY_ACCOUNT.getId()).freezeWith(client)
                .sign(MY_ACCOUNT.getPrivateKey()).execute(client).getReceipt(client).validateStatus(true);
        System.out.printf("Deleted contract id: %s%n", contractId);
    }

    private static void invokeContract(Client client, ContractId contractId)
            throws TimeoutException, PrecheckStatusException, ReceiptStatusException {

        var response = new ContractExecuteTransaction().setContractId(contractId).setGas(100_000)
                .setFunction("function1", new ContractFunctionParameters().addUint16(5).addUint16(6)).execute(client);

        int result = Objects.requireNonNull(response.getRecord(client).validateReceiptStatus(true).contractFunctionResult).getInt32(0);

        System.out.printf("Invoked function1 of contract id %s with arguments 5 and 6 and received result: %s%n", contractId, result);
    }

    private static ContractId createContract(Client client) throws ReceiptStatusException, TimeoutException, PrecheckStatusException, IOException {

        final var receipt = new ContractCreateFlow().setBytecode(ContractHelper.getBytecodeHex("CertificationC1.json"))
                .setConstructorParameters(new ContractFunctionParameters()).setGas(14_000_000).setAdminKey(MY_ACCOUNT.privateKey()).execute(client)
                .getReceipt(client).validateStatus(true);

        final var contractId = Objects.requireNonNull(receipt.contractId);
        System.out.printf("Created contract with id: %s. Status: %s%n", contractId, receipt.status);

        return contractId;
    }

}