package com.ericsson.ei.waitlist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.ericsson.ei.handlers.MatchIdRulesHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.util.JSON;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class TestWaitListWorker {

    private static File qpidConfig = null;
    static AMQPBrokerManager amqpBrocker;
    static ConnectionFactory cf;
    static Connection conn;
    private static String jsonFileContent;
    private ArrayList<String> list = new ArrayList<>();
    private final static String eventPath = "src/test/resources/EiffelArtifactCreatedEvent.json";
    private final String input1 = "src/test/resources/testWaitListinput1.json";
    private final String input2 = "src/test/resources/testWaitListinput2.json";
    protected String message;

    @InjectMocks
    WaitListWorker waitListWorker;
    @Mock
    RulesHandler rulesHandler;
    @Mock
    MatchIdRulesHandler matchId;
    @Mock
    RmqHandler rmqHandler;
    @Mock
    WaitListStorageHandler waitListStorageHandler;
    @Mock
    MongoDBHandler mongoDBHandler;
    @Mock
    ArrayList<String> newList;
    @Mock
    JmesPathInterface jmesPathInterface;
    @Mock
    JsonNode jsonNode;
    @Mock
    RulesObject rulesObject;


    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        list.add(FileUtils.readFileToString(new File(input1)));
        list.add(FileUtils.readFileToString(new File(input2)));
        Mockito.when(mongoDBHandler.dropDocument(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(waitListStorageHandler.getWaitList()).thenReturn(list);
        Mockito.when(rulesHandler.getRulesForEvent(Mockito.anyString())).thenReturn(rulesObject);
        Mockito.when(jmesPathInterface.runRuleOnEvent(Mockito.anyString(), Mockito.anyString())).thenReturn(jsonNode);
    }

    public static class AMQPBrokerManager {
        private String path;
        private static final String PORT = "8672";
        private final Broker broker = new Broker();

        public AMQPBrokerManager(String path) {
            super();
            this.path = path;
        }

        public void startBroker() throws Exception {
            final BrokerOptions brokerOptions = new BrokerOptions();
            brokerOptions.setConfigProperty("qpid.amqp_port", PORT);
            brokerOptions.setConfigProperty("qpid.pass_file", "src/test/resources/configs/password.properties");
            brokerOptions.setInitialConfigurationLocation(path);
            broker.startup(brokerOptions);
        }

        public void stopBroker() {
            broker.shutdown();
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        System.setProperty("rabbitmq.port", "8672");
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");
        String config = "src/test/resources/configs/qpidConfig.json";
        jsonFileContent = FileUtils.readFileToString(new File(eventPath));
        qpidConfig = new File(config);
        amqpBrocker = new AMQPBrokerManager(qpidConfig.getAbsolutePath());
        amqpBrocker.startBroker();
        cf = new ConnectionFactory();
        cf.setUsername("guest");
        cf.setPassword("guest");
        cf.setPort(8672);
        conn = cf.newConnection();

    }

    @Test
    public void testRunWithoutMatchObjects() {
        Mockito.when(matchId.fetchObjectsById(Mockito.anyObject(), Mockito.anyString())).thenReturn(newList);
        try {
            waitListWorker.run();
            assertTrue(true);
        } catch (Exception e) {
            assertFalse(true);
            e.printStackTrace();
        }
    }

    @Test
    public void testRunWithMatchbjects() {
        Mockito.when(matchId.fetchObjectsById(Mockito.anyObject(), Mockito.anyString())).thenReturn(list);
        try {
            waitListWorker.run();
            assertTrue(true);
        } catch (Exception e) {
            assertFalse(true);
            e.printStackTrace();
        }
    }

    @Test
    public void testDropDocumentFromWaitList() {
        try {
            String event = FileUtils.readFileToString(new File(eventPath));
            String condition = "{Event:" + JSON.parse(event).toString() + "}";
            assertTrue(waitListWorker.dropDocumentFromWaitList(condition));
        } catch (Exception e) {
            assertFalse(true);
            System.out.println("error occured while deleting document from waitlist");
        }
    }

    @Test
    public void testPublishandReceiveEvent() {
        try {
            Channel channel = conn.createChannel();
            String queueName = "er001-eiffelxxx.eiffelintelligence.messageConsumer.durable";
            String exchange = "ei-poc-4";
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {
                    message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                }
            };
            channel.basicConsume(queueName, true, consumer);
            channel.basicPublish(exchange, queueName, null, jsonFileContent.getBytes());
            Thread.sleep(1001);
            assertTrue(message != null);
            assertTrue(message.equals(jsonFileContent));
        } catch (Exception e) {
            assertFalse(true);
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        amqpBrocker.stopBroker();
        try {
            conn.close();
        } catch (Exception e) {
            // We try to close the connection but if
            // the connection is closed we just receive the
            // exception and go on
        }

    }

}
