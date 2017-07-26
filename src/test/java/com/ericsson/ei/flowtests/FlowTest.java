package com.ericsson.ei.flowtests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.apache.qpid.server.exchange.TopicExchange;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.handlers.EventHandler;
import com.ericsson.ei.rmq.consumer.RmqConsumer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FlowTest {

        public static File qpidConfig = null;
        static AMQPBrokerManager amqpBrocker;

        @Autowired
        RmqConsumer rmqConsumer;

        @Autowired
        EventHandler eventHandler;

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

    @BeforeClass
    public static void setup() throws Exception {
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

    @AfterClass
    public static void tearDown() throws Exception {
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
    public void test() {
        try {
            Channel channel = conn.createChannel();
            String queueName = rmqConsumer.getQueueName();
            String exchange = "ei-poc-4";

            long count = channel.consumerCount(queueName);
            ArrayList<String> eventNames = getEventNamesToSend();

            for(String eventName : eventNames) {
                JsonNode eventJson = parsedJason.get(eventName);
                String event = eventJson.toString();
                channel.basicPublish(exchange, queueName,  null, event.getBytes());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private ArrayList<String> getEventNamesToSend() {
         ArrayList<String> eventNames = new ArrayList<>();
         eventNames.add("event_EiffelArtifactCreatedEvent_3");
//         eventNames.add("event_EiffelArtifactPublishedEvent_3");
//         eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
         eventNames.add("event_EiffelTestCaseStartedEvent_3");
         eventNames.add("event_EiffelTestCaseFinishedEvent_3");

         return eventNames;
    }
}
