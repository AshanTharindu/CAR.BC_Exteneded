package network.communicationHandler;

import chainUtil.ChainUtil;
import com.google.gson.Gson;
import core.blockchain.*;
import chainUtil.KeyGenerator;
import core.blockchain.Block;
import core.blockchain.Transaction;
import core.blockchain.TransactionProposal;
import core.consensus.Consensus;
import network.Client.RequestMessage;
import network.Neighbour;
import network.Node;
import network.Protocol.*;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

public class MessageSender {

    private static  MessageSender messageSender;

    private MessageSender() {};

    public static MessageSender getInstance() {
        if(messageSender == null) {
            messageSender = new MessageSender();
        }
        return messageSender;
    }

    //messages of new protocol
    public void requestIP(int ListeningPort) throws IOException {

//        URL whatismyip = new URL("http://checkip.amazonaws.com");
//        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
//        String ip = in.readLine(); //you get the IP as a String

        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("ip",ip);
        jsonObject.put("ListeningPort",ListeningPort);

        RequestMessage requestIPMessage = RequestIPMessageCreator.createRequestIPMessage(jsonObject);
        requestIPMessage.addHeader("keepActive", "false");
//        Node.getInstance().sendMessageToNeighbour(1, blockMessage);
        Node.getInstance().sendMessageToPeer("127.0.0.1", 49154,requestIPMessage);
    }

    public void sendHelloResponse(int listeningPort, String clientIP, int clientPort) {
        JSONObject portInfo = new JSONObject();
        portInfo.put("ListeningPort", listeningPort);
        RequestMessage helloResponse = HelloResponseCreator.createHelloResponseMessage(portInfo);
        Node.getInstance().sendMessageToPeer(clientIP, clientPort, helloResponse);
    }

    public void requestBlockchainHash() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ListeningPort",Node.getInstance().getNodeConfig().getListenerPort());
        RequestMessage blockChainRequest = BlockChainHashRequestCreator.createBlockChainHashRequest(jsonObject);
        blockChainRequest.addHeader("keepActive", "false");
        Consensus.getInstance().setBlockchainRequest(Node.getInstance().getNodeConfig().getNeighbours().size());
        Node.getInstance().broadcast(blockChainRequest);
        System.out.println("requestBlockchainHash");
    }

    public void requestBlockchain() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ListeningPort",Node.getInstance().getNodeConfig().getListenerPort());
        RequestMessage blockChainRequest = BlockChainRequestCreator.createBlockChainRequest(jsonObject);
        blockChainRequest.addHeader("keepActive", "false");
        Node.getInstance().broadcast(blockChainRequest);
    }

    public void requestBlockchainFromPeer(String ip, int listeningPort) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ListeningPort",Node.getInstance().getNodeConfig().getListenerPort());
        RequestMessage blockChainRequest = BlockChainRequestCreator.createBlockChainRequest(jsonObject);
        blockChainRequest.addHeader("keepActive", "false");
        Node.getInstance().sendMessageToPeer(ip,listeningPort,blockChainRequest);
    }

    public void sendSignedBlockChain(String ip, int listeningPort, String signedBlockchain, String blockchainHash) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("signedBlockchain", signedBlockchain);
        jsonObject.put("blockchainHash", blockchainHash);
        jsonObject.put("publicKey",KeyGenerator.getInstance().getPublicKeyAsString());
        RequestMessage blockSignMessage = BlockChainSignCreator.createBlockChainSignRequest(jsonObject);
        Node.getInstance().sendMessageToPeer(ip, listeningPort, blockSignMessage);
        System.out.println("sendSignedBlockChain");
    }

    public void sendBlockchainToPeer(String ip, int listeningPort, String jsonBlockchain, int blockchainLength) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("blockchain", jsonBlockchain);
        jsonObject.put("blockchainLength", blockchainLength);
        RequestMessage blockchainSendMessage = BlockchainSendMessageCreator.createBlockchainSendMessage(jsonObject);
        Node.getInstance().sendMessageToPeer(ip,listeningPort,blockchainSendMessage);
        System.out.println("blockchain sent");
    }


    public void BroadCastBlock(Block block) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("block",blockToJSON(block));
        RequestMessage blockMessage = BlockMessageCreator.createBlockMessage(jsonObject);
        blockMessage.addHeader("keepActive", "false");
        blockMessage.addHeader("messageType", "BlockBroadcast");
        for(Neighbour neighbour: Node.getInstance().getNodeConfig().getNeighbours()) {
            Node.getInstance().sendMessageToPeer(neighbour.getIp(),neighbour.getPort(),blockMessage);
        }
    }

    public void sendAgreement(String signedBlock, String blockHash) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("signedBlock", signedBlock);
        jsonObject.put("blockHash", blockHash);
        RequestMessage agreementMessage = AgreementCreator.createAgreementRequest(jsonObject);
        Node.getInstance().broadcast(agreementMessage);
    }



    public void requestAgreement(Block block, int neighbourIndex) {
        System.out.println("Agreement request send");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("block",blockToJSON(block).toString());
        RequestMessage blockMessage = BlockMessageCreator.createBlockMessage(jsonObject);
        blockMessage.addHeader("keepActive", "false");
        blockMessage.addHeader("messageType", "AgreementRequest");
        Node.getInstance().sendMessageToNeighbour(neighbourIndex, blockMessage);
    }

    public void sendAgreement(Block block, int neighbourIndex, String agreement, byte[] signature) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("block",blockToJSON(block).toString());
        jsonObject.put("agreement",agreement);
        jsonObject.put("signature",ChainUtil.bytesToHex(signature));
        jsonObject.put("publickey",KeyGenerator.getInstance().getPublicKeyAsString());
        RequestMessage blockMessage = BlockMessageCreator.createBlockMessage(jsonObject);
        blockMessage.addHeader("keepActive", "false");
        blockMessage.addHeader("messageType", "AgreementResponse");
        Node.getInstance().sendMessageToNeighbour(neighbourIndex, blockMessage);
    }

    public void reqestTransactionValidation(TransactionProposal transactionProposal, int neighbourIndex) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("transactionProposal",new JSONObject(transactionProposal).toString());
        RequestMessage blockMessage = BlockMessageCreator.createBlockMessage(jsonObject);
        blockMessage.addHeader("keepActive", "false");
        blockMessage.addHeader("messageType", "TransactionProposal");
        Node.getInstance().sendMessageToNeighbour(neighbourIndex, blockMessage);
    }

    public void sendTransactionValidation(TransactionResponse transactionResponse, int neighbourIndex) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("transactionResponse",new Gson().toJson(transactionResponse));
        //jsonObject.put("signature",ChainUtil.bytesToHex(signature));
        RequestMessage blockMessage = BlockMessageCreator.createBlockMessage(jsonObject);
        blockMessage.addHeader("keepActive", "false");
        blockMessage.addHeader("messageType", "TransactionValidation");
        Node.getInstance().sendMessageToNeighbour(neighbourIndex, blockMessage);
    }

    public String blockToJSON(Block block) {
        Gson gson = new Gson();
        return gson.toJson(block);
    }

    public String transactionToJSON(Transaction transaction) {
        Gson gson = new Gson();
        return gson.toJson(transaction);
    }

}
