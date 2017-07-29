package spn.os;

import spn.examples.OpticalMapping.OpticalMappingTenant;
import spn.os.element.VirtualNetwork;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.Observer;

public class Messenger implements MessageListener, Observer {
    private static int ackMode;
    private static String clientQueueName;
    private boolean transacted = false;
    private MessageProducer producer;
    private Session session;
    private Connection connection;
    private Destination tempDest;
    private List<Topic> topics;
    private List<MessageConsumer> topicConsumers;
    private HashMap<Integer, Tenant> tenantMap;

    static {
        clientQueueName = "client.messages";
        ackMode = Session.AUTO_ACKNOWLEDGE;
    }

    private String arbiterIpAddress;
    private Console console = Console.getConsole(this);

    public Messenger(String ipAddress) {
        arbiterIpAddress = ipAddress;
        tenantMap = new HashMap<Integer, Tenant>();
        topics = new ArrayList<Topic>();
        topicConsumers = new ArrayList<MessageConsumer>();

        try {
            connect();
            //connectToControllerAgent();
            //addTopicSubscription("ConnectPoint{elementId=of:0000000000000500, portNumber=7}-" +
            //                             "ConnectPoint{elementId=of:0000000000000600, portNumber=6}");

        } catch (JMSException e) {
            console.debug("Could not create a session with Arbiter right now");
        }

    }

