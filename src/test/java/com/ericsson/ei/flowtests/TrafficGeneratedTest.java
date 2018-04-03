/*
   Copyright 2017 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.ei.flowtests;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.waitlist.WaitListStorageHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TrafficGeneratedTest {

    private static final int EVENT_PACKAGES = 10;
    private static Logger log = LoggerFactory.getLogger(TrafficGeneratedTest.class);

    public static File qpidConfig = null;
    private static MongodForTestsFactory testsFactory;
    static TrafficGeneratedTest.AMQPBrokerManager amqpBrocker;
    static Queue queue = null;
    static RabbitAdmin admin;

    static MongoClient mongoClient = null;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private WaitListStorageHandler waitlist;

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
    private static JsonNode parsedJson;
    private static String jsonFilePath = "src/test/resources/test_events_MP.json";
    static private final String inputFilePath = "src/test/resources/aggregated_document_MP.json";

    @Value("${database.name}") private String database;
    @Value("${event_object_map.collection.name}") private String event_map;

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
        waitlist.setMongoDbHandler(mongoDBHandler);
    }

    public static void setUpMessageBus() throws Exception {
        System.setProperty("rabbitmq.port", "8672");
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");


        String config = "src/test/resources/configs/qpidConfig.json";
        jsonFileContent = FileUtils.readFileToString(new File(jsonFilePath), "UTF-8");
        ObjectMapper objectmapper = new ObjectMapper();
        parsedJson = objectmapper.readTree(jsonFileContent);
        qpidConfig = new File(config);
        amqpBrocker = new TrafficGeneratedTest.AMQPBrokerManager(qpidConfig.getAbsolutePath());
        amqpBrocker.startBroker();
        cf = new ConnectionFactory();
        cf.setUsername("guest");
        cf.setPassword("guest");
        cf.setHandshakeTimeout(20000);
        cf.setPort(8672);
        conn = cf.newConnection();
    }

    public static void setUpEmbeddedMongo() throws Exception {
        int port = 12349;
        System.setProperty("mongodb.port", ""+port);

        testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
        mongoClient = testsFactory.newMongo();
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
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {}
            }

            // wait for all events to be processed
            long processedEvents = 0;
            MongoCollection eventMap = mongoClient.getDatabase(database).getCollection(event_map);
            while (processedEvents < eventsCount) {
                processedEvents = eventMap.count();
            }

            TimeUnit.MILLISECONDS.sleep(1000);

            long timeAfter = System.currentTimeMillis();
            long diffTime = timeAfter - timeBefore;

            String expectedDocument = FileUtils.readFileToString(new File(inputFilePath), "UTF-8");
            ObjectMapper objectmapper = new ObjectMapper();
            JsonNode expectedJson = objectmapper.readTree(expectedDocument);
            for (int i = 0; i < EVENT_PACKAGES; i++) {
                String document = objectHandler.findObjectById("6acc3c87-75e0-4b6d-88f5-b1a5d4".concat(String.format("%06d", i)));
                JsonNode actualJson = objectmapper.readTree(document);
                assertEquals(expectedJson.toString().length(), actualJson.toString().length());
            }

            String time = "" + diffTime / 60000 + "m " + (diffTime / 1000) % 60 + "s " + diffTime % 1000;
            System.out.println("Time of execution: " + time);
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
    	ObjectMapper mapper = new ObjectMapper();
        List<String> events = new ArrayList<>();
        String newID;
        for (int i = 0; i < EVENT_PACKAGES; i++) {
            for(String eventName : eventNames) {
                JsonNode eventJson = parsedJson.get(eventName);
                newID = eventJson.at("/meta/id").textValue().substring(0, 30).concat(String.format("%06d", i));;
                ((ObjectNode) eventJson.path("meta")).set("id", mapper.readValue(newID, JsonNode.class));
                for (JsonNode link : eventJson.path("links")) {
                    if (link.has("target")) {
                        newID = link.path("target").textValue().substring(0, 30).concat(String.format("%06d", i));
                        ((ObjectNode) link).set("target", mapper.readValue(newID, JsonNode.class));
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
