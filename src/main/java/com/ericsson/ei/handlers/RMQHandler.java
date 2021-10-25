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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.ericsson.ei.listeners.EIMessageListenerAdapter;
import com.ericsson.ei.listeners.RMQConnectionListener;
import com.ericsson.ei.mongo.MongoCondition;
import com.ericsson.ei.mongo.MongoConstants;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.BasicDBObject;

import lombok.Getter;
import lombok.Setter;

@Component
public class RMQHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMQHandler.class);
    
    private static final String WAITLIST_BINDING_KEY = "eiffel-intelligence.waitlist";

    @Value("${threads.max.pool.size}")
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

    @Getter
    @Setter
    @Value("${spring.data.mongodb.database}")
    private String dataBaseName;

    @Getter
    @Setter
    @Value("${bindingkeys.collection.name}")
    private String collectionName;

    @Setter
    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Getter
    @JsonIgnore
    private AmqpAdmin amqpAdmin;

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
        MessageListenerAdapter listenerAdapter = new EIMessageListenerAdapter(eventHandler);
        container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(springConnectionFactory);
        container.setQueueNames(rmqProperties.getQueueName(), rmqProperties.getWaitlistQueueName());
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

            rabbitTemplate.setDefaultReceiveQueue(rmqProperties.getWaitlistQueueName());
            rabbitTemplate.setExchange(rmqProperties.getExchangeName());
            rabbitTemplate.setRoutingKey(WAITLIST_BINDING_KEY);
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
    protected Queue externalQueue() {
        return new Queue(rmqProperties.getQueueName(), true);
    }

    @Bean
    protected Queue internalQueue() {
        return new Queue(rmqProperties.getWaitlistQueueName(), true);
    }

    @Bean
    protected TopicExchange exchange() {
        return new TopicExchange(rmqProperties.getExchangeName());
    }

    @Bean
    protected Binding binding() {
        return BindingBuilder.bind(internalQueue()).to(exchange()).with(WAITLIST_BINDING_KEY);
    }

    @Bean
    public List<Binding> bindings() {
        String[] bindingKeysArray = splitBindingKeys(rmqProperties.getBindingKeys());
        List<Binding> bindingList = new ArrayList<Binding>();
        for (String bindingKey : bindingKeysArray) {
            bindingList.add(BindingBuilder.bind(externalQueue()).to(exchange()).with(bindingKey));
        }
        deleteBindings(bindingKeysArray,bindingList);
        return bindingList;
    }

    private boolean isRMQCredentialsSet() {
        return !StringUtils.isEmpty(rmqProperties.getUser()) && !StringUtils.isEmpty(rmqProperties.getPassword());
    }

    private String[] splitBindingKeys(String bindingKeys) {
        String bindingKeysWithoutWhitespace = bindingKeys.replaceAll("\\s+", "");
        return bindingKeysWithoutWhitespace.split(",");
    }

    /**
     * This method is used to delete the bindings in rabbitMQ.
     * By comparing the binding keys used in the properties and binding keys stored in mongoDB.
     * newBindingKeysArray is the only binding keys array.
     * AMQPBindingObjectList is entire list of bindings.
     * Binding key which is not present in the current AMQPBindingObjectList gets deleted and removed from mongoDB.
     * @return
     */
    private void deleteBindings(String[] newBindingKeysArray, List<Binding> AMQPBindingObjectList) {
        // Creating BindingKeys Collection in mongoDB
        ArrayList<String> allDocuments = mongoDBHandler.getAllDocuments(dataBaseName, collectionName);
        ArrayList<String> existingBindingsData = new ArrayList<String>();
        if (!allDocuments.isEmpty()) {
            for (String bindings : allDocuments) {
                JSONObject bindingObj = new JSONObject(bindings);
                final String mongoDbBindingKey = bindingObj.getString("bindingKeys");
                MongoCondition condition = MongoCondition.bindingKeyCondition(mongoDbBindingKey);
                if (!Arrays.asList(newBindingKeysArray).contains(mongoDbBindingKey)) {
                    String destinationDB = bindingObj.getString("destination");
                    String exchangeDB = bindingObj.getString("exchange");
                    // Binding the old binding key and removing from queue
                    Binding b = new Binding(destinationDB, DestinationType.QUEUE, exchangeDB, mongoDbBindingKey, null);
                    amqpAdmin = new RabbitAdmin(connectionFactory());
                    amqpAdmin.removeBinding(b);
                    // Removing binding document from mongoDB
                    mongoDBHandler.dropDocument(dataBaseName, collectionName, condition);
                } else {
                    // storing the existing key into an array.
                    existingBindingsData.add(mongoDbBindingKey);
                }
            }
        }
        // to store the binding keys used for rabbitMQ, in mongo db.
        storeNewBindingKeys(existingBindingsData, AMQPBindingObjectList);
    }

	/**
     * This method is used to store the binding keys used for rabbitMQ, in mongoDB.
     * @return
     */
    private void storeNewBindingKeys(ArrayList<String> existingBindingsData, List<Binding> AMQPBindingObjectList){
    // comparing with the stored key and adding the new binding key into the mongoDB.
       for(final Binding bindingKey:AMQPBindingObjectList){
            if(existingBindingsData.contains(bindingKey.getRoutingKey())){
                LOGGER.info("Binding already present in mongoDB");
            }else{
                    BasicDBObject document = new BasicDBObject();
                    document.put(MongoConstants.MB_DESTINATION,bindingKey.getDestination());
                    document.put(MongoConstants.MB_DESTINATIONT_TYPE, bindingKey.getDestinationType().toString());
                    document.put(MongoConstants.MB_EXCHANGE, bindingKey.getExchange());
                    document.put(MongoConstants.MB_BINDING_KEYS, bindingKey.getRoutingKey());
                    document.put(MongoConstants.MB_ARG, bindingKey.getArguments().toString());
                    mongoDBHandler.insertDocument(dataBaseName, collectionName, document.toString());
             }
         }
    }
}
