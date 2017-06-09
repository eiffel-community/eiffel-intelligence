package com.ericsson.ei.extraction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ExtractionHandler {

    @Autowired private JmesPathInterface jmesPathInterface;
    //TODO:@Autowired private MergeHandler mergeHandler;
    //TODO:@Autowired private ProcessRulesHandler processRulesHandler;
    //TODO:@Autowired private HistoryIdRulesHandler historyIdRulesHandler;

    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmesPathInterface = jmesPathInterface;
    }

//    public void setMergeHandler(MergeHandler mergeHandler) {
//        this.mergeHandler = mergeHandler;
//    }
//
//    public void setProcessRulesHandler(ProcessRulesHandler processRulesHandler) {
//        this.processRulesHandler = processRulesHandler;
//    }
//
//    public void setHistoryIdRulesHandler(HistoryIdRulesHandler historyIdRulesHandler) {
//        this.historyIdRulesHandler = historyIdRulesHandler;
//    }

    public void runExtraction(RulesObject rulesObject, String event , JsonNode aggregationObject) {
        JsonNode extractedContent;
        extractedContent = extractContent(rulesObject, event);

        if(aggregationObject != null) {
            //aggregationObject = mergeHandler.mergeToObject(extractedContent, aggregationObject, rulesObject);
            //aggregationObject = processRulesHandler.runProcessRules(aggregationObject, rulesObject);
            //historyIdRulesHandler.runHistoryIdRules(aggregationObject, rulesObject, event);
        } else {
            //mergeHandler.addNewObject(extractedContent);
        }

    }

    private JsonNode extractContent(RulesObject rulesObject, String event) {
        String extractonRules;
        extractonRules = rulesObject.getExtractionRules();
        return jmesPathInterface.runRuleOnEvent(extractonRules, event);
    }
}
