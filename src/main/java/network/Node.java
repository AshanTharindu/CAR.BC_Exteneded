package network;

import Exceptions.FileUtilityException;
import chainUtil.KeyGenerator;
import config.CommonConfigHolder;
import config.NodeConfig;
import constants.Constants;
import network.Client.Client;
import network.Client.RequestMessage;
import network.Listener.Listener;
import network.Protocol.HelloMessageCreator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Node {
    private final Logger log = LoggerFactory.getLogger(Node.class);
    private static final Node instance = new Node();
    Listener listener;
    Client client;
    private NodeConfig nodeConfig;
    private List<Neighbour> tempNeighbour;

    private Node() {
        tempNeighbour = new ArrayList<>();
    }

    public static Node getInstance() {
        return instance;
    }

    public void init() throws FileUtilityException {

        /* Set config and its parameters */
        Random random = new Random();
        long peerID = random.nextLong();

        //Create config
        this.nodeConfig = new NodeConfig(peerID);

        //Set port to listen on
        JSONObject commonConfig = CommonConfigHolder.getInstance().getConfigJson();
        nodeConfig.setListenerPort(commonConfig.getInt("listener_port"));

        //Add neighbours list
//        JSONArray neighbours = commonConfig.getJSONArray("neighbours");

        //getting ips if they available and rejoin netowrk
        if (KeyGenerator.getInstance().getResourcesFilePath("peersDetails.json") != null) {
            String resourcePath = System.getProperty(Constants.CARBC_HOME)
                    + "/src/main/resources/" + "peersDetails.json";
//            JSONObject peersListObject = new JSONObject(FileUtils.readFileContentAsText(resourcePath));

            String path = KeyGenerator.getInstance().getResourcesFilePath("peersDetails.json");
            System.out.println(path);
            JSONObject peersListObject = new JSONObject(FileUtils.readFileContentAsText(resourcePath));
            System.out.println(peersListObject.toString());
            JSONArray peersList = peersListObject.getJSONArray("peers");
            for (int i = 0; i < peersList.length(); i++) {
                System.out.println(peersList.getJSONObject(i).toString());
                JSONObject neighbourJson = peersList.getJSONObject(i);
                String neightbourIP = neighbourJson.getString("ip");
                int neightbourPort = neighbourJson.getInt("ListeningPort");
                Neighbour neighbour = new Neighbour(neightbourIP, neightbourPort);
                nodeConfig.addNeighbour(neighbour);
            }
        }


        log.info("Initializing Node:{}", peerID);

    }

    //revert later

    public void initTest() throws FileUtilityException {

        /* Set config and its parameters */
        Random random = new Random();
        long peerID = random.nextLong();

        //Create config
        this.nodeConfig = new NodeConfig(peerID);

        //Set port to listen on
        JSONObject commonConfig = CommonConfigHolder.getInstance().getConfigJson();
        nodeConfig.setListenerPort(commonConfig.getInt("listener_port"));

        //Add neighbours list

//        JSONArray neighbours = commonConfig.getJSONArray("neighbours");
//        for (int i = 0; i < neighbours.length(); i++) {
//            JSONObject neighbourJson = neighbours.getJSONObject(i);
//            String neightbourIP = neighbourJson.getString("ip");
//            int neightbourPort = neighbourJson.getInt("port");
//            Neighbour neighbour = new Neighbour(neightbourIP, neightbourPort);
//            nodeConfig.addNeighbour(neighbour);
//        }

        log.info("Initializing Node:{}", peerID);

    }

    public void startListening() {
        this.listener = new Listener();
        this.listener.init(nodeConfig.getListenerPort());
        this.listener.start();
        log.info("Initialized listener");
    }

    public void sendMessageToNeighbour(int neighnourIndex, RequestMessage requestMessage) {
        Client client = new Client();
        Neighbour neighbour1 = nodeConfig.getNeighbours().get(neighnourIndex);
        client.init(neighbour1, requestMessage);
        client.start();
        log.info("Initialized client");
    }

    //send message to a specific peer
    public void sendMessageToPeer(String IP, int port, RequestMessage requestMessage) {
        Client client = new Client();
        client.initTest(IP,port,requestMessage);
        client.start();
        log.info("Initialized client");
    }

    //broadcast message to the network
    public void broadcast(RequestMessage requestMessage) {
        for(Neighbour neighbour: nodeConfig.getNeighbours()) {
            Client client = new Client();
            client.initTest(neighbour.getIp(),neighbour.getPort(),requestMessage);
            client.start();
        }
    }

//    public void addPeers(String data) {
//        JSONObject peersList = new JSONObject(data);
//        JSONArray peers = peersList.getJSONArray("peers");
//        for (int i = 0; i < peers.length(); i++) {
//            JSONObject peersJson = peers.getJSONObject(i);
//            String peerIP = peersJson.getString("ip");
//            int peerPort = peersJson.getInt("ListeningPort");
////            String peerPublicKey = peersJson.getString("publicKey");
//            Neighbour neighbour = new Neighbour(peerIP,peerPort);
//            nodeConfig.addNeighbour(neighbour);
//        }
//    }

    public void addPeerToTempList(String data) {
        JSONObject peersList = new JSONObject(data);
        JSONArray peers = peersList.getJSONArray("peers");
        for (int i = 0; i < peers.length(); i++) {
            JSONObject peersJson = peers.getJSONObject(i);
            String peerIP = peersJson.getString("ip");
            int peerPort = peersJson.getInt("ListeningPort");
//            String peerPublicKey = peersJson.getString("publicKey");
            Neighbour neighbour = new Neighbour(peerIP,peerPort);
            tempNeighbour.add(neighbour);
        }
    }

    public void talkToPeers(List<Neighbour> neighbours) {
        for(Neighbour neighbour: neighbours) {
            JSONObject portInfo = new JSONObject();
            portInfo.put("ListeningPort",nodeConfig.getListenerPort());
            RequestMessage helloMsg = HelloMessageCreator.createHelloMessage(portInfo);
            sendMessageToPeer(neighbour.getIp(),neighbour.getPort(),helloMsg);
        }
    }

    public List<Neighbour> getTempNeighbour() {
        return tempNeighbour;
    }

    public void joinNetwork(String data) {
        addPeerToTempList(data);
        talkToPeers(tempNeighbour);
    }

    public void addActiveNeighbour(String ip, int port) {
        nodeConfig.addNeighbour(new Neighbour(ip, port));
        System.out.println("Active member added");
        System.out.println("*******************neighbour list******************");
        for(Neighbour neighbour: nodeConfig.getNeighbours()) {
            System.out.println("IP: "+ neighbour.getIp() + " port: " + neighbour.getPort());
        }
    }

    public NodeConfig getNodeConfig() {
        return nodeConfig;
    }

}
