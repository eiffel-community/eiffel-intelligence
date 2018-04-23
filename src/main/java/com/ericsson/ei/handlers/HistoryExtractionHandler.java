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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.jsonmerge.MergePrepare;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        // empty so we use it instead of passed pathInAggregatedObject
        String aggregatedObject = mergeHandler.getAggregatedObject(aggregatedObjectId, false);
        String longRuleString = createLongMergeRule(pathInAggregatedObject, ruleJson);
        String objAtPathStr = "";
        String pathTrimmed = mergePrepare.trimLastInPath(pathInAggregatedObject, ".");
        try {
            // if (!pathInAggregatedObject.isEmpty()) {

            pathTrimmed = mergePrepare.makeJmespathArrayIndexes(pathTrimmed);
            JsonNode objAtPath = jmesPathInterface.runRuleOnEvent(pathTrimmed, aggregatedObject);
            objAtPathStr = objAtPath.toString();
            // }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        String array_path = getPathFromExtractedContent(objAtPathStr, ruleString);

        if (!array_path.isEmpty()) {
            // pathInAggregatedObject = array_path;
            pathInAggregatedObject = pathTrimmed + "." + array_path;
            pathInAggregatedObject = MergePrepare.destringify(pathInAggregatedObject);
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

    private String createLongMergeRule(String longPath, JsonNode ruleJson) {
        // longPath = mergePrepare.trimLastInPath(longPath, ".");
        String pathNoIndexes = mergePrepare.removeArrayIndexes(longPath);
        String[] pathSubstrings = pathNoIndexes.split("\\.");
        JsonNode mergeObject = ruleJson;

        try {
            String pathString = "";
            for (int i = 1; i < pathSubstrings.length; i++) {
                int mergePathIndex = pathSubstrings.length - (1 + i);
                String pathElement = pathSubstrings[mergePathIndex];
                ObjectNode newObject = JsonNodeFactory.instance.objectNode();
                newObject.put(pathElement, mergeObject);
                mergeObject = newObject;
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

        return mergeObject.toString();
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
        String ruleKey = MergePrepare.destringify(rulePair[0]);
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
