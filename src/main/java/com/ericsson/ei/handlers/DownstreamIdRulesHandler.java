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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.waitlist.WaitListStorageHandler;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class DownstreamIdRulesHandler {

    static Logger log = (Logger) LoggerFactory.getLogger(DownstreamIdRulesHandler.class);

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private MatchIdRulesHandler matchIdRulesHandler;

    @Autowired
    private DownstreamExtractionHandler downstreamExtractionHandler;

    @Autowired
    private WaitListStorageHandler waitListStorageHandler;

    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmesPathInterface = jmesPathInterface;
    }

    public void runIdRules(RulesObject rulesObject, String event) {
        if (rulesObject != null && event != null) {
            JsonNode idsJsonObj = getIds(rulesObject, event);
            List<String> objects;
            String id;
            if (idsJsonObj != null && idsJsonObj.isArray()) {
                for (final JsonNode idJsonObj : idsJsonObj) {
                    id = idJsonObj.textValue();
                    objects = matchIdRulesHandler.fetchObjectsById(rulesObject, id);
                    for (String object : objects) {
                        downstreamExtractionHandler.runExtraction(rulesObject, id, event, object);
                    }
                    if (objects.size() == 0) {
                        try {
                            waitListStorageHandler.addEventToWaitList(event, rulesObject);
                        } catch (Exception e) {
                            log.info(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    public JsonNode getIds(RulesObject rulesObject, String event) {
        String idRule = rulesObject.getDownstreamIdentifyRules();
        JsonNode ids = null;
        if (idRule != null && !idRule.isEmpty()) {
            try {
                ids = jmesPathInterface.runRuleOnEvent(idRule, event);
            } catch (Exception e) {
                log.info(e.getMessage(),e);
            }
        }

        return ids;
    }
}
