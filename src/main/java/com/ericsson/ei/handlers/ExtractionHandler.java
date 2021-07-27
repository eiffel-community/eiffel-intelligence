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

import com.ericsson.ei.rules.ProcessRulesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.exception.PropertyNotFoundException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class ExtractionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractionHandler.class);

    @Autowired private JmesPathInterface jmesPathInterface;
    @Autowired private MergeHandler mergeHandler;
    @Autowired private ObjectHandler objectHandler;
    @Autowired private ProcessRulesHandler processRulesHandler;
    @Autowired private UpStreamEventsHandler upStreamEventsHandler;

    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmesPathInterface = jmesPathInterface;
    }

    public void setMergeHandler(MergeHandler mergeHandler) {
        this.mergeHandler = mergeHandler;
    }

    public void setProcessRulesHandler(ProcessRulesHandler processRulesHandler) {
        this.processRulesHandler = processRulesHandler;
    }

    public void setUpStreamEventsHandler(UpStreamEventsHandler upStreamEventsHandler) {
        this.upStreamEventsHandler = upStreamEventsHandler;
    }

    public void setObjectHandler(ObjectHandler objectHandler) {
        this.objectHandler = objectHandler;
    }

    public void runExtraction(RulesObject rulesObject, String id, String event, String aggregatedDbObject) throws MongoDBConnectionException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode aggregatedJsonObject = mapper.readTree(aggregatedDbObject);
            runExtraction(rulesObject, id, event, aggregatedJsonObject);
        } catch (Exception e) {
            LOGGER.error("Failed with extraction.", e);
            if (e.getMessage() != null && e.getMessage().equalsIgnoreCase("MongoDB Connection down")) {
                throw new MongoDBConnectionException("MongoDB Connection down");
            }
        }
    }

    public void runExtraction(RulesObject rulesObject, String mergeId, String event, JsonNode aggregatedDbObject) throws MongoDBConnectionException {
        try {
            JsonNode extractedContent = extractContent(rulesObject, event);
            
            String mergedContent = null;
            String aggregatedObjectId = null;

            if(aggregatedDbObject != null) {
                LOGGER.debug("ExtractionHandler: Merging Aggregated Object:\n{}"
                        + "\nwith extracted content:\n{}"
                        + "\nfrom event:\n{}",
                        aggregatedDbObject.toString(), extractedContent.toString(), event);
                aggregatedObjectId = objectHandler.extractObjectId(aggregatedDbObject);
                mergedContent = mergeHandler.mergeObject(aggregatedObjectId, mergeId, rulesObject, event, extractedContent);
                mergedContent = processRulesHandler.runProcessRules(event, rulesObject, mergedContent, aggregatedObjectId, mergeId);
            } else {
                LOGGER.trace("***** Extraction starts for the aggregation Id: " + mergeId);
                ObjectNode objectNode = (ObjectNode) extractedContent;
                objectNode.put("TemplateName", rulesObject.getTemplateName());
                mergedContent = mergeHandler.addNewObject(event, extractedContent, rulesObject);
                aggregatedObjectId = mergeId;
                upStreamEventsHandler.runHistoryExtractionRulesOnAllUpstreamEvents(mergeId);
                mergedContent = objectHandler.findObjectById(mergeId);
                LOGGER.trace("**** Extraction ends for the aggregation Id: " + mergeId);
            }
            objectHandler.checkAggregations(mergedContent, aggregatedObjectId);
        } catch (PropertyNotFoundException e) {
            LOGGER.debug("Did not run history extraction on upstream events.", e);
        } catch (Exception e) {
            LOGGER.error("Failed to run extraction for event {}", event, e);
            if (e.getMessage() != null && e.getMessage().equalsIgnoreCase("MongoDB Connection down")) {
                throw new MongoDBConnectionException("MongoDB Connection down");
            }
        }
    }

    private JsonNode extractContent(RulesObject rulesObject, String event) {
        String extractionRules;
        extractionRules = rulesObject.getExtractionRules();
        return jmesPathInterface.runRuleOnEvent(extractionRules, event);
    }

}
