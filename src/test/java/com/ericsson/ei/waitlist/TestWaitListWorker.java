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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.Before;
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

import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.handlers.MatchIdRulesHandler;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.handlers.RmqHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.test.utils.TestConfigs;
import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class TestWaitListWorker {

    private static final String EVENT_PATH = "src/test/resources/EiffelArtifactCreatedEvent.json";
    private static final String INPUT_1 = "src/test/resources/testWaitListinput1.json";
    private static final String INPUT_2 = "src/test/resources/testWaitListinput2.json";

    private static File qpidConfig = null;
    private static String jsonFileContent;

    private List<String> list;
    private String message;

    @InjectMocks
    private WaitListWorker waitListWorker;
    @Mock
    private RulesHandler rulesHandler;
    @Mock
    private MatchIdRulesHandler matchId;
    @Mock
    private RmqHandler rmqHandler;
    @Mock
    private WaitListStorageHandler waitListStorageHandler;
    @Mock
    private MongoDBHandler mongoDBHandler;
    @Mock
    private JmesPathInterface jmesPathInterface;
    @Mock
    private JsonNode jsonNode;
    @Mock
    private RulesObject rulesObject;
    @Mock
    private EventToObjectMapHandler eventToObjectMapHandler;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        setupMB();
        list = new ArrayList<>();
        list.add(FileUtils.readFileToString(new File(INPUT_1), "UTF-8"));
        list.add(FileUtils.readFileToString(new File(INPUT_2), "UTF-8"));
        Mockito.when(waitListStorageHandler.getWaitList()).thenReturn(list);
        Mockito.when(rulesHandler.getRulesForEvent(Mockito.anyString())).thenReturn(rulesObject);
        Mockito.when(jmesPathInterface.runRuleOnEvent(Mockito.anyString(), Mockito.anyString())).thenReturn(jsonNode);
    }

    void setupMB() throws Exception {
        TestConfigs.init();
        jsonFileContent = FileUtils.readFileToString(new File(EVENT_PATH), "UTF-8");
    }

    @Test
    public void testRunWithoutMatchObjects() throws JSONException {
        Mockito.when(eventToObjectMapHandler.isEventInEventObjectMap(Mockito.anyString())).thenReturn(false);
        Mockito.when(matchId.fetchObjectsById(Mockito.any(RulesObject.class), Mockito.anyString()))
                .thenReturn(new ArrayList<>());
        try {
            waitListWorker.run();
            assertTrue(true);
        } catch (Exception e) {
            assertFalse(true);
            e.printStackTrace();
        }
    }

    @Test
    public void testRunWithMatchObjects() {
        Mockito.when(eventToObjectMapHandler.isEventInEventObjectMap(Mockito.anyString())).thenReturn(false);
        Mockito.when(matchId.fetchObjectsById(Mockito.any(RulesObject.class), Mockito.anyString())).thenReturn(list);
        try {
            waitListWorker.run();
            assertTrue(true);
        } catch (Exception e) {
            assertFalse(true);
            e.printStackTrace();
        }
    }

    @Test
    public void testRunIfEventExistsInEventObjectMap() {
        Mockito.when(eventToObjectMapHandler.isEventInEventObjectMap(Mockito.anyString())).thenReturn(true);
        try {
            waitListWorker.run();
            assertTrue(true);
        } catch (Exception e) {
            assertFalse(true);
            e.printStackTrace();
        }
    }

    @Test
    public void testPublishAndReceiveEvent() {
        try {
            Channel channel = TestConfigs.getConn().createChannel();
            String queueName = "er001-eiffelxxx.eiffelintelligence.messageConsumer.durable";
            String exchange = "ei-poc-4";
            createExchange(exchange, queueName);
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {
                    message = new String(body, "UTF-8");
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

    private void createExchange(final String exchangeName, final String queueName) {
        final CachingConnectionFactory ccf = new CachingConnectionFactory(TestConfigs.getCf());
        RabbitAdmin admin = new RabbitAdmin(ccf);
        Queue queue = new Queue(queueName, false);
        admin.declareQueue(queue);
        final TopicExchange exchange = new TopicExchange(exchangeName);
        admin.declareExchange(exchange);
        admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("#"));
        ccf.destroy();
    }
}