    private void connect() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                "tcp://" + arbiterIpAddress + ":61616");

        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(transacted, ackMode);
        Destination adminQueue = session.createQueue(clientQueueName);

        // Setup a message producer to send message to
        // the queue the server is consuming from
        this.producer = session.createProducer(adminQueue);
        this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        tempDest = session.createTemporaryQueue();
        MessageConsumer responseConsumer = session.createConsumer(tempDest);

        responseConsumer.setMessageListener(this);
    }

    /**
     * DRAFT DRAFT DRAFT
     */
    private void connectToControllerAgent() {
        try {
            connectToControllerAgent(arbiterIpAddress);
        } catch (JMSException e) {
            console.error("Could not connect to controller agent", e.getMessage());
        }
    }

    /**
     * DRAFT DRAFT DRAFT
     *
     * @throws JMSException
     */
    private void connectToControllerAgent(String ipAddress) throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                "tcp://" + ipAddress + ":61618");
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(transacted, ackMode);

    }

    /**
     * DRAFT DRAFT DRAFT
     *
     * @throws JMSException
     */
    public void refreshControllerSubscriptions() throws JMSException {
        for (MessageConsumer mc : topicConsumers) {
            mc.close();
        }

        if (topics.isEmpty()) {
            return;
        }

        TopicMessageListener lstnr = new TopicMessageListener();

        for (Topic t : topics) {
            MessageConsumer mc = session.createConsumer(t);
            topicConsumers.add(mc);
            mc.setMessageListener(lstnr);
        }
    }

    /**
     * DRAFT DRAFT DRAFT
     *
     * @throws JMSException
     */
    public void addTopicSubscription(Topic t) throws JMSException {
        this.topics.add(t);
        refreshControllerSubscriptions();
    }

    /**
     * DRAFT DRAFT DRAFT
     *
     * @throws JMSException
     */
    public void addTopicSubscription(String topicName) throws JMSException {
        if (session == null) {
            if (connection == null) {
                console.error("Connection is null");
                return;
            }
            session = connection.createSession(transacted, ackMode);
        }

        console.debug("Creating topic");
        Topic t = session.createTopic(topicName);
        console.debug("Adding topic to list");
        this.topics.add(t);
        refreshControllerSubscriptions();
    }

    /**
     * DRAFT DRAFT DRAFT
     *
     * @throws JMSException
     */
    class TopicMessageListener implements MessageListener {
        @Override
        public void onMessage(Message message) {
            String content;
            try {
                content = ((TextMessage) message).getText();
                console.info("Received message on topic", content);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * This method should send the necessary json files for Arbiter to create
     * the VirtualNetwork object And receive the necessary json files for it to
     * create the VNO object
     *
     * @param tenant
     * @param virtual
     * @return
     */
    public void requestVNO(Tenant tenant, String vnoName,
                           VirtualNetwork virtual) {
        int tenantId = tenant.getId();
        tenantMap.put(tenantId, tenant);
        if (virtual == null) {
            console.error("Virtual Network is null");
            return;
        }
        String virtualNetworkJsonStr = virtual.toJsonString();

        try {
            sendCommand(virtualNetworkJsonStr, "LOAD",
                        vnoName, tenantId);

        } catch (JMSException e1) {
            console.debug(e1.getMessage());
            try {
                connect();
            } catch (JMSException e2) {
                console.error("Error trying to send request to Arbiter. Is Arbiter running?");
                //do nothing
            }
        }

    }


    /**
     * This method should send the necessary json files for Arbiter to create
     * the VirtualNetwork object And receive the necessary json files for it to
     * create the VNO object
     *
     * @param tenant  Tenant object
     * @param virtual Virtual network
     * @return
     */
    public void reloadVNO(Tenant tenant, String vnoName,
                          VirtualNetwork virtual) {
        int tenantId = tenant.getId();
        tenantMap.put(tenantId, tenant);
        String virtualNetworkJsonStr = virtual.toJsonString();

        try {
            sendCommand(virtualNetworkJsonStr, "RELOAD",
                        vnoName, tenantId);

        } catch (JMSException e) {
            // TODO handle appropriately
            e.printStackTrace();
        }

    }

    public void deleteVNO(VNO vno) {
        int tenantId = vno.getOwnerId();

        try {
            requestDelete(vno.getId(), vno.getName(), tenantId);

        } catch (JMSException e) {
            // TODO handle appropriately
            e.printStackTrace();
        }
    }


    private void sendCommand(Session session, Destination dest,
                             String jsonString, String cmd,
                             String vnoName, int tenantId) throws JMSException {

        if (session == null) {
            try {
                connect();
            } catch (JMSException e) {
                String err = "Could not establish a session with Arbiter.";
                throw new JMSException(err);
            }

            if (session == null) {
                String err = "Failed to establish a session with Arbiter.";
                throw new JMSException(err);
            }
        }

        TextMessage txtMessage = session.createTextMessage();
        txtMessage.setText(jsonString);
        txtMessage.setStringProperty("command", cmd);
        txtMessage.setStringProperty("vno-name", vnoName);
        txtMessage.setIntProperty("tenant-id", tenantId);


        // Set the reply to the queue the server
        // will respond to
        txtMessage.setJMSReplyTo(dest);

        String correlationId = this.createRandomString();
        txtMessage.setJMSCorrelationID(correlationId);
        this.producer.send(txtMessage);
    }

    private void sendCommand(Session session, Destination dest, int vnoId,
                             String cmd, String vnoName, int tenantId) throws JMSException {

        if (session == null) {
            try {
                connect();
            } catch (JMSException e) {
                String err = "Could not establish a session with Arbiter.";
                throw new JMSException(err);
            }

        }

        TextMessage txtMessage = session.createTextMessage();
        txtMessage.setStringProperty("command", cmd);
        txtMessage.setStringProperty("vno-name", vnoName);
        txtMessage.setIntProperty("vno-id", tenantId);
        txtMessage.setIntProperty("tenant-id", tenantId);

        txtMessage.setJMSReplyTo(dest);

        String correlationId = this.createRandomString();
        txtMessage.setJMSCorrelationID(correlationId);
        this.producer.send(txtMessage);
    }


    public void sendCommand(String jsonString, String cmd, String vnoName, int tenantId) throws JMSException {
        sendCommand(this.session, this.tempDest, jsonString, cmd,
                    vnoName, tenantId);
    }

    public void requestDelete(int vnoId, String vnoName, int tenantId)
            throws JMSException {
        sendCommand(this.session, this.tempDest, vnoId, "DELETE", vnoName,
                    tenantId);
    }

    public void sendStateUpdate(int vnoId, int tenantId, VNO.State newState)
            throws JMSException {
        TextMessage txtMessage = session.createTextMessage();
        txtMessage.setStringProperty("command", "VNO_STATE_UPDATE");
        txtMessage.setIntProperty("tenant-id", tenantId);
        txtMessage.setIntProperty("vno-id", vnoId);
        txtMessage.setStringProperty("new-vno-state", newState.toString());
        txtMessage.setJMSReplyTo(this.tempDest);
        String correlationId = this.createRandomString();
        txtMessage.setJMSCorrelationID(correlationId);
        this.producer.send(txtMessage);
    }

    @Deprecated
    public void registerController(int tenantId, int vnoId,
                                   String controllerIpPort) throws JMSException {
        TextMessage txtMessage = session.createTextMessage();
        txtMessage.setStringProperty("command", "REGISTER_CONTROLLER");
        txtMessage.setIntProperty("tenant-id", tenantId);
        txtMessage.setIntProperty("vno-id", vnoId);
        txtMessage.setStringProperty("controller-ip-port", controllerIpPort);
        txtMessage.setJMSReplyTo(this.tempDest);
        String correlationId = this.createRandomString();
        txtMessage.setJMSCorrelationID(correlationId);
        this.producer.send(txtMessage);
    }

    private String createRandomString() {
        Random random = new Random(System.currentTimeMillis());
        long randomLong = random.nextLong();
        return Long.toHexString(randomLong);
    }

    private List<String> reorderList(List<String> list) {
        if (list == null) {
            return null;
        }

        if (list.isEmpty() || list.size() == 1) {
            return list;
        }

        List<String> newList = new ArrayList<String>();
        int movingIndex = list.size() - 1;
        while (movingIndex >= 0) {
            newList.add(list.get(movingIndex--));
        }

        return newList;
    }

    @Override
    public void onMessage(Message message) {
        String messageText = null;
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                handleIncomingMessage(textMessage);
            }
        } catch (JMSException e) {
            // Handle the exception appropriately
        }
    }

    public void handleIncomingMessage(TextMessage txtMsg) throws JMSException {
//        console.info("<< Message Received ");

        String cmd = null;
        String jsonContent = null;
        Integer tenantId;
        int vnoId;
        String vnoName = null;
        String tenantHostListJson;
        String nwAddressSpace;

        try {
            cmd = txtMsg.getStringProperty("command");
            tenantId = txtMsg.getIntProperty("tenant-id");
            jsonContent = txtMsg.getText();

            if (cmd == null) {
                console.error("Received an invalid message");
                return;
            }

        } catch (Exception e) {

            // TODO handle appropriately
        }

//		Map<String, String> sfcJsonMap = new HashMap<String, String>();
//		List<String> rspInputs;
        String rply = txtMsg.getStringProperty("status-reply");

        switch (cmd) {
            case "LOAD":

                console.info("Status-Reply", rply);
                if (rply.equals("ERROR")) {
                    console.error(
                            "An error occurred on the remote side while processing request",
                            txtMsg.getText());
                } else if (rply.equals("FAIL")) {
                    console.error("VNO could not be allocated. Reason",
                                  txtMsg.getText());
                } else {
                    vnoId = txtMsg.getIntProperty("vno-id");
                    vnoName = txtMsg.getStringProperty("vno-name");
                    tenantId = txtMsg.getIntProperty("tenant-id");
                    tenantHostListJson = txtMsg.getStringProperty("host-list");
                    nwAddressSpace = txtMsg.getStringProperty("network-address-space");


                    // TODO hard-coding for mapping tenant example
                    String physicalJsonStr = txtMsg
                            .getStringProperty("physical-network");

                    List<String> virtualMappingsJsonStr = (List<String>) txtMsg.getObjectProperty("virtual_mappings");
                    List<String> opticalMappingsJsonStr = (List<String>) txtMsg.getObjectProperty("optical_mappings");
//                    virtualMappingsJsonStr = reorderList(virtualMappingsJsonStr);
//                    opticalMappingsJsonStr = reorderList(opticalMappingsJsonStr);

//                    console.debug("Received physical network:");
//                    console.json(physicalJsonStr);
//                    console.debug("Received virtual mapping:");
//                    console.json(virtualMappingsJsonStr.get(0));
//                    console.info("Received optical mapping #1:");
//                    console.json(opticalMappingsJsonStr.get(0));
//                    console.info("Received optical mapping #2:");
//                    console.json(opticalMappingsJsonStr.get(1));

                    // Retrieve tenant obj from tenant ID
                    Tenant tnt = tenantMap.get(tenantId);
                    tnt.buildVNO(vnoId, vnoName, physicalJsonStr,
                                 virtualMappingsJsonStr,
                                 opticalMappingsJsonStr,
                                 tenantHostListJson, nwAddressSpace);
//                    VNO vno = tnt.getVNO(vnoId);
//                    console.info("Sending a request to register this VNO's Controller >>");
                    /*registerController(tnt.getId(), vnoId,
                                       vno.getControllerIpPort(), vno.getSfcMgr()
                                               .getIpAddress());*/
                }
                break;
            case "RELOAD":

                console.info("Status-Reply", rply);
                if (rply.equals("ERROR")) {
                    console.error(
                            "An error occurred on the remote side while processing request",
                            txtMsg.getText());
                } else if (rply.equals("FAIL")) {
                    console.error("VNO could not be allocated. Reason",
                                  txtMsg.getText());
                } else {
                    vnoId = txtMsg.getIntProperty("vno-id");
                    vnoName = txtMsg.getStringProperty("vno-name");
                    tenantId = txtMsg.getIntProperty("tenant-id");
                    tenantHostListJson = txtMsg.getStringProperty("host-list");
                    nwAddressSpace = txtMsg.getStringProperty("network-address-space");

                    // TODO hard-coding for mapping tenant example
                    String physicalJsonStr = txtMsg
                            .getStringProperty("physical-network");
                    List<String> virtualMappingsJsonStr = (List<String>) txtMsg.getObjectProperty("virtual_mappings");
                    List<String> opticalMappingsJsonStr = (List<String>) txtMsg.getObjectProperty("optical_mappings");


//				for (String property : SfcManager.SFC_PROPERTIES) {
//					sfcJsonMap
//							.put(property, txtMsg.getStringProperty(property));
//				}
//
//				rspInputs = (List<String>) txtMsg.getObjectProperty("rsp");

//                    console.info("<-- Received Physical Network");
////				console.debug(physicalJsonStr);
//                    console.info("<-- Received Virtual Mapping");
////				console.debug(virtualMapping1JsonStr);
//                    console.info("<-- Received Optical Mapping");
////				console.debug(opticalMapping1JsonStr);

                    // Retrieve tenant obj from tenant ID
                    Tenant tnt = tenantMap.get(tenantId);

                    tnt.updateVNO(vnoId, vnoName, physicalJsonStr,
                                  virtualMappingsJsonStr,
                                  opticalMappingsJsonStr,
                                  tenantHostListJson, nwAddressSpace);
                    VNO vno = tnt.getVNO(vnoId);

//				console.info(">> Sending a request to register this VNO's Controller");
//                    registerController(tnt.getId(), vnoId,
//                                       vno.getControllerIpPort()
//                                               .getIpAddress());
                }
                break;
            case "DELETE":
                if (txtMsg.getStringProperty("status-reply").equals("SUCCESS")) {
                    console.info("VNO was successfully deallocated");
                } else {
                    console.error(
                            "Some error occurred on the remote side while trying to remove VNO",
                            txtMsg.getText() != null ? txtMsg.getText() : "");
                }
                break;
            case "VNO_STATE_UPDATE":
                if (txtMsg.getStringProperty("status-reply").equals("ACK")) {
//                    console.info("State update acknowledged by Arbiter");
                }
                break;
            case "REGISTER_CONTROLLER":
//			if (txtMsg.getStringProperty("status-reply").equals("ERROR"))
//				console.error("An error occurred", txtMsg.getText());
//			else if (txtMsg.getStringProperty("status-reply").equals("FAIL"))
//				console.error("Request Failed", txtMsg.getText());
//			else if (txtMsg.getStringProperty("status-reply").equals("SUCCESS"))
//				console.info("Controller successfully registered.");
                console.info("VNO Loaded.\n");
                break;
            default:
                // TODO

        }

    }

    @Override
    public void update(Observable o, Object arg) {
        VNO vno = (VNO) o;
        VNO.State vnoState = (VNO.State) arg;

        String msg = String.format("VNO [%d] from Tenant [%d] has a new state",
                                   vno.getId(), vno.getOwnerId());
        console.info(msg, vnoState.toString());
        try {
            sendStateUpdate(vno.getId(), vno.getOwnerId(), vnoState);
        } catch (JMSException e) {
            console.error("Failed to notify Arbiter of state change",
                          e.getMessage());
        }
    }
}
