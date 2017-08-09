package com.ericsson.ei.flowtests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.handlers.EventHandler;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.rmq.consumer.RmqConsumer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FlowTest {

    private static Logger log = LoggerFactory.getLogger(FlowTest.class);

    public static File qpidConfig = null;
    static AMQPBrokerManager amqpBrocker;
    static MongodExecutable mongodExecutable = null;

    @Autowired
    RmqConsumer rmqConsumer;

    @Autowired
    ObjectHandler objectHandler;

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
    private static String jsonFileContent;
    private static JsonNode parsedJason;
    private static String jsonFilePath = "src/test/resources/test_events.json";
    static private final String inputFilePath = "src/test/resources/AggregatedDocument.json";

    @BeforeClass
    public static void setup() throws Exception {
        System.setProperty("flow.test", "true");
        System.setProperty("eiffel.intelligence.processedEventsCount", "0");
        setUpMessageBus();
        setUpEmbeddedMongo();
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
        int port = 12349;
        System.setProperty("mongodb.port", ""+port);

        MongodStarter starter = MongodStarter.getDefaultInstance();

        String bindIp = "localhost";

        IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(bindIp, port, Network.localhostIsIPv6()))
            .build();


        try {
            mongodExecutable = starter.prepare(mongodConfig);
            MongodProcess mongod = mongodExecutable.start();
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        amqpBrocker.stopBroker();

        if (mongodExecutable != null)
            mongodExecutable.stop();

        try {
            conn.close();
        } catch (Exception e) {
            //We try to close the connection but if
            //the connection is closed we just receive the
            //exception and go on
        }
    }

    @Test
    public void test() {
        try {
            String queueName = rmqConsumer.getQueueName();
            String exchange = "ei-poc-4";
            createExchange(exchange, queueName);
            Channel channel = conn.createChannel();


            ArrayList<String> eventNames = getEventNamesToSend();
            int eventsCount = eventNames.size();
            for(String eventName : eventNames) {
                JsonNode eventJson = parsedJason.get(eventName);
                String event = eventJson.toString();
                channel.basicPublish(exchange, queueName,  null, event.getBytes());
            }

            // wait for all events to be processed
            int processedEvents = 0;
            while (processedEvents < eventsCount) {
                String countStr = System.getProperty("eiffel.intelligence.processedEventsCount");
                processedEvents = Integer.parseInt(countStr);
            }

            String document = objectHandler.findObjectById("6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");
            String expectedDocument = FileUtils.readFileToString(new File(inputFilePath));
            ObjectMapper objectmapper = new ObjectMapper();
            JsonNode expectedJson = objectmapper.readTree(expectedDocument);
            JsonNode actualJson = objectmapper.readTree(document);
            String breakString = "breakHere";
            assertEquals(expectedJson, actualJson);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    private ArrayList<String> getEventNamesToSend() {
         ArrayList<String> eventNames = new ArrayList<>();
         eventNames.add("event_EiffelArtifactCreatedEvent_3");
         eventNames.add("event_EiffelArtifactPublishedEvent_3");
         eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
         eventNames.add("event_EiffelTestCaseStartedEvent_3");
         eventNames.add("event_EiffelTestCaseFinishedEvent_3");

         return eventNames;
    }

    private void createExchange(final String exchangeName, final String queueName) {
        final CachingConnectionFactory cf = new CachingConnectionFactory(AMQPBrokerManager.PORT);
        final RabbitAdmin admin = new RabbitAdmin(cf);
        final Queue queue = new Queue(queueName, false);
        admin.declareQueue(queue);
        final TopicExchange exchange = new TopicExchange(exchangeName);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
        cf.destroy();
    }
}
