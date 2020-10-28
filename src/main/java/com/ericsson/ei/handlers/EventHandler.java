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
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.utils.MongoDBMonitorThread;
import com.ericsson.ei.utils.SpringContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

@Component
public class EventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandler.class);

    @Autowired
    RulesHandler rulesHandler;

    @Autowired
    IdRulesHandler idRulesHandler;

    @Autowired
    DownstreamIdRulesHandler downstreamIdRulesHandler;

    @Autowired
    Environment environment;

    @Autowired
    MongoDBMonitorThread mongoDBMonitorThread;

    public RulesHandler getRulesHandler() {
        return rulesHandler;
    }

    public void eventReceived(final String event) throws MongoDBConnectionException{
        final RulesObject eventRules = rulesHandler.getRulesForEvent(event);
        idRulesHandler.runIdRules(eventRules, event);
    }

    @Async("eventHandlerExecutor")
    public void onMessage(final Message message, final Channel channel) throws Exception {
        final String messageBody = new String(message.getBody());
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode node = objectMapper.readTree(messageBody);
        final String id = node.get("meta").get("id").toString();
        final long deliveryTag = message.getMessageProperties().getDeliveryTag();
        LOGGER.debug("Thread id {} spawned for EventHandler", Thread.currentThread().getId());
		try {
			eventReceived(messageBody);
			channel.basicAck(deliveryTag, false);
			LOGGER.info("Event {} processed", id);
		} catch (MongoDBConnectionException mdce) {
			if (mdce.getMessage().equalsIgnoreCase("MongoDB Connection down")) {
				if (mongoDBMonitorThread.getState() == Thread.State.NEW
						|| mongoDBMonitorThread.getState() == Thread.State.TERMINATED) {
					// if the previous Thread state is TERMINATED then get a new
					// mongoDBMonitorThread instance
					synchronized (this) {
						if (mongoDBMonitorThread.getState() == Thread.State.TERMINATED) {
							mongoDBMonitorThread = SpringContext.getBean(MongoDBMonitorThread.class);
						}
						// New thread will start to monitor the mongoDB connection status
						if (mongoDBMonitorThread.getState() == Thread.State.NEW) {
							mongoDBMonitorThread.setMongoDBConnected(false);
							mongoDBMonitorThread.start();
						}
					}
				}
				// Continue the loop till the mongoDB connection is Re-established
				while (!mongoDBMonitorThread.isMongoDBConnected()) {
					try {
						Thread.sleep(30000);
						LOGGER.debug("Waiting for MongoDB connection...");
					} catch (InterruptedException ie) {
						LOGGER.error("MongoDBMonitorThread got Interrupted");
					}
				}
			}
			// once the mongoDB Connection is up event will be sent back to queue with
			// un-acknowledgement
			channel.basicNack(deliveryTag, false, true);
			LOGGER.debug("Sent back the event to queue with un-acknowledgement: " + message.getBody());
		} catch (Exception e) {
			LOGGER.error("Event is not Re-queued due to exception " + e);
		}
	}
}