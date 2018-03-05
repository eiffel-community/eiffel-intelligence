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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

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
import com.sun.jna.platform.win32.WinNT.TOKEN_GROUPS;

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
        list.add(FileUtils.readFileToString(new File(input1), "UTF-8"));
        list.add(FileUtils.readFileToString(new File(input2), "UTF-8"));
//        Mockito.when(mongoDBHandler.dropDocument(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
//                .thenReturn(true);
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
        jsonFileContent = FileUtils.readFileToString(new File(eventPath), "UTF-8");
        qpidConfig = new File(config);
        amqpBrocker = new AMQPBrokerManager(qpidConfig.getAbsolutePath());
        amqpBrocker.startBroker();
        cf = new ConnectionFactory();
        cf.setUsername("guest");
        cf.setPassword("guest");
        cf.setPort(8672);
        cf.setHandshakeTimeout(60000);
        cf.setConnectionTimeout(60000);
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

//    TO DO fix this test
//    @Test
//    public void testDropDocumentFromWaitList() {
//        try {
//            String event = FileUtils.readFileToString(new File(eventPath), "UTF-8");
//            String condition = "{Event:" + JSON.parse(event).toString() + "}";
//            assertTrue(waitListStorageHandler.dropDocumentFromWaitList(condition));
//        } catch (Exception e) {
//            assertFalse(true);
//            System.out.println("error occured while deleting document from waitlist");
//        }
//    }

    @Test
    public void testPublishandReceiveEvent() {
        try {
            Channel channel = conn.createChannel();
            String queueName = "er001-eiffelxxx.eiffelintelligence.messageConsumer.durable";
            String exchange = "ei-poc-4";
            createExchange(exchange, queueName);
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
            e.printStackTrace();
            assertFalse(true);
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


    private void createExchange(final String exchangeName, final String queueName) {
        final CachingConnectionFactory ccf = new CachingConnectionFactory(cf);
        RabbitAdmin admin = new RabbitAdmin(ccf);
        Queue queue = new Queue(queueName, false);
        admin.declareQueue(queue);
        final TopicExchange exchange = new TopicExchange(exchangeName);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
        ccf.destroy();
    }
}
