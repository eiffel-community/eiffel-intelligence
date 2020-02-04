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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.rules.MatchIdRulesHandler;
import com.ericsson.ei.handlers.RMQHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientException;

@Component
public class WaitListWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitListWorker.class);

    @Autowired
    private WaitListStorageHandler waitListStorageHandler;

    @Autowired
    private RMQHandler rmqHandler;

    @Autowired
    private RulesHandler rulesHandler;

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private MatchIdRulesHandler matchIdRulesHandler;

    @Autowired
    private EventToObjectMapHandler eventToObjectMapHandler;

    private boolean shutdownInProgress = false;
    private static final String ID = "_id";
    private static final String EVENT = "Event";
    private static final String TIME = "Time";

    @Scheduled(initialDelayString = "${waitlist.resend.initial.delay}", fixedRateString = "${waitlist.resend.fixed.rate}")
    public void run() {
        if(shutdownInProgress) {
            return;
        }
        try {
            getAllDocumentsAndCheckTargetAggregations();
        } catch (MongoClientException e) {
            LOGGER.error("Failed to get documents from MongoDB", e.getMessage());
        }
    }

    private void getAllDocumentsAndCheckTargetAggregations() {
        List<String> documents = waitListStorageHandler.getWaitList();
        for (String document : documents) {
            try {
                checkAggregationsExistAndRepublishEvent(document);
            } catch (Exception e) {
                LOGGER.error("Exception occured while trying to resend event: {}", document, e);
            }
        }
    }

    private void checkAggregationsExistAndRepublishEvent(String document) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode eventJson = objectMapper.readTree(document);
        String id = eventJson.get(ID).asText();
        if (eventToObjectMapHandler.isEventInEventObjectMap(id)) {
            waitListStorageHandler.dropDocumentFromWaitList(document);
        } else {
            checkTargetAggregationsExistAndRepublishEvent(eventJson);
        }
    }

    public void checkTargetAggregationsExistAndRepublishEvent(JsonNode eventJson) {
        JsonNode event = eventJson.get(EVENT);
        String eventStr = event.asText();
        RulesObject rulesObject = rulesHandler.getRulesForEvent(eventStr);
        String idRule = rulesObject.getIdentifyRules();
        // waitlistId is only used for debugging and tests
        int waitlistId = this.hashCode();
        if (idRule != null && !idRule.isEmpty()) {
            JsonNode ids = jmesPathInterface.runRuleOnEvent(idRule, eventStr);
            if (ids.isArray()) {
                JsonNode idNode = eventJson.get(ID);
                JsonNode timeNode = eventJson.get(TIME);
                LOGGER.debug("[EIFFEL EVENT RESENT FROM WAITLIST: {}] id:{} time:{}", waitlistId, idNode.textValue(),
                        timeNode);
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

    @PreDestroy
    public void shutdownSignalSent() {
        shutdownInProgress = true;
    }
}