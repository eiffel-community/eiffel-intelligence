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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.DownstreamMergeHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DownstreamExtractionHandler {

    private static Logger log = LoggerFactory.getLogger(DownstreamExtractionHandler.class);

    @Autowired private JmesPathInterface jmesPathInterface;
    @Autowired private DownstreamMergeHandler mergeHandler;
    @Autowired private ObjectHandler objectHandler;

    public void runExtraction(RulesObject rulesObject, String mergeId, String event, String aggregatedDbObject) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            log.debug("Start extraction of Aggregated Object:\n" + aggregatedDbObject + 
            		"\nwith Event:\n" + event);
            JsonNode aggregatedJsonObject = mapper.readValue(aggregatedDbObject, JsonNode.class);
            runExtraction(rulesObject, mergeId, event, aggregatedJsonObject);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    public void runExtraction(RulesObject rulesObject, String mergeId, String event, JsonNode aggregatedDbObject) {
        JsonNode extractedContent;
        extractedContent = extractContent(rulesObject, event);
        log.debug("Start extraction of Aggregated Object:\n" + aggregatedDbObject.toString() + 
        		"\nwith Event:\n" + event);


        if(aggregatedDbObject != null) {
            String objectId = objectHandler.extractObjectId(aggregatedDbObject);
            String mergedContent = mergeHandler.mergeObject(objectId, mergeId, rulesObject, event, extractedContent);
        }
    }

    private JsonNode extractContent(RulesObject rulesObject, String event) {
        String extractionRules = rulesObject.getDownstreamExtractionRules();
        return jmesPathInterface.runRuleOnEvent(extractionRules, event);
    }
}
