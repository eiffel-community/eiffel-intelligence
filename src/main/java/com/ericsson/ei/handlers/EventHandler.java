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
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

@Component
@Scope(value = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EventHandler {

    private static Logger log = LoggerFactory.getLogger(EventHandler.class);

    @Autowired
    RulesHandler rulesHandler;

    @Autowired
    IdRulesHandler idRulesHandler;

    @Autowired
    DownstreamIdRulesHandler downstreamIdRulesHandler;

    @Autowired
    Environment environment;

    public RulesHandler getRulesHandler() {
        return rulesHandler;
    }

    public void eventReceived(String event) {
        log.info("Thread id " + Thread.currentThread().getId() + " spawned");
        RulesObject eventRules = rulesHandler.getRulesForEvent(event);
        idRulesHandler.runIdRules(eventRules, event);
        // downstreamIdRulesHandler.runIdRules(eventRules, event);
    }

    @Async
    public void onMessage(Message message, Channel channel) throws Exception {
        String messageBody = new String(message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(messageBody);
        String id = node.get("meta").get("id").toString();
        String port = environment.getProperty("local.server.port");
        log.debug("Event {} received on port {}", id, port);

        eventReceived(messageBody);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag, false);

        log.debug("Event {} processed on port {}", id, port);
    }
}
