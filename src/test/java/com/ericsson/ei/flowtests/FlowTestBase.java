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

import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.handlers.UpStreamEventsHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.apache.commons.io.FileUtils;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FlowTestBase {

    private static Logger log = LoggerFactory.getLogger(FlowTest.class);

    @Autowired
    RmqHandler rmqHandler;

    @Autowired
    ObjectHandler objectHandler;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Value("${database.name}") private String database;
    @Value("${event_object_map.collection.name}") private String event_map;

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

    public static void setJsonFilePath(String jsonFilePath) {
        FlowTestBase.jsonFilePath = jsonFilePath;
    }

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
        System.setProperty("waitlist.initialDelayResend", "5000");
        System.setProperty("waitlist.fixedRateResend", "1000");

        String config = "src/test/resources/configs/qpidConfig.json";
        ObjectMapper objectmapper = new ObjectMapper();
        qpidConfig = new File(config);
        amqpBrocker = new AMQPBrokerManager(qpidConfig.getAbsolutePath());
        amqpBrocker.startBroker();
        cf = new ConnectionFactory();
        cf.setUsername("guest");
        cf.setPassword("guest");
        cf.setPort(8672);
        cf.setHandshakeTimeout(600000);
        cf.setConnectionTimeout(600000);
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
            testParametersChange();
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

    protected void testParametersChange() throws IOException {
        setSpecificTestCaseParameters();
        jsonFileContent = FileUtils.readFileToString(new File(jsonFilePath));
        ObjectMapper objectmapper = new ObjectMapper();
        parsedJason = objectmapper.readTree(jsonFileContent);
    }

    protected void setSpecificTestCaseParameters(){
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

    // count documents that were processed
    private long countProcessedEvents(String database, String collection){
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection table = db.getCollection(collection);
        long countedDocuments = table.count();
        return countedDocuments;
    }

    protected void waitForEventsToBeProcessed(int eventsCount) {
        // wait for all events to be processed
        long processedEvents = 0;
        while (processedEvents < eventsCount) {
            processedEvents = countProcessedEvents(database, event_map);
            log.info("Have gotten: " + processedEvents + " out of: " + eventsCount);
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (Exception e) {
                log.info(e.getMessage(),e);
            }
        }
    }

    protected void checkResult() throws JSONException {
         String document = objectHandler.findObjectById("6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");
         String expectedDocument;
        try {
            expectedDocument = FileUtils.readFileToString(new File(inputFilePath), "UTF-8");
            ObjectMapper objectmapper = new ObjectMapper();
            JsonNode expectedJson = objectmapper.readTree(expectedDocument);
            JsonNode actualJson = objectmapper.readTree(document);
            String breakString = "breakHere";
            System.out.println(actualJson);
            JSONAssert.assertEquals(expectedJson.toString(), actualJson.toString(), false);
        } catch (IOException e) {
            log.info(e.getMessage(),e);
        }

    }
}
