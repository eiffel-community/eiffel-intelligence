/*
   Copyright 2019 Ericsson AB.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.handlers.RMQProperties;


@ContextConfiguration(classes = RMQProperties.class)
@TestPropertySource(locations = "classpath:RmqHandlerTestValues.properties")
@RunWith(SpringJUnit4ClassRunner.class)

public class RmqPropertiesTest {

    private Boolean queueDurable = true;
    private String host = "127.0.0.1";
    private String exchangeName = "ei-poc-4";
    private Integer port = 5672;
    private String domainId = "EN1";
    private String componentName = "eiffelintelligence";
    private String bindingKey = "#";
    private String queueSuffix = "messageQueue";
    private String queueName = "EN1.eiffelintelligence.messageQueue.durable";
    private String waitlistQueueName = "EN1.eiffelintelligence.messageConsumer.durable.waitlist";

    @Autowired
    private RMQProperties rmqProperties;

    @Test
    public void getQueueDurableTest() {
        assertThat(rmqProperties.getQueueDurable(), is(equalTo(queueDurable)));
    }

    @Test
    public void getHostTest() {
        assertThat(rmqProperties.getHost(), is(equalTo(host)));
    }

    @Test
    public void getExchangeNameTest() {
        assertThat(rmqProperties.getExchangeName(), is(equalTo(exchangeName)));
    }

    @Test
    public void getPortTest() {
        assertThat(rmqProperties.getPort(), is(equalTo(port)));
    }

    @Test
    public void getDomainIdTest() {
        assertThat(rmqProperties.getDomainId(), is(equalTo(domainId)));
    }

    @Test
    public void getComponentNameTest() {
        assertThat(rmqProperties.getComponentName(), is(equalTo(componentName)));
    }

    @Test
    public void getRoutingKeyTest() {
        assertThat(rmqProperties.getBindingKey(), is(equalTo(bindingKey)));
    }

    @Test
    public void getQueueSuffixTest() {
        assertThat(rmqProperties.getQueueSuffix(), is(equalTo(queueSuffix)));
    }

    @Test
    public void getQueueNameTest() {
        assertThat(rmqProperties.getQueueName(), is(equalTo(queueName)));
    }

    @Test
    public void getWaitlistQueueNameTest() {
        assertThat(rmqProperties.getWaitlistQueueName(), is(equalTo(waitlistQueueName)));
    }

}
