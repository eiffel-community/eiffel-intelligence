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

import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.DownstreamMergeHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoExecutionTimeoutException;

@Component
public class DownstreamExtractionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownstreamExtractionHandler.class);

    @Autowired private JmesPathInterface jmesPathInterface;
    @Autowired private DownstreamMergeHandler mergeHandler;
    @Autowired private ObjectHandler objectHandler;
    

    public void runExtraction(RulesObject rulesObject, String mergeId, String event, String aggregatedDbObject) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            LOGGER.debug("Start extraction of Aggregated Object:\n{} \nwith Event:\n{}", aggregatedDbObject, event);
            JsonNode aggregatedJsonObject = mapper.readValue(aggregatedDbObject, JsonNode.class);
            runExtraction(rulesObject, mergeId, event, aggregatedJsonObject);
        } catch (Exception e) {
            LOGGER.info("Failed with extraction." ,e);
        }
    }

    public void runExtraction(RulesObject rulesObject, String mergeId, String event, JsonNode aggregatedDbObject) 
            throws MongoExecutionTimeoutException, MongoDBConnectionException {
        JsonNode extractedContent;
        extractedContent = extractContent(rulesObject, event);
        LOGGER.debug("Start extraction of Aggregated Object:\n{} \nwith Event:\n{}", aggregatedDbObject.toString(), event);

        if(aggregatedDbObject != null) {
            String objectId = objectHandler.extractObjectId(aggregatedDbObject);
            mergeHandler.mergeObject(objectId, mergeId, rulesObject, event, extractedContent);
        }
    }

    private JsonNode extractContent(RulesObject rulesObject, String event) {
        String extractionRules = rulesObject.getDownstreamExtractionRules();
        return jmesPathInterface.runRuleOnEvent(extractionRules, event);
    }
}
