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
package com.ericsson.ei.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoExecutionTimeoutException;

@Component
public class ProcessRulesHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessRulesHandler.class);

    @Value("${rules.replacement.marker:%IdentifyRulesEventId%}")
    private String replacementMarker;

    @Autowired
    private JmesPathInterface jmespath;

    @Autowired
    private MergeHandler mergeHandler;

    @Autowired
    private RulesHandler rulesHandler;

    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmespath = jmesPathInterface;
    }

    public void setMergeHandler(MergeHandler mergeHandler) {
        this.mergeHandler = mergeHandler;
    }

    public String runProcessRules(String event, RulesObject rulesObject, String aggregationObject, String objectId, String mergeId) 
            throws MongoExecutionTimeoutException, MongoDBConnectionException {
        String processRules = rulesObject.fetchProcessRules();
        if (processRules != null) {
            String identifyRule = rulesHandler.getRulesForEvent(event).getIdentifyRules();
            String id = jmespath.runRuleOnEvent(identifyRule, event).get(0).textValue();

            if(processRules.contains(replacementMarker)) {
                processRules = processRules.replace(replacementMarker, id);
            }

            LOGGER.info("processRules: {}\n aggregationObject: {}\n event: {}", processRules, aggregationObject, event);
            JsonNode ruleResult = jmespath.runRuleOnEvent(processRules, aggregationObject);
            return mergeHandler.mergeObject(objectId, mergeId, rulesObject, event, ruleResult);
        }

        return aggregationObject;
    }
}
