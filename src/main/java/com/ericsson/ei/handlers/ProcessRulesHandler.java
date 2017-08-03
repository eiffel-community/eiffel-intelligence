package com.ericsson.ei.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ProcessRulesHandler {

    @Autowired
    JmesPathInterface jmespath;

    @Autowired
    MergeHandler mergeHandler;

    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmespath = jmesPathInterface;
    }

    public void setMergeHandler(MergeHandler mergeHandler) {
        this.mergeHandler = mergeHandler;
    }

    public String runProcessRules(String event, RulesObject rulesObject, String aggregationObject, String objectId) {
        String processRules = rulesObject.fetchProcessRules();
        JsonNode ruleResult = jmespath.runRuleOnEvent(processRules, aggregationObject);
        String aggregatedObject = mergeHandler.mergeObject(objectId, rulesObject, event, ruleResult);
        return aggregatedObject;
    }
}
