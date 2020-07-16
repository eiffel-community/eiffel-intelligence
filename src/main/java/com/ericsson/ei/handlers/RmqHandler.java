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
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.ericsson.ei.listener.EIMessageListenerAdapter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.BasicDBObject;

import lombok.Getter;
import lombok.Setter;

@Component
public class RmqHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RmqHandler.class);

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
    private String bindingKeys;

    @Getter
    @Setter
    @Value("${rabbitmq.consumerName}")
    private String consumerName;
    
    @Getter
    @Setter
    @Value("${spring.data.mongodb.database}")
    private String dataBaseName;

    @Getter
    @Setter
    @Value("${bindingkeys.collection.name}")
    private String collectionName;

    @Value("${threads.maxPoolSize}")
    private int maxThreads;
    
    @Setter
    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Setter
    @JsonIgnore
    private RabbitTemplate rabbitTemplate;
    @Getter
    @JsonIgnore
    private CachingConnectionFactory cachingConnectionFactory;
    @Getter
    @JsonIgnore
    private SimpleMessageListenerContainer container;

    @Getter
    @JsonIgnore
    private AmqpAdmin amqpAdmin;

    @Bean
    public ConnectionFactory connectionFactory() {
        cachingConnectionFactory = new CachingConnectionFactory(host, port);

        if (user != null && user.length() != 0 && password != null && password.length() != 0) {
            cachingConnectionFactory.setUsername(user);
            cachingConnectionFactory.setPassword(password);
        }

        if (tlsVersion != null && !tlsVersion.isEmpty()) {
            try {
                LOGGER.debug("Using SSL/TLS version {} connection to RabbitMQ.", tlsVersion);
                cachingConnectionFactory.getRabbitConnectionFactory().useSslProtocol(tlsVersion);
            } catch (final Exception e) {
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
    public Queue externalQueue() {
        return new Queue(getQueueName(), true);
    }

    @Bean
    public Queue internalQueue() {
        return new Queue(getWaitlistQueueName(), true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    Binding binding() {
        return BindingBuilder.bind(internalQueue()).to(exchange()).with(getWaitlistQueueName());
    }

    @Bean
    public List<Binding> bindings(){
        final String[] bindingKeysArray = splitBindingKeys(bindingKeys);
        final List<Binding> bindingList = new ArrayList<>();
        for (final String bindingKey : bindingKeysArray) {
            bindingList.add(BindingBuilder.bind(externalQueue()).to(exchange()).with(bindingKey));
        }
        deleteBindings(bindingKeysArray,bindingList);
        return bindingList;
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
                String condition = "{\"bindingKeys\": /.*" + mongoDbBindingKey + "/}";
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
        // To store the new binding key into the mongoDB.
        storeNewBindingKeys(existingBindingsData, AMQPBindingObjectList);
    }

    /**
     * This method is used to store the bindings of new binding key into mongoDB.
     * @return
     */

    private void storeNewBindingKeys(ArrayList<String> existingBindingsData, List<Binding> AMQPBindingObjectList){
    // comparing with the stored key and adding the new binding key into the mongoDB.
       for(final Binding bindingKey:AMQPBindingObjectList){
            if(existingBindingsData.contains(bindingKey.getRoutingKey())){
                LOGGER.info("Binding already present in mongoDB");
            }else{
                    BasicDBObject document = new BasicDBObject();
                    document.put("destination",bindingKey.getDestination());
                    document.put("destinationType", bindingKey.getDestinationType().toString());
                    document.put("exchange", bindingKey.getExchange());
                    document.put("bindingKeys", bindingKey.getRoutingKey());
                    document.put("arg", bindingKey.getArguments().toString());
                    mongoDBHandler.insertDocument(dataBaseName, collectionName, document.toString());
             }
         }
    }

    @Bean
    public SimpleMessageListenerContainer bindToQueueForRecentEvents(
            final ConnectionFactory springConnectionFactory,
            final EventHandler eventHandler) {
        final MessageListenerAdapter listenerAdapter = new EIMessageListenerAdapter(eventHandler);
        container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(springConnectionFactory);
        container.setQueueNames(getQueueName(), getWaitlistQueueName());
        container.setMessageListener(listenerAdapter);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setPrefetchCount(maxThreads);
        return container;
    }

    /**
     * This configures the settings used when sending messages to the waitlist.
     * It uses the exchange and queue defined in the properties and the routing key
     * is a reserved one intended for internal use only.
     * @return
     */
    @Bean
    public RabbitTemplate rabbitMqTemplate() {
        if (rabbitTemplate == null) {
            if (cachingConnectionFactory != null) {
                rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
            } else {
                rabbitTemplate = new RabbitTemplate(connectionFactory());
            }

            rabbitTemplate.setExchange(exchangeName);
            rabbitTemplate.setQueue(getWaitlistQueueName());
            rabbitTemplate.setRoutingKey(getWaitlistQueueName());
            rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
                @Override
                public void confirm(final CorrelationData correlationData, final boolean ack, final String cause) {
                    LOGGER.info("Received confirm with result : {}", ack);
                }
            });
        }
        return rabbitTemplate;
    }

    public String getQueueName() {
        final String durableName = queueDurable ? "durable" : "transient";
        return domainId + "." + componentName + "." + consumerName + "." + durableName;
    }

    public String getWaitlistQueueName() {

        final String durableName = queueDurable ? "durable" : "transient";
        return domainId + "." + componentName + "." + consumerName + "." + durableName + "."
                + waitlistSufix;
    }

    public void publishObjectToWaitlistQueue(final String message) {
        LOGGER.debug("Publishing message to message bus...");
        rabbitTemplate.convertAndSend(message);
    }

    public void close() {
        try {
            container.destroy();
            cachingConnectionFactory.destroy();
        } catch (final Exception e) {
            LOGGER.error("Exception occurred while closing connections.", e);
        }
    }

    private String[] splitBindingKeys(final String bindingKeys) {
        final String bindingKeysWithoutWhitespace = bindingKeys.replaceAll("\\s+", "");
        return bindingKeysWithoutWhitespace.split(",");
    }
}
