/***********************************************************************
 *                                                                     *
 * Copyright Ericsson AB 2017                                          *
 *                                                                     *
 * No part of this software may be reproduced in any form without the  *
 * written permission of the copyright owner.                          *
 *                                                                     *
 ***********************************************************************/
package com.ericsson.ei.utils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.Queue.BindOk;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.AMQP.Queue.DeleteOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

/**
 * Basic AMQP communication class.
 *
 * If RabbitMQ uses SSL then some system properties must be added.
 * eiffel.net.ssl.enabled=true javax.net.ssl.trustStore=Path
 * javax.net.ssl.trustStorePassword=Password
 */
public class AMQPCommunication {
    ConnectionFactory factory;
    Connection connection;
    Channel channel;

    private static final Logger LOGGER = LoggerFactory.getLogger(AMQPCommunication.class);

    /**
     * AMQPCommunication constructor.
     *
     * @param host
     *            host name
     * @param port
     *            port number
     */
    public AMQPCommunication(final String host, final int port) {
        LOGGER.info("Setting up RabbitMQ connection to '{}:{}'", host, port);
        this.factory = new ConnectionFactory();
        this.factory.setHost(host);
        this.factory.setPort(port);
        useSSL();
    }

    /**
     * AMQPCommunication constructor, using provided connection factory.
     *
     * @param factory
     *            connection factory
     */
    public AMQPCommunication(final ConnectionFactory factory) {
        LOGGER.info("Setting up RabbitMQ connection to '{}:{}'", factory.getHost(), factory.getPort());
        this.factory = factory;
        useSSL();
    }

    /**
     * Send a message to specified message bus.
     *
     * @param message
     *            content to send
     * @param exchange
     *            exchange to receive content
     * @param key
     *            routing key
     * @return true if message was sent, false otherwise
     */
    public boolean produceMessage(final String message, final String exchange, final String key) {
        LOGGER.info("Preparing to produce message -> Host: {}, Exchange: {}, RoutingKey: {}\nMessage: {}",
                factory.getHost() + ":" + factory.getPort(), exchange, key, message);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.basicPublish(exchange, key, null, message.getBytes());
            LOGGER.info("Message being sent.");
            return true;
        } catch (IOException | TimeoutException e) {
            LOGGER.error("An error occured when trying to produce the message.\nError: {}", e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close connection.\nError {}", e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * Consume a single message synchronously.
     *
     * @param queue
     *            queue to get message from
     * @return received message
     */
    public String consumeMessage(final String queue) {
        LOGGER.info("Preparing to consume message -> Host: {}, Queue: {}", factory.getHost() + ":" + factory.getPort(),
                queue);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            final GetResponse response = channel.basicGet(queue, true);
            if (response != null) {
                final String message = new String(response.getBody(), "UTF-8");
                LOGGER.info("Consumed message: {}", message);
                return message;
            }
        } catch (IOException | TimeoutException e) {
            LOGGER.error("An error occured when trying to consume the message.\nError: {}", e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close connection.\nError {}", e.getMessage());
                }
            }
        }
        return "";
    }

    /**
     * Delete a specified queue.
     *
     * @param queue
     *            name of queue
     * @return true if delete was successful, false otherwise
     */
    public boolean deleteQueue(final String queue) {
        boolean success = false;
        LOGGER.info("Delete queue '{}'", queue);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            final DeleteOk isOK = channel.queueDelete(queue);
            if(isOK != null) {
                success = true;
            }
        } catch (IOException | TimeoutException e) {
            LOGGER.error("An error occured when trying to delete the queue.\nError: {}", e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close connection.\nError {}", e.getMessage());
                }
            }
        }
        return success;
    }

    /**
     * Declare a specified queue.
     *
     * @param queue
     *            name of queue
     * @return true if declare was successful, false otherwise
     */
    public boolean declareQueue(final String queue) {
        boolean success = false;
        LOGGER.info("Declaring queue '{}'", queue);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            final DeclareOk isOK = channel.queueDeclare(queue, true, false, false, null);
            if(isOK != null) {
                success = true;
            }
        } catch (IOException | TimeoutException e) {
            LOGGER.error("An error occured when trying to declare the queue.\nError: {}", e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close connection.\nError {}", e.getMessage());
                }
            }
        }
        return success;
    }

    /**
     * Declare a specified exchange
     *
     * @param exchange
     *            name of exchange
     * @return true if declare was successful, false otherwise
     */
    public boolean declareExchange(final String exchange) {
        boolean success = false;
        LOGGER.info("Declaring exchange '{}'", exchange);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            final AMQP.Exchange.DeclareOk isOK = channel.exchangeDeclare(exchange, BuiltinExchangeType.TOPIC);
            if(isOK != null) {
                success = true;
            }
        } catch (IOException | TimeoutException e) {
            LOGGER.error("An error occurred when trying to declare the exchange.\nError: {}", e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close connection.\nError {}", e.getMessage());
                }
            }
        }
        return success;
    }

    /**
     * Bind queue to an exchange with routing key.
     *
     * @param queue
     *            name of queue
     * @param exchange
     *            name of exchange
     * @param key
     *            name of routing key
     * @return true if binding was successful, false otherwise
     */
    public boolean bindQueue(final String queue, final String exchange, final String key) {
        boolean success = false;
        LOGGER.info("Binding queue '{}' to exchange '{}' with routing key '{}'", queue, exchange, key);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            final BindOk isOK = channel.queueBind(queue, exchange, key);
            if(isOK != null) {
                success = true;
            }
        } catch (IOException | TimeoutException e) {
            LOGGER.error("An error occured when trying to bind the queue.\nError: {}", e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close connection.\nError {}", e.getMessage());
                }
            }
        }
        return success;
    }

    /**
     * Check if SSL Protocol needs to be enabled.
     */
    public final void useSSL() {
        final boolean check = Boolean.valueOf(System.getProperty("eiffel.net.ssl.enabled", "false"));
        if (check) {
            try {
                this.factory.useSslProtocol(SSLContext.getDefault());
            } catch (NoSuchAlgorithmException e) {
                LOGGER.info("Failed to get SSL context.\nError {}", e.getMessage());
            }
        }
    }

    /**
     * @return current host name
     */
    public String getHost() {
        return this.factory.getHost();
    }

    /**
     * @param host
     *            host name to set
     */
    public void setHost(final String host) {
        this.factory.setHost(host);
    }

    /**
     * @return current port number
     */
    public int getPort() {
        return this.factory.getPort();
    }

    /**
     * @param port
     *            port number to set
     */
    public void setPort(final int port) {
        this.factory.setPort(port);
    }
}
