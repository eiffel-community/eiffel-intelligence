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
package com.ericsson.ei.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.ericsson.ei.listeners.EIMessageListenerAdapter;
import com.ericsson.ei.listeners.RMQConnectionListener;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Component
public class RMQHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMQHandler.class);

    @Getter
    @Setter
    @Value("${rabbitmq.queue.durable}")
    private Boolean queueDurable;

    @Getter
    @Setter
    @Value("${rabbitmq.host}")
    private String host;

    @Getter
    @Setter
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Getter
    @Setter
    @Value("${rabbitmq.port}")
    private Integer port;

    @Getter
    @Setter
    @Value("${rabbitmq.tlsVersion}")
    private String tlsVersion;

    @Getter
    @Setter
    @JsonIgnore
    @Value("${rabbitmq.user}")
    private String user;

    @Getter
    @Setter
    @JsonIgnore
    @Value("${rabbitmq.password}")
    private String password;

    @Getter
    @Setter
    @Value("${rabbitmq.domainId}")
    private String domainId;

    @Getter
    @Setter
    @Value("${rabbitmq.componentName}")
    private String componentName;

    @Getter
    @Setter
    @Value("${rabbitmq.waitlist.queue.suffix}")
    private String waitlistSufix;

    @Getter
    @Setter
    @Value("${rabbitmq.binding.key}")
    private String bindingKey;

    @Getter
    @Setter
    @Value("${rabbitmq.consumerName}")
    private String consumerName;

    @Value("${threads.maxPoolSize}")
    private int maxThreads;

    @Setter
    @JsonIgnore
    private RabbitTemplate rabbitTemplate;

    @Getter
    @JsonIgnore
    private CachingConnectionFactory cachingConnectionFactory;

    @Getter
    @JsonIgnore
    private SimpleMessageListenerContainer container;

    @Autowired
    @JsonIgnore
    private RMQConnectionListener rmqConnectionListener = new RMQConnectionListener();

    @Bean
    public ConnectionFactory connectionFactory() {
        cachingConnectionFactory = new CachingConnectionFactory(host, port);
        cachingConnectionFactory.addConnectionListener(rmqConnectionListener);

        if (user != null && user.length() != 0 && password != null && password.length() != 0) {
            cachingConnectionFactory.setUsername(user);
            cachingConnectionFactory.setPassword(password);
        }

        if (tlsVersion != null && !tlsVersion.isEmpty()) {
            try {
                LOGGER.debug("Using SSL/TLS version {} connection to RabbitMQ.", tlsVersion);
                cachingConnectionFactory.getRabbitConnectionFactory().useSslProtocol(tlsVersion);
            } catch (Exception e) {
                LOGGER.error("Failed to set SSL/TLS version.", e);
            }
        }

        cachingConnectionFactory.setPublisherConfirms(true);
        cachingConnectionFactory.setPublisherReturns(true);

        // This will disable connectionFactories auto recovery and use Spring AMQP auto
        // recovery
        cachingConnectionFactory.getRabbitConnectionFactory().setAutomaticRecoveryEnabled(false);

        return cachingConnectionFactory;
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
        return BindingBuilder.bind(queue).to(exchange).with(bindingKey);
    }

    @Bean
    public SimpleMessageListenerContainer bindToQueueForRecentEvents(
            ConnectionFactory springConnectionFactory,
            EventHandler eventHandler) {
        String queueName = getQueueName();
        MessageListenerAdapter listenerAdapter = new EIMessageListenerAdapter(eventHandler);
        container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(springConnectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setPrefetchCount(maxThreads);
        return container;
    }

    public String getQueueName() {
        String durableName = queueDurable ? "durable" : "transient";
        return domainId + "." + componentName + "." + consumerName + "." + durableName;
    }

    public String getWaitlistQueueName() {

        String durableName = queueDurable ? "durable" : "transient";
        return domainId + "." + componentName + "." + consumerName + "." + durableName + "."
                + waitlistSufix;
    }

    @Bean
    public RabbitTemplate rabbitMqTemplate() {
        if (rabbitTemplate == null) {
            if (cachingConnectionFactory != null) {
                rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
            } else {
                rabbitTemplate = new RabbitTemplate(connectionFactory());
            }

            rabbitTemplate.setExchange(exchangeName);
            rabbitTemplate.setRoutingKey(bindingKey);
            rabbitTemplate.setQueue(getQueueName());
            rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
                @Override
                public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                    LOGGER.info("Received confirm with result : {}", ack);
                }
            });
        }
        return rabbitTemplate;
    }

    public void publishObjectToWaitlistQueue(String message) {
        LOGGER.debug("Publishing message to message bus...");
        rabbitTemplate.convertAndSend(message);
    }

    public void close() {
        try {
            container.destroy();
            cachingConnectionFactory.destroy();
        } catch (Exception e) {
            LOGGER.error("Exception occurred while closing connections.", e);
        }
    }
}
