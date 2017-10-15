package com.ericsson.ei.rmqhandler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.EventHandler;
import com.rabbitmq.client.Channel;

@Component
public class RmqHandler {

    @Value("${rabbitmq.queue.durable}")
    private Boolean queueDurable;
    @Value("${rabbitmq.host}")
    private String host;
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${rabbitmq.port}")
    private Integer port;
    @Value("${rabbitmq.tls}")
    private String tlsVer;
    @Value("${rabbitmq.user}")
    private String user;
    @Value("${rabbitmq.password}")
    private String password;
    @Value("${rabbitmq.domainId}")
    private String domainId;
    @Value("${rabbitmq.componentName}")
    private String componentName;
    @Value("${rabbitmq.waitlist.queue.suffix}")
    private String waitlistSufix;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;
    @Value("${rabbitmq.consumerName}")
    private String consumerName;
    private RabbitTemplate rabbitTemplate;
    private CachingConnectionFactory factory;
    private SimpleMessageListenerContainer container;
    private SimpleMessageListenerContainer waitlistContainer;
    static Logger log = (Logger) LoggerFactory.getLogger(RmqHandler.class);

    public Boolean getQueueDurable() {
        return queueDurable;
    }

    public void setQueueDurable(Boolean queueDurable) {
        this.queueDurable = queueDurable;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getTlsVer() {
        return tlsVer;
    }

    public void setTlsVer(String tlsVer) {
        this.tlsVer = tlsVer;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    @Bean
    ConnectionFactory connectionFactory() {
        factory = new CachingConnectionFactory(host, port);
        factory.setPublisherConfirms(true);
        factory.setPublisherReturns(true);
        if(user != null && user.length() !=0 && password != null && password.length() !=0) {
            factory.setUsername(user);
            factory.setPassword(password);
        }
        return factory;
    }

    @Bean
    Queue queue() {
        return new Queue(getQueueName(), true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    @Bean
    SimpleMessageListenerContainer bindToQueueForRecentEvents(ConnectionFactory factory, EventHandler eventHandler) {
        String queueName = getQueueName();
        MessageListenerAdapter listenerAdapter = new EIMessageListenerAdapter(eventHandler);
        container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return container;
    }

    public String getQueueName() {
        String durableName = queueDurable ? "durable" : "transient";
        return domainId + "." + componentName + "." + consumerName + "." + durableName;
    }

    public String getWaitlistQueueName() {
        String durableName = queueDurable ? "durable" : "transient";
        return domainId + "." + componentName + "." + consumerName + "." + durableName + "." + waitlistSufix;
    }

    @Bean
    public RabbitTemplate rabbitMqTemplate() {
        if (rabbitTemplate == null) {
            if (factory != null) {
                rabbitTemplate = new RabbitTemplate(factory);
            } else {
                rabbitTemplate = new RabbitTemplate(connectionFactory());
            }

            rabbitTemplate.setExchange(exchangeName);
            rabbitTemplate.setRoutingKey(routingKey);
            rabbitTemplate.setQueue(getQueueName());
            rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
                @Override
                public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                    log.info("Received confirm with result : {}", ack);
                }
            });
        }
        return rabbitTemplate;
    }

    public void publishObjectToWaitlistQueue(String message) {
        log.info("publishing message to message bus...");
        rabbitMqTemplate().convertAndSend(message);
    }

    public void close() {
        try {
            waitlistContainer.destroy();
            container.destroy();
            factory.destroy();
        } catch (Exception e) {
            log.info("exception occured while closing connections");
            log.info(e.getMessage(),e);
        }
    }
}
