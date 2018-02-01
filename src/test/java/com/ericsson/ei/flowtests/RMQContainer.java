package com.ericsson.ei.flowtests;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RMQContainer {

    private static Logger log = LoggerFactory.getLogger(RMQContainer.class);

    @Autowired
    RmqHandler rmqHandler;

    @Autowired
    ObjectHandler objectHandler;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Value("${database.name}") String database;
    @Value("${event_object_map.collection.name}") String event_map;


    public static File qpidConfig = null;
    static AMQPBrokerManager amqpBroker;
    protected static MongodForTestsFactory testsFactory;
    static MongoClient mongoClient = null;
    static Queue queue = null;
    static RabbitAdmin admin;
    static ConnectionFactory cf;
    static Connection conn;
    protected static String jsonFileContent;
    protected static JsonNode parsedJson;
    protected static String jsonFilePath = "src/test/resources/test_events.json";
    static protected String inputFilePath = "src/test/resources/AggregatedDocument.json";

    private static boolean started = false;

    private static Lock lock = new ReentrantLock();


    @PostConstruct
    public void initMocks() {
        mongoDBHandler.setMongoClient(mongoClient);
    }

    static void setupEnvironment() throws Exception {
        lock.lock();

        if (started) {
            System.out.println("ALREADY STARTED!!!!!");
        }

        System.setProperty("flow.test", "true");
        System.setProperty("eiffel.intelligence.processedEventsCount", "0");
        System.setProperty("eiffel.intelligence.waitListEventsCount", "0");
        setUpMessageBus();
        setUpEmbeddedMongo();

        started = true;
    }

    static void tearDownEnvironment() throws Exception {
//        if (!started) {
//            System.out.println("ALREADY STOPPED!!!");
//        }
//
//        if (amqpBroker != null) {
//            amqpBroker.stopBroker();
//        }
//
//        try {
//            conn.close();
//        } catch (Exception e) {
//            //We try to close the connection but if
//            //the connection is closed we just receive the
//            //exception and go on
//        }

        started = false;
        lock.unlock();
    }


    private static void setUpMessageBus() throws Exception {
        System.setProperty("rabbitmq.port", "8672");
        System.setProperty("rabbitmq.user", "guest");
        System.setProperty("rabbitmq.password", "guest");
        System.setProperty("waitlist.initialDelayResend", "10");
        System.setProperty("waitlist.fixedRateResend", "10");

        String config = "src/test/resources/configs/qpidConfig.json";
        jsonFileContent = FileUtils.readFileToString(new File(jsonFilePath), "UTF-8");
        ObjectMapper objectmapper = new ObjectMapper();
        parsedJson = objectmapper.readTree(jsonFileContent);
        qpidConfig = new File(config);
        amqpBroker = new AMQPBrokerManager(qpidConfig.getAbsolutePath());
        amqpBroker.startBroker();
        cf = new ConnectionFactory();
        cf.setUsername("guest");
        cf.setPassword("guest");
        cf.setPort(8672);
        cf.setHandshakeTimeout(40000);
        conn = cf.newConnection();
    }

    private static void setUpEmbeddedMongo() throws Exception {
        testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
        mongoClient = testsFactory.newMongo();
        String port = "" + mongoClient.getAddress().getPort();
        System.setProperty("mongodb.port", port);
    }

}
