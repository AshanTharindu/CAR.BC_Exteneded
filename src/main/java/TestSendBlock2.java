import Exceptions.FileUtilityException;
import chainUtil.ChainUtil;
import chainUtil.KeyGenerator;
import config.CommonConfigHolder;
import constants.Constants;
import core.blockchain.*;
import core.communicationHandler.MessageSender;
import network.Client.RequestMessage;
import network.Node;
import network.Protocol.BlockMessageCreator;
import org.json.JSONObject;
import org.slf4j.impl.SimpleLogger;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class TestSendBlock2 {
    public static void main(String[] args) throws FileUtilityException {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");

        /*
        * Set the main directory as home
        * */
        System.setProperty(Constants.CARBC_HOME, System.getProperty("user.dir"));

        /*
        * At the very beginning
        * A Config common to all: network, blockchain, etc.
        * */
        CommonConfigHolder commonConfigHolder = CommonConfigHolder.getInstance();
        commonConfigHolder.setConfigUsingResource("peer2");

        /*
        * when initializing the network
        * */
        Node node = Node.getInstance();
        node.init();

        /*
        * when we want our node to start listening
        * */
        node.startListening();

        /*
        * when we want to send a block
        * */
//        JSONObject ourBlock = new JSONObject();
//        JSONObject ourBlock1 = new JSONObject();
//        ourBlock1.put("firstName", "Ashan");
//        ourBlock1.put("lastName", "Tharindu");
//        ourBlock.put("personDetails",ourBlock1);
//        RequestMessage blockMessage = BlockMessageCreator.createBlockMessage(ourBlock);
//        blockMessage.addHeader("keepActive", "false");
//        blockMessage.addHeader("messageType", "AgreementRequest");
//        node.sendMessageToNeighbour(1, blockMessage);

        try {
            byte[] prevhash = ChainUtil.hexStringToByteArray("1234");
            byte[] hash = ChainUtil.hexStringToByteArray("5678");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            byte[] data = ChainUtil.hexStringToByteArray("1456");
            byte[] signatue1 = ChainUtil.hexStringToByteArray("3332");
            byte[] signatue2 = ChainUtil.hexStringToByteArray("3442");
            PublicKey publicKey = KeyGenerator.getInstance().getPublicKey();
            Validator validator1 = new Validator("val1pubkey","owner",true,3);
            Validator validator2 = new Validator("val2pubkey","seller",true,4);
            ArrayList<Validation> validations = new ArrayList<>();
            validations.add(new Validation(validator1,"3332"));
            validations.add(new Validation(validator2,"3442"));
            BlockHeader blockHeader = new BlockHeader("101","1234",timestamp,
                    "senderPubkey",123,true);
            Transaction transaction = new Transaction("senderpubkey",validations,"1456",
                    "tran1",new TransactionInfo());

            Block block = new Block(blockHeader,transaction);
            JSONObject jsonObject = new JSONObject(block);
            String myJson = jsonObject.toString();
            System.out.println(myJson);
          //  MessageSender.getInstance().requestAgreement(block,1);

            Validator validator3 = new Validator(KeyGenerator.getInstance().getEncodedPublicKeyString(KeyGenerator.getInstance().getPublicKey()),"owner",true,3);
            Validator validator4 = new Validator("v2","seller",true,4);
            ArrayList<Validator> validators = new ArrayList<>();
            validators.add(validator3);
            //validators.add(validator2);
            Calendar calendar = Calendar.getInstance();
            java.util.Date now = calendar.getTime();

            java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

            TransactionInfo transactionInfo = new TransactionInfo();

            TransactionProposal proposal = new TransactionProposal(KeyGenerator.getInstance().getEncodedPublicKeyString(KeyGenerator.getInstance().getPublicKey()),validators,
                    "data","proposal1",null,transactionInfo);

            proposal.sendProposal();

        } catch (Exception e) {
            e.getMessage();
        }
    }
}
