package com.ericsson.ei.measuringPerformanceTest;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.commons.io.FileUtils;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.junit.AfterClass;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TrafficGeneratedTest {

    private static final int EVENT_PACKAGES = 1;
    private static Logger log = LoggerFactory.getLogger(TrafficGeneratedTest.class);

    public static File qpidConfig = null;
    static TrafficGeneratedTest.AMQPBrokerManager amqpBrocker;
    static Queue queue = null;
    static RabbitAdmin admin;

    static MongodExecutable mongodExecutable = null;

    @Autowired
    RmqHandler rmqHandler;

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
    private static String jsonFilePath = "src/test/resources/test_events_MP.json";
    static private final String inputFilePath = "src/test/resources/aggregated_document_MP.json";

    @BeforeClass
    public static void setup() throws Exception {
        System.setProperty("trafficGenerated.test", "true");
        System.setProperty("eiffel.intelligence.processedEventsCount", "0");
        System.setProperty("eiffel.intelligence.waitListEventsCount", "0");
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
        amqpBrocker = new TrafficGeneratedTest.AMQPBrokerManager(qpidConfig.getAbsolutePath());
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
                .version(Version.V3_4_1)
                .net(new Net(bindIp, port, Network.localhostIsIPv6()))
                .build();


        try {
            mongodExecutable = starter.prepare(mongodConfig);
            MongodProcess mongod = mongodExecutable.start();
        } catch (Exception e) {
            log.debug(e.getMessage(),e);
        }
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
    public void trafficGeneratedTest() {
        try {
            List<String> eventNames = getEventNamesToSend();
            List<String> events = getPreparedEventsToSend(eventNames);
            int eventsCount = eventNames.size() * EVENT_PACKAGES;

            String queueName = rmqHandler.getQueueName();
            String exchange = "ei-poc-4";
            createExchange(exchange, queueName);
            Channel channel = conn.createChannel();

            long timeBefore = System.currentTimeMillis();

            for (String event : events) {
                try {
                    channel.basicPublish(exchange, queueName, null, event.getBytes());
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
            }

            // wait for all events to be processed
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

            long timeAfter = System.currentTimeMillis();
            long diffTime = timeAfter - timeBefore;

            String expectedDocument = FileUtils.readFileToString(new File(inputFilePath));
            ObjectMapper objectmapper = new ObjectMapper();
            JsonNode expectedJson = objectmapper.readTree(expectedDocument);
            for (int i = 0; i < EVENT_PACKAGES; i++) {
                String document = objectHandler.findObjectById("6acc3c87-75e0-4b6d-88f5-b1a5d4".concat(String.format("%06d", i)));
                JsonNode actualJson = objectmapper.readTree(document);
                assertEquals(expectedJson.toString().length(), actualJson.toString().length());
            }
            System.out.println(diffTime / 60000 + "m " + (diffTime / 1000) % 60 + "s " + diffTime % 1000);
        } catch (Exception e) {
            log.debug(e.getMessage(),e);
        }
    }

    /**
     * This method loops through every package of events and changes their ids and targets to unique value.
     * Ids of events that are located in the same package are related.
     * Events are sent to RabbitMQ queue. Deterministic traffic is used.
     * @param eventNames list of events to be sent.
     * @return list of ready to send events.
     */
    private List<String> getPreparedEventsToSend(List<String> eventNames) throws IOException {
        List<String> events = new ArrayList<>();
        String newID;
        for (int i = 0; i < EVENT_PACKAGES; i++) {
            for(String eventName : eventNames) {
                JsonNode eventJson = parsedJason.get(eventName);
                newID = eventJson.at("/meta/id").textValue().substring(0, 30).concat(String.format("%06d", i));;
                ((ObjectNode) eventJson.path("meta")).put("id", newID);
                for (JsonNode link : eventJson.path("links")) {
                    if (link.has("target")) {
                        newID = link.path("target").textValue().substring(0, 30).concat(String.format("%06d", i));
                        ((ObjectNode) link).put("target", newID);
                    }
                }
                events.add(eventJson.toString());
            }
        }
        return events;
    }

    private List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();

        eventNames.add("event_EiffelArtifactCreatedEvent");
        eventNames.add("event_EiffelArtifactPublishedEvent");
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent");
        eventNames.add("event_EiffelTestCaseStartedEvent");
        eventNames.add("event_EiffelTestCaseFinishedEvent");
//        eventNames.add("event_EiffelSourceChangeCreatedEvent");
//        eventNames.add("event_EiffelSourceChangeSubmittedEvent");

        return eventNames;
    }

    private void createExchange(final String exchangeName, final String queueName) {
        final CachingConnectionFactory ccf = new CachingConnectionFactory(cf);
        admin = new RabbitAdmin(ccf);
        queue = new Queue(queueName, false);
        admin.declareQueue(queue);
        final TopicExchange exchange = new TopicExchange(exchangeName);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
        ccf.destroy();
    }

}
