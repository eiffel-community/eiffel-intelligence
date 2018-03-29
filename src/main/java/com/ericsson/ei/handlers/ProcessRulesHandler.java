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

import com.ericsson.ei.rules.RulesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ProcessRulesHandler {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ProcessRulesHandler.class);

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

    public String runProcessRules(String event, RulesObject rulesObject, String aggregationObject, String objectId, String mergeId) {
        String processRules = rulesObject.fetchProcessRules();
        if (processRules != null) {
            String identifyRule = rulesHandler.getRulesForEvent(event).getIdentifyRules();
            String id = jmespath.runRuleOnEvent(identifyRule, event).get(0).textValue();

            if(processRules.contains("%IdentifyRules%")) {
                processRules = processRules.replace("%IdentifyRules%", id);
            }

            LOGGER.info("processRules: " + processRules);
            LOGGER.info("aggregationObject: " + aggregationObject);
            LOGGER.info("event: " + event);
            JsonNode ruleResult = jmespath.runRuleOnEvent(processRules, aggregationObject);
            return mergeHandler.mergeObject(objectId, mergeId, rulesObject, event, ruleResult);
        }

        return aggregationObject;
    }
}
