package com.ericsson.ei.flowtests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

public class FlowTestBase {

    private static Logger log = LoggerFactory.getLogger(FlowTest.class);

    @Autowired
    RmqHandler rmqHandler;

    @Autowired
    ObjectHandler objectHandler;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    public static File qpidConfig = null;
    static AMQPBrokerManager amqpBrocker;
    protected static MongodForTestsFactory testsFactory;
    static MongoClient mongoClient = null;
    static Queue queue = null;
    static RabbitAdmin admin;

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

    static ConnectionFactory cf;
    static Connection conn;
    protected static String jsonFileContent;
    protected static JsonNode parsedJason;
    protected static String jsonFilePath = "src/test/resources/test_events.json";
    static protected String inputFilePath = "src/test/resources/AggregatedDocument.json";

    @BeforeClass
    public static void setup() throws Exception {
        System.setProperty("flow.test", "true");
        System.setProperty("eiffel.intelligence.processedEventsCount", "0");
        System.setProperty("eiffel.intelligence.waitListEventsCount", "0");
        setUpMessageBus();
        setUpEmbeddedMongo();
    }

    @PostConstruct
    public void initMocks() {
        mongoDBHandler.setMongoClient(mongoClient);
    }

    public static void setUpMessageBus() throws Exception {
        System.setProperty("rabbitmq.port", "8672");
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");


        String config = "src/test/resources/configs/qpidConfig.json";
        jsonFileContent = FileUtils.readFileToString(new File(jsonFilePath));
        ObjectMapper objectmapper = new ObjectMapper();
        parsedJason = objectmapper.readTree(jsonFileContent);
        qpidConfig = new File(config);
        amqpBrocker = new AMQPBrokerManager(qpidConfig.getAbsolutePath());
        amqpBrocker.startBroker();
        cf = new ConnectionFactory();
        cf.setUsername("guest");
        cf.setPassword("guest");
        cf.setPort(8672);
        conn = cf.newConnection();
    }

    public static void setUpEmbeddedMongo() throws Exception {
         testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
         mongoClient = testsFactory.newMongo();
         String port = "" + mongoClient.getAddress().getPort();
         System.setProperty("mongodb.port", port);
    }

    @AfterClass
    public static void tearDown() throws Exception {
         if (amqpBrocker != null)
             amqpBrocker.stopBroker();
        try {
            conn.close();
        } catch (Exception e) {
            //We try to close the connection but if
            //the connection is closed we just receive the
            //exception and go on
        }
    }

    @Test
    public void flowTest() {
        try {
            String queueName = rmqHandler.getQueueName();
            Channel channel = conn.createChannel();
            String exchangeName = "ei-poc-4";
            createExchange(exchangeName, queueName);

            ArrayList<String> eventNames = getEventNamesToSend();
            int eventsCount = eventNames.size();
            for(String eventName : eventNames) {
                JsonNode eventJson = parsedJason.get(eventName);
                String event = eventJson.toString();
                channel.basicPublish(exchangeName, queueName,  null, event.getBytes());
            }

            // wait for all events to be processed
            waitForEventsToBeProcessed(eventsCount);
            checkResult();
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    protected void createExchange(final String exchangeName, final String queueName) {
        final CachingConnectionFactory ccf = new CachingConnectionFactory(cf);
        admin = new RabbitAdmin(ccf);
        queue = new Queue(queueName, false);
        admin.declareQueue(queue);
        final TopicExchange exchange = new TopicExchange(exchangeName);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
        ccf.destroy();
    }

    protected ArrayList<String> getEventNamesToSend() {
        ArrayList<String> eventNames = new ArrayList<>();
        return eventNames;
    }

    protected void waitForEventsToBeProcessed(int eventsCount) {
         int processedEvents = 0;
         while (processedEvents < eventsCount) {
             String countStr = System.getProperty("eiffel.intelligence.processedEventsCount");
             String waitingCountStr = System.getProperty("eiffel.intelligence.waitListEventsCount");
             if (waitingCountStr == null)
                 waitingCountStr = "0";
             Properties props = admin.getQueueProperties(queue.getName());
             int messageCount = Integer.parseInt(props.get("QUEUE_MESSAGE_COUNT").toString());
             processedEvents = Integer.parseInt(countStr) - Integer.parseInt(waitingCountStr) - messageCount;
         }
    }

    protected void checkResult() {
         String document = objectHandler.findObjectById("6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");
         String expectedDocument;
        try {
            expectedDocument = FileUtils.readFileToString(new File(inputFilePath));
            ObjectMapper objectmapper = new ObjectMapper();
            JsonNode expectedJson = objectmapper.readTree(expectedDocument);
            JsonNode actualJson = objectmapper.readTree(document);
            String breakString = "breakHere";
            assertEquals(expectedJson.toString().length(), actualJson.toString().length());
        } catch (IOException e) {
            log.info(e.getMessage(),e);
        }

    }
}
