package com.ericsson.ei.rmq.consumer;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.EventHandler;


@Component
public class RmqConsumer {

    @Value("${rabbitmq.queue.durable}") private Boolean queueDurable;
    @Value("${rabbitmq.host}") private String host;
    @Value("${rabbitmq.exchange.name}") private String exchangeName;
    @Value("${rabbitmq.port}") private Integer port;
    @Value("${rabbitmq.tls}") private String tlsVer;
    @Value("${rabbitmq.user}") private String user;
    @Value("${rabbitmq.password}") private String password;
    @Value("${rabbitmq.domainId}") private String domainId;
    @Value("${rabbitmq.componentName}") private String componentName;
    @Value("${rabbitmq.routing.key}") private String routingKey;
    @Value("${rabbitmq.consumerName}") private String consumerName;

//    SimpleMessageListenerContainer container;

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
        CachingConnectionFactory factory = new CachingConnectionFactory(host,port);
//        factory.setUsername("guest");
//        factory.setPassword("guest");
        return factory;
    }

    @Bean
    SimpleMessageListenerContainer bindToQueueForRecentEvents(ConnectionFactory factory, EventHandler eventHandler) {
        String durableName = queueDurable ? "durable" : "transient";
        String queueName = domainId + "." + componentName + "." + consumerName + "." + durableName;
        Queue queue = new Queue(queueName, queueDurable);
        TopicExchange topicExchange = new TopicExchange(exchangeName);
        RabbitAdmin rabbitAdmin = new RabbitAdmin(factory);
        rabbitAdmin.declareExchange(topicExchange);
        rabbitAdmin.declareQueue(queue);
        BindingBuilder.bind(queue).to(topicExchange).with(routingKey);
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(eventHandler, "eventReceived");
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        return container;
    }
}
