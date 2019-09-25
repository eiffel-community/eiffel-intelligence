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

import org.apache.commons.lang3.StringUtils;
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

import lombok.Getter;
import lombok.Setter;

@Component
public class RMQHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMQHandler.class);

    @Value("${threads.maxPoolSize}")
    private int maxThreads;

    @Setter
    private RabbitTemplate rabbitTemplate;

    @Getter
    private CachingConnectionFactory cachingConnectionFactory;

    @Getter
    private SimpleMessageListenerContainer container;

    @Autowired
    private RMQConnectionListener rmqConnectionListener = new RMQConnectionListener();

    @Getter
    @Autowired
    private RMQProperties rmqProperties;

    @Bean
    public ConnectionFactory connectionFactory() {
        cachingConnectionFactory = new CachingConnectionFactory(rmqProperties.getHost(), rmqProperties.getPort());
        cachingConnectionFactory.addConnectionListener(rmqConnectionListener);

        if (isRMQCredentialsSet()) {
            cachingConnectionFactory.setUsername(rmqProperties.getUser());
            cachingConnectionFactory.setPassword(rmqProperties.getPassword());
        }

        if (!StringUtils.isEmpty(rmqProperties.getTlsVersion())) {
            try {
                LOGGER.debug("Using SSL/TLS version {} connection to RabbitMQ.", rmqProperties.getTlsVersion());
                cachingConnectionFactory.getRabbitConnectionFactory().useSslProtocol(rmqProperties.getTlsVersion());
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
    public SimpleMessageListenerContainer bindToQueueForRecentEvents(
            ConnectionFactory springConnectionFactory,
            EventHandler eventHandler) {
        String queueName = rmqProperties.getQueueName();
        MessageListenerAdapter listenerAdapter = new EIMessageListenerAdapter(eventHandler);
        container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(springConnectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setPrefetchCount(maxThreads);
        return container;
    }

    @Bean
    public RabbitTemplate rabbitMqTemplate() {
        if (rabbitTemplate == null) {
            if (cachingConnectionFactory != null) {
                rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
            } else {
                rabbitTemplate = new RabbitTemplate(connectionFactory());
            }

            rabbitTemplate.setExchange(rmqProperties.getExchangeName());
            rabbitTemplate.setRoutingKey(rmqProperties.getBindingKey());
            rabbitTemplate.setQueue(rmqProperties.getQueueName());
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

    @Bean
    protected Queue queue() {
        return new Queue(rmqProperties.getQueueName(), true);
    }

    @Bean
    protected TopicExchange exchange() {
        return new TopicExchange(rmqProperties.getExchangeName());
    }

    @Bean
    protected Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(rmqProperties.getBindingKey());
    }

    private boolean isRMQCredentialsSet() {
        return !StringUtils.isEmpty(rmqProperties.getUser()) && !StringUtils.isEmpty(rmqProperties.getPassword());
    }
}
