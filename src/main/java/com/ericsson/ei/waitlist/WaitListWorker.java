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
package com.ericsson.ei.waitlist;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.handlers.MatchIdRulesHandler;
import com.ericsson.ei.handlers.RmqHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WaitListWorker {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(WaitListWorker.class);

    @Autowired
    private WaitListStorageHandler waitListStorageHandler;

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    private RulesHandler rulesHandler;

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private MatchIdRulesHandler matchIdRulesHandler;

    @Autowired
    private EventToObjectMapHandler eventToObjectMapHandler;

    @Scheduled(initialDelayString = "${waitlist.initialDelayResend}", fixedRateString = "${waitlist.fixedRateResend}")
    public void run() {
        List<String> documents = waitListStorageHandler.getWaitList();
        for (String document : documents) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode eventJson = objectMapper.readTree(document);
                String id = eventJson.get("_id").asText();
                if (eventToObjectMapHandler.isEventInEventObjectMap(id)) {
                    waitListStorageHandler.dropDocumentFromWaitList(document);
                } else {
                    checkTargetAggregationsExistAndRepublishEvent(eventJson);
                }
            } catch (Exception e) {
                LOGGER.error("Exception occured while trying to resend event: " + document, e);
            }
        }
    }

    public void checkTargetAggregationsExistAndRepublishEvent(JsonNode eventJson) {
        JsonNode event = eventJson.get("Event");
        String eventStr = event.asText();
        RulesObject rulesObject = rulesHandler.getRulesForEvent(eventStr);
        String idRule = rulesObject.getIdentifyRules();

        if (idRule != null && !idRule.isEmpty()) {
            JsonNode ids = jmesPathInterface.runRuleOnEvent(idRule, eventStr);
            if (ids.isArray()) {
                JsonNode idNode = eventJson.get("_id");
                JsonNode timeNode = eventJson.get("Time");
                LOGGER.debug("[EIFFEL EVENT RESENT] id:" + idNode.textValue() + " time:" + timeNode);
                for (final JsonNode idJsonObj : ids) {
                    Collection<String> objects = matchIdRulesHandler.fetchObjectsById(rulesObject,
                            idJsonObj.textValue());
                    if (!objects.isEmpty()) {
                        rmqHandler.publishObjectToWaitlistQueue(eventStr);
                    }
                }
            }
        }
    }
}
