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
package com.ericsson.ei.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.ShutdownSignalException;

import lombok.Getter;

/**
 * This class is a ConnectionListener that is added to the RMQConnectionFactory, and is there after
 * triggered by Spring RMQConnectionFactory when ever an update in the connection occurs.
 *
 */
@Component
public class RMQConnectionListener implements ConnectionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMQConnectionListener.class);

    @Getter
    private boolean connected = false;

    /**
     * Triggered when a connection is made or remade after a lost connection.
     */
    @Override
    public void onCreate(Connection connection) {
        LOGGER.info("RabbitMQ connection has been initiated.");
        connected = true;

    }

    /**
     * Triggered when connection is closed.
     */
    @Override
    public void onClose(Connection connection) {
        LOGGER.info("RabbitMQ connection has been closed.");
        connected = false;
    }

    /**
     * Triggered when a connection is terminated.
     */
    @Override
    public void onShutDown(ShutdownSignalException signal) {
        LOGGER.info("RabbitMQ connection has been terminated.\nReason:{}", signal.getMessage());
        connected = false;
    }

}
