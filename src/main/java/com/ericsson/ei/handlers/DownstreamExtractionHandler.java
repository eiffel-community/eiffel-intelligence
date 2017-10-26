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
            JsonNode aggregatedJsonObject = mapper.readValue(aggregatedDbObject, JsonNode.class);
            runExtraction(rulesObject, mergeId, event, aggregatedJsonObject);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    public void runExtraction(RulesObject rulesObject, String mergeId, String event, JsonNode aggregatedDbObject) {
        JsonNode extractedContent;
        extractedContent = extractContent(rulesObject, event);

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
