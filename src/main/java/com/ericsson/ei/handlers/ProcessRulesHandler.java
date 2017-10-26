package com.ericsson.ei.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ProcessRulesHandler {

     static Logger log = (Logger) LoggerFactory.getLogger(ProcessRulesHandler.class);

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

    public String runProcessRules(String event, RulesObject rulesObject, String aggregationObject, String objectId, String mergeId) {
        String processRules = rulesObject.fetchProcessRules();
        if (processRules != null) {
            log.info("processRules: " + processRules);
            log.info("aggregationObject: " + aggregationObject);
            log.info("event: " + event);
            JsonNode ruleResult = jmespath.runRuleOnEvent(processRules, aggregationObject);
            String aggregatedObject = mergeHandler.mergeObject(objectId, mergeId, rulesObject, event, ruleResult);
            return aggregatedObject;
        }

        return aggregationObject;
    }
}
