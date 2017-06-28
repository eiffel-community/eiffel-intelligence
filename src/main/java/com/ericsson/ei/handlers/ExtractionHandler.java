package com.ericsson.ei.handlers;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ExtractionHandler {

    static Logger log = (Logger) LoggerFactory.getLogger(ExtractionHandler.class);

    @Autowired private JmesPathInterface jmesPathInterface;
    @Autowired private MergeHandler mergeHandler;
    //TODO:@Autowired private ProcessRulesHandler processRulesHandler;
    //TODO:@Autowired private HistoryIdRulesHandler historyIdRulesHandler;

    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmesPathInterface = jmesPathInterface;
    }

    public void setMergeHandler(MergeHandler mergeHandler) {
        this.mergeHandler = mergeHandler;
    }

//    public void setProcessRulesHandler(ProcessRulesHandler processRulesHandler) {
//        this.processRulesHandler = processRulesHandler;
//    }
//
//    public void setHistoryIdRulesHandler(HistoryIdRulesHandler historyIdRulesHandler) {
//        this.historyIdRulesHandler = historyIdRulesHandler;
//    }

    public void runExtraction(RulesObject rulesObject, String id, String event, String aggregatedObject) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode aggregatedJsonObject = mapper.readValue(aggregatedObject, JsonNode.class);
            runExtraction(rulesObject, id, event, aggregatedJsonObject);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    public void runExtraction(RulesObject rulesObject, String id, String event, JsonNode aggregatedObject) {
        JsonNode extractedContent;
        extractedContent = extractContent(rulesObject, event);

        if(aggregatedObject != null) {
            String mergedContent = mergeHandler.mergeObject(id, rulesObject, event, extractedContent);
            //aggregationObject = processRulesHandler.runProcessRules(aggregationObject, rulesObject);
            //historyIdRulesHandler.runHistoryIdRules(aggregationObject, rulesObject, event);
        } else {
            mergeHandler.addNewObject(event, extractedContent);
        }
    }

    private JsonNode extractContent(RulesObject rulesObject, String event) {
        String extractonRules;
        extractonRules = rulesObject.getExtractionRules();
        return jmesPathInterface.runRuleOnEvent(extractonRules, event);
    }
}
