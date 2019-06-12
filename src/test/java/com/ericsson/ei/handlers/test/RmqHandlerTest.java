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
package com.ericsson.ei.handlers.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;

import com.ericsson.ei.App;
import com.ericsson.ei.handlers.RmqHandler;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        App.class, 
        EmbeddedMongoAutoConfiguration.class // <--- Don't forget THIS
    })
public class RmqHandlerTest {

    private Boolean queueDurable = true;
    private String host = "127.0.0.1";
    private String exchangeName = "ei-poc-4";
    private Integer port = 5672;
    private String domainId = "EN1";
    private String componentName = "eiffelintelligence";
    private String bindingKey = "#";
    private String consumerName = "messageConsumer";

    @InjectMocks
    private RmqHandler rmqHandler;

    @Mock
    private ConnectionFactory factory;
    
    @BeforeClass
    public static void init() {
        int port = SocketUtils.findAvailableTcpPort();
        System.setProperty("spring.data.mongodb.port", "" + port);
    }

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
        initProperties();
    }

    public void initProperties()
    {
        rmqHandler.setQueueDurable(queueDurable);
        rmqHandler.setHost(host);
        rmqHandler.setExchangeName(exchangeName);
        rmqHandler.setPort(port);
        rmqHandler.setDomainId(domainId);
        rmqHandler.setComponentName(componentName);
        rmqHandler.setBindingKey(bindingKey);
        rmqHandler.setConsumerName(consumerName);
    }

    @Test
    public void getQueueDurableTest() {
        assertTrue(rmqHandler.getQueueDurable().equals(queueDurable));
    }

    @Test
    public void getHostTest() {
        assertTrue(rmqHandler.getHost().equals(host));
    }

    @Test
    public void getExchangeNameTest() {
        assertTrue(rmqHandler.getExchangeName().equals(exchangeName));
    }

    @Test
    public void getPortTest() {
        assertTrue(rmqHandler.getPort().equals(port));
    }

    @Test
    public void getDomainIdTest() {
        assertTrue(rmqHandler.getDomainId().equals(domainId));
    }

    @Test
    public void getComponentNameTest() {
        assertTrue(rmqHandler.getComponentName().equals(componentName));
    }

    @Test
    public void getRoutingKeyTest() {
        assertTrue(rmqHandler.getBindingKey().equals(bindingKey));
    }

    @Test
    public void getConsumerNameTest() {
        assertTrue(rmqHandler.getConsumerName().equals(consumerName));
    }

    @Test
    public void testMessageBusConnection() {
        factory = rmqHandler.connectionFactory();
        assertNotNull(factory);
    }
}