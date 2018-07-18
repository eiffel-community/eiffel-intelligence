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

import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.handlers.MatchIdRulesHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

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

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }

    @Scheduled(initialDelayString = "${waitlist.initialDelayResend}", fixedRateString = "${waitlist.fixedRateResend}")
    public void run() throws JSONException {
        RulesObject rulesObject;
        List<String> documents = waitListStorageHandler.getWaitList();
        for (String document : documents) {
            if (eventToObjectMapHandler.isEventInEventObjectMap(new JSONObject(document).getString("_id"))) {
                waitListStorageHandler.dropDocumentFromWaitList(document);
            } else {
                String event = new JSONObject(document).getString("Event");
                rulesObject = rulesHandler.getRulesForEvent(event);
                String idRule = rulesObject.getIdentifyRules();

                if (idRule != null && !idRule.isEmpty()) {
                    JsonNode ids = jmesPathInterface.runRuleOnEvent(idRule, event);
                    if (ids.isArray()) {
                        for (final JsonNode idJsonObj : ids) {
                            Collection<String> objects = matchIdRulesHandler.fetchObjectsById(rulesObject, idJsonObj.textValue());
                            if (!objects.isEmpty()) {
                                rmqHandler.publishObjectToWaitlistQueue(event);
                            }
                        }
                    }
                }
            }
        }
    }
}
