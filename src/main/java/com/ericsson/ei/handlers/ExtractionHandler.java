package com.ericsson.ei.handlers;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ExtractionHandler {

    static Logger log = (Logger) LoggerFactory.getLogger(ExtractionHandler.class);

    @Autowired private JmesPathInterface jmesPathInterface;
    @Autowired private MergeHandler mergeHandler;
    @Autowired private ObjectHandler objectHandler;
    @Autowired private ProcessRulesHandler processRulesHandler;
    //TODO:@Autowired private HistoryIdRulesHandler historyIdRulesHandler;

    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmesPathInterface = jmesPathInterface;
    }

    public void setMergeHandler(MergeHandler mergeHandler) {
        this.mergeHandler = mergeHandler;
    }

    public void setProcessRulesHandler(ProcessRulesHandler processRulesHandler) {
        this.processRulesHandler = processRulesHandler;
    }

    public void setObjectHandler(ObjectHandler objectHandler) {
        this.objectHandler = objectHandler;
    }

    public void runExtraction(RulesObject rulesObject, String id, String event, String aggregatedDbObject) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode aggregatedJsonObject = mapper.readValue(aggregatedDbObject, JsonNode.class);
            runExtraction(rulesObject, id, event, aggregatedJsonObject);
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
            mergedContent = processRulesHandler.runProcessRules(event, rulesObject, mergedContent, objectId, mergeId);
            //historyIdRulesHandler.runHistoryIdRules(aggregationObject, rulesObject, event);
        } else {
            ObjectNode objectNode = (ObjectNode) extractedContent;
            objectNode.put("TemplateName", rulesObject.getTemplateName());
            mergeHandler.addNewObject(event, extractedContent, rulesObject);
        }
    }

    private JsonNode extractContent(RulesObject rulesObject, String event) {
        String extractionRules;
        extractionRules = rulesObject.getExtractionRules();
        return jmesPathInterface.runRuleOnEvent(extractionRules, event);
    }

}
