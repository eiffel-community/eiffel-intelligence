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
package com.ericsson.ei.rmqhandler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.EventHandler;

@Component
public class RmqHandler {
	static Logger log = (Logger) LoggerFactory.getLogger(RmqHandler.class);

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

	@JsonIgnore
	@Getter
	@Setter
	@Value("${rabbitmq.user}")
	private String user;

	@JsonIgnore
	@Getter
	@Setter
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
	@Value("${rabbitmq.routing.key}")
	private String routingKey;

	@Getter
	@Setter
	@Value("${rabbitmq.consumerName}")
	private String consumerName;

	private RabbitTemplate rabbitTemplate;
	private CachingConnectionFactory factory;
	private SimpleMessageListenerContainer container;
	private SimpleMessageListenerContainer waitlistContainer;

	@Bean
	ConnectionFactory connectionFactory() {
		com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();
		connectionFactory.setHost(host);
		connectionFactory.setPort(port);
		if (user != null && user.length() != 0 && password != null && password.length() != 0) {
			connectionFactory.setUsername(user);
			connectionFactory.setPassword(password);
		}

		if (tlsVersion != null && !tlsVersion.isEmpty()) {
			try {
				log.info("Using SSL/TLS version " + tlsVersion + " connection to RabbitMQ.");
				connectionFactory.useSslProtocol(tlsVersion);
			} catch (KeyManagementException e) {
				log.error("Failed to set SSL/TLS version.");
				log.error(e.getMessage(), e);
			} catch (NoSuchAlgorithmException e) {
				log.error("Failed to set SSL/TLS version.");
				log.error(e.getMessage(), e);
			}
		}

		factory = new CachingConnectionFactory(connectionFactory);
		factory.setPublisherConfirms(true);
		factory.setPublisherReturns(true);

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
			log.info(e.getMessage(), e);
		}
	}
}
