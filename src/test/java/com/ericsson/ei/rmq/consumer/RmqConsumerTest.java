package com.ericsson.ei.rmq.consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.handlers.EventHandler;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RmqConsumerTest {

    @Autowired
    EventHandler eventHandler;

    private Boolean queueDurable = true;
    private String host = "127.0.0.1";
    private String exchangeName = "ei-poc-4";
    private Integer port = 5672;
    private String domainId = "EN1";
    private String componentName = "eiffelintelligence";
    private String routingKey = "#";
    private String consumerName = "messageConsumer";

    @InjectMocks
    RmqConsumer rmqConsumer;

    @Mock ConnectionFactory factory;

    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initProperties();
    }

    public void initProperties()
    {
        rmqConsumer.setQueueDurable(queueDurable);
        rmqConsumer.setHost(host);
        rmqConsumer.setExchangeName(exchangeName);
        rmqConsumer.setPort(port);
        rmqConsumer.setDomainId(domainId);
        rmqConsumer.setComponentName(componentName);
        rmqConsumer.setRoutingKey(routingKey);
        rmqConsumer.setConsumerName(consumerName);
    }

    @Test
    public void getQueueDurableTest() {
        assertTrue(rmqConsumer.getQueueDurable().equals(queueDurable));
    }

    @Test
    public void getHostTest() {
        assertTrue(rmqConsumer.getHost().equals(host));
    }

    @Test
    public void getExchangeNameTest() {
        assertTrue(rmqConsumer.getExchangeName().equals(exchangeName));
    }

    @Test
    public void getPortTest() {
        assertTrue(rmqConsumer.getPort().equals(port));
    }

    @Test
    public void getDomainIdTest() {
        assertTrue(rmqConsumer.getDomainId().equals(domainId));
    }

    @Test
    public void getComponentNameTest() {
        assertTrue(rmqConsumer.getComponentName().equals(componentName));
    }

    @Test
    public void getRoutingKeyTest() {
        assertTrue(rmqConsumer.getRoutingKey().equals(routingKey));
    }

    @Test
    public void getConsumerNameTest() {
        assertTrue(rmqConsumer.getConsumerName().equals(consumerName));
    }

    @Test
    public void testMessageBusConnection() {
        factory = rmqConsumer.connectionFactory();
        assertNotNull(factory);
    }
}