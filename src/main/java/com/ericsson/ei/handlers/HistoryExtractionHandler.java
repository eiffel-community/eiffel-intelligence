package com.ericsson.ei.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;

public class HistoryExtractionHandler {

    static Logger log = (Logger) LoggerFactory.getLogger(HistoryExtractionHandler.class);

    @Autowired private JmesPathInterface jmesPathInterface;
    @Autowired private MergeHandler mergeHandler;


    public void runHistoryExtraction(String aggregatedObjectId, RulesObject rulesObject, String event, String pathInAggregatedObject) {
            JsonNode extractedContent;
            extractedContent = extractContent(rulesObject, event);
            mergeHandler.mergeObject(aggregatedObjectId, rulesObject, event, extractedContent, pathInAggregatedObject);
    }

    private JsonNode extractContent(RulesObject rulesObject, String event) {
        String extractonRules;
        extractonRules = rulesObject.getHistoryExtractionRules();
        return jmesPathInterface.runRuleOnEvent(extractonRules, event);
    }


}



