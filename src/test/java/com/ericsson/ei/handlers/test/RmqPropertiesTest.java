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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.handlers.RMQProperties;


@ContextConfiguration(classes = RMQProperties.class)
@TestPropertySource(locations = "RmqHandlerTestValues.properties")
@RunWith(SpringJUnit4ClassRunner.class)

public class RmqPropertiesTest {

    private Boolean queueDurable = true;
    private String host = "127.0.0.1";
    private String exchangeName = "ei-poc-4";
    private Integer port = 5672;
    private String domainId = "EN1";
    private String componentName = "eiffelintelligence";
    private String bindingKey = "#";
    private String consumerName = "messageConsumer";
    private String queueName = "EN1.eiffelintelligence.messageConsumer.durable";
    private String waitlistQueueName = "EN1.eiffelintelligence.messageConsumer.durable.waitlist";

    @Autowired
    private RMQProperties rmqProperties;

    @Mock
    private ConnectionFactory factory;

    @Test
    public void getQueueDurableTest() {
        assertTrue(rmqProperties.getQueueDurable().equals(queueDurable));
    }

    @Test
    public void getHostTest() {
        assertTrue(rmqProperties.getHost().equals(host));
    }

    @Test
    public void getExchangeNameTest() {
        assertTrue(rmqProperties.getExchangeName().equals(exchangeName));
    }

    @Test
    public void getPortTest() {
        assertTrue(rmqProperties.getPort().equals(port));
    }

    @Test
    public void getDomainIdTest() {
        assertTrue(rmqProperties.getDomainId().equals(domainId));
    }

    @Test
    public void getComponentNameTest() {
        assertTrue(rmqProperties.getComponentName().equals(componentName));
    }

    @Test
    public void getRoutingKeyTest() {
        assertTrue(rmqProperties.getBindingKey().equals(bindingKey));
    }

    @Test
    public void getConsumerNameTest() {
        assertTrue(rmqProperties.getConsumerName().equals(consumerName));
    }

    @Test
    public void getQueueNameTest() {
        assertTrue(rmqProperties.getQueueName().equals(queueName));
    }

    @Test
    public void getWaitlistQueueNameTest() {
        assertTrue(rmqProperties.getWaitlistQueueName().equals(waitlistQueueName));
    }

}
