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

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.jsonmerge.MergePrepare;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.wnameless.json.flattener.JsonFlattener;

import org.springframework.stereotype.Component;

// TODO: Auto-generated Javadoc
/**
 * The Class HistoryExtractionHandler.
 */
@Component
public class HistoryExtractionHandler {

    static Logger log = (Logger) LoggerFactory.getLogger(HistoryExtractionHandler.class);

    @Autowired
    private JmesPathInterface jmesPathInterface;
    @Autowired
    private MergeHandler mergeHandler;
    @Autowired
    private MergePrepare mergePrepare;

    /**
     * Run history extraction.
     *
     * @param aggregatedObjectId
     *            the aggregated object id
     * @param rules
     *            the rules object
     * @param event
     *            the event
     * @param pathInAggregatedObject
     *            the path in aggregated object
     * @return the string
     */
    public String runHistoryExtraction(String aggregatedObjectId, RulesObject rules, String event,
            String pathInAggregatedObject) {
        JsonNode objectToMerge = extractContent(rules, event);
        if (objectToMerge == null) {
            return pathInAggregatedObject;
        }

        JsonNode ruleJson = getHistoryPathRule(rules, event);
        String ruleString = ruleJson.toString();

        // if we need to add append to an array then array_path will not be
        // empty so we use it instead passed pathInAggregatedObject
        String aggregatedObject = mergeHandler.getAggregatedObject(aggregatedObjectId, false);
        String array_path = getPathFromExtractedContent(aggregatedObject, ruleString);
        if (!array_path.isEmpty()) {
            pathInAggregatedObject = array_path;
        } else {
            String ruleKey = getRulePath(ruleString);
            if (pathInAggregatedObject.isEmpty()) {
                pathInAggregatedObject = ruleKey;
            } else {
                if (pathInAggregatedObject.length() > 0 && pathInAggregatedObject.lastIndexOf(".") != -1)
                    pathInAggregatedObject = pathInAggregatedObject.substring(0,
                            pathInAggregatedObject.lastIndexOf("."));
                pathInAggregatedObject += "." + ruleKey;
            }
        }

        mergeHandler.mergeObject(aggregatedObjectId, aggregatedObjectId, rules, event, objectToMerge,
                pathInAggregatedObject);

        return pathInAggregatedObject;
    }

    /**
     * Get the rule path as dot notation
     * 
     * @param stringRule
     *            - rule as string
     * @return
     */
    private String getRulePath(String stringRule) {
        String flattenRule = JsonFlattener.flatten(stringRule);
        String[] rulePair = flattenRule.split(":");
        String ruleKey = mergePrepare.destringify(rulePair[0]);
        return ruleKey;
    }

    /**
     * Gets the path from given content.
     *
     * @param content
     *            the content
     * @return the path from given content
     */
    private String getPathFromExtractedContent(String content, String mergeRules) {
        return mergePrepare.getMergePath(content, mergeRules);
    }

    /**
     * Extract content.
     *
     * @param rulesObject
     *            the rules object
     * @param event
     *            the event
     * @return the json node
     */
    private JsonNode extractContent(RulesObject rulesObject, String event) {
        String extractionRules;
        extractionRules = rulesObject.getHistoryExtractionRules();
        return jmesPathInterface.runRuleOnEvent(extractionRules, event);
    }

    /**
     * @param rulesObject
     * @param event
     * @return
     */
    private JsonNode getHistoryPathRule(RulesObject rulesObject, String event) {
        String rule = rulesObject.getHistoryPathRules();
        return jmesPathInterface.runRuleOnEvent(rule, event);
    }

}
