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
package com.ericsson.ei.jsonmerge;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class MergeHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeHandler.class);

    @Value("${rules.replacement.marker}")
    private String replacementMarker;

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private MergePrepare prepareMergePrepareObject;

    @Autowired
    private ObjectHandler objectHandler;

    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmesPathInterface = jmesPathInterface;
    }

    public void setPrepareMergePrepareObject(MergePrepare prepareMergePrepareObject) {
        this.prepareMergePrepareObject = prepareMergePrepareObject;
    }

    public void setReplacementMarker(String marker) {
        replacementMarker = marker;
    }

    /**
     * @param id            the id of the aggregated object
     * @param mergeId       the id of the link used to identify this aggregated
     *                      object
     * @param rules         the current rules for the received event
     * @param event         the received event
     * @param objectToMerge the object to be merged
     * @return the aggregated object updated with the objectToMerge
     */
    public String mergeObject(String id, String mergeId, RulesObject rules, String event, JsonNode objectToMerge) {
        String mergedObject = null;
        String preparedToMergeObject;
        try {
            // lock and get the AggregatedObject
            String aggregatedObject = getAggregatedObject(id, true);
            LOGGER.debug("AGGREGATED OBJECT : " + aggregatedObject);
            String mergeRule = getMergeRules(rules);
            if (mergeRule != null && !mergeRule.isEmpty()) {
                String updatedRule = replaceIdMarkerInRules(mergeRule, mergeId);
                // populate the rule with data from event
                String ruleForMerge = jmesPathInterface.runRuleOnEvent(updatedRule, event).toString();
                // compute the path where to insert the object
                String mergePath = prepareMergePrepareObject.getMergePath(aggregatedObject, ruleForMerge, false);
                // inflate the object to be merged with levels from merge path
                preparedToMergeObject = prepareMergePrepareObject.addMissingLevels(aggregatedObject,
                        objectToMerge.toString(), ruleForMerge, mergePath);
                LOGGER.debug("PREPARE TO MERGE OBJECT : " + preparedToMergeObject);
            } else {
                preparedToMergeObject = objectToMerge.toString();
            }

            mergedObject = mergeContentToObject(aggregatedObject, preparedToMergeObject);
            LOGGER.debug("Merged Aggregated Object:\n{}", mergedObject);
        } finally {
            // unlocking of document will be performed, when mergedObject will
            // be inserted to database
            objectHandler.updateObject(mergedObject, rules, event, id);
        }

        return mergedObject;
    }

    /**
     *
     * @param id            the id of the aggregated object
     * @param mergeId       the id of the link used to identify this aggregated
     *                      object
     * @param rules         the current rules for the received event
     * @param event         the received event
     * @param objectToMerge the object to be merged
     * @param mergePath     the path in the aggregated object where to merge the
     *                      object
     * @return the aggregated object updated with the objectToMerge
     */
    public String mergeObject(String id, String mergeId, RulesObject rules, String event, JsonNode objectToMerge,
            String mergePath) {
        String mergedObject = null;
        String preparedToMergeObject;
        // lock and get the AggregatedObject
        String aggregatedObject = getAggregatedObject(id, true);

        // String mergeRule = getMergeRules(rules);
        if (mergePath != null && !mergePath.isEmpty()) {
            // inflate the object to be merged with levels from merge path
            preparedToMergeObject = prepareMergePrepareObject.addMissingLevels(aggregatedObject,
                    objectToMerge.toString(), "", mergePath);
        } else {
            preparedToMergeObject = objectToMerge.toString();
        }

        mergedObject = mergeContentToObject(aggregatedObject, preparedToMergeObject);
        LOGGER.debug("Merged Aggregated Object:\n{}", mergedObject);
        // unlocking of document will be performed, when mergedObject will
        // be inserted to database
        objectHandler.updateObject(mergedObject, rules, event, id);

        return mergedObject;
    }

    protected String getMergeRules(RulesObject rules) {
        return rules.getMergeRules();
    }

    /**
     * The merge rule can contain a placeholder for the ids extracted from IdentifyRules.
     * This placeholder need to be replaced with an id from the ones extracted with
     * IdentifyRules before we use it in JMESPath. The placeholder is defined in
     * application.properties as 'rules.replacement.marker'.
     *
     * @param rule string
     * @param id   that the mergeIdMarker will be replaced with
     * @return JSON object ready to be used in JMESPath
     */
    public String replaceIdMarkerInRules(String rule, String id) {

        if (rule.contains(replacementMarker)) {
            String updatedRule = rule.replaceAll(replacementMarker, "\"" + id + "\"");
            updatedRule = "`" + updatedRule + "`";
            return updatedRule;
        }
        return rule;
    }

    public String mergeContentToObject(String aggregatedObject, String preparedObject) {
        JSONObject aggregatedJsonObject = null;
        try {
            aggregatedJsonObject = new JSONObject(aggregatedObject);
            JSONObject preparedJsonObject = new JSONObject(preparedObject);
            updateJsonObject(aggregatedJsonObject, preparedJsonObject);
        } catch (JSONException e) {
            LOGGER.info("Failed to parse JSON.", e);
        }
        return aggregatedJsonObject == null ? null : aggregatedJsonObject.toString();
    }

    /**
     * Append preparedJsonObject to the given aggregatedJsonObject
     *
     * @param aggregatedJsonObject JSON object
     * @param preparedJsonObject   JSON object
     */
    private void updateJsonObject(JSONObject aggregatedJsonObject, JSONObject preparedJsonObject) {
        try {
            Iterator<String> preparedJsonKeys = preparedJsonObject != null ? preparedJsonObject.keys()
                    : new JSONObject().keys();

            while (preparedJsonKeys.hasNext()) {
                String preparedJsonKey = preparedJsonKeys.next();
                if (aggregatedJsonObject.has(preparedJsonKey)) {
                    final Object aggregatedObj = aggregatedJsonObject.get(preparedJsonKey);
                    final Object preparedObj = preparedJsonObject.get(preparedJsonKey);
                    if (aggregatedObj instanceof JSONObject) {
                        JSONObject objectFromAggregation = (JSONObject) (aggregatedObj.equals(null) ? new JSONObject()
                                : aggregatedObj);
                        JSONObject objectFromPreparation = (JSONObject) (preparedObj.equals(null) ? new JSONObject()
                                : preparedObj);
                        updateJsonObject(objectFromAggregation, objectFromPreparation);
                    } else if (aggregatedObj instanceof JSONArray) {
                        JSONArray objectFromAggregation = (JSONArray) (aggregatedObj.equals(null) ? new JSONObject()
                                : aggregatedObj);
                        JSONArray objectFromPreparation = (JSONArray) (preparedObj.equals(null) ? new JSONObject()
                                : preparedObj);
                        updateJsonObject(objectFromAggregation, objectFromPreparation);
                    } else {
                        aggregatedJsonObject.put(preparedJsonKey, preparedJsonObject.get(preparedJsonKey));
                    }
                } else {
                    aggregatedJsonObject.put(preparedJsonKey, preparedJsonObject.get(preparedJsonKey));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update JSON object for aggregatedJsonObject: {} and preparedJsonObject: {}",
                    aggregatedJsonObject, preparedJsonObject, e);
        }
    }

    /**
     * Append JSON elements from preparedJsonObject element-wise into
     * aggregatedJsonObject JSON array
     *
     * @param aggregatedJsonObject JSON array
     * @param preparedJsonObject   JSON array
     */
    private void updateJsonObject(JSONArray aggregatedJsonObject, JSONArray preparedJsonObject) {
        if (preparedJsonObject.length() > aggregatedJsonObject.length()) {
            aggregatedJsonObject.put(new JSONObject());
        }
        for (int i = 0; i < preparedJsonObject.length(); i++) {
            try {
                final Object aggregatedObj = aggregatedJsonObject.get(i);
                final Object preparedObj = preparedJsonObject.get(i);
                if (aggregatedObj instanceof JSONObject) {
                    JSONObject objectFromAggregation = (JSONObject) (aggregatedObj.equals(null) ? new JSONObject()
                            : aggregatedObj);
                    JSONObject objectFromPreparation = (JSONObject) (preparedObj.equals(null) ? new JSONObject()
                            : preparedObj);
                    updateJsonObject(objectFromAggregation, objectFromPreparation);

                } else if (aggregatedObj instanceof JSONArray) {
                    JSONArray objectFromAggregation = (JSONArray) (aggregatedObj.equals(null) ? new JSONArray()
                            : aggregatedObj);
                    JSONArray objectFromPreparation = (JSONArray) (preparedObj.equals(null) ? new JSONArray()
                            : preparedObj);
                    updateJsonObject(objectFromAggregation, objectFromPreparation);
                }
            } catch (JSONException e) {
                LOGGER.error("Failed to update JSON object for aggregatedJsonObject: {} and preparedJsonObject: {}",
                        aggregatedJsonObject, preparedJsonObject, e);
            }
        }
    }

    /**
     * This method set lock property in document in database and returns the
     * aggregated document which will be further modified.
     *
     * @param id String to search in database and lock this document.
     */
    public String getAggregatedObject(String id, boolean withLock) {
        String document = "";
        if (withLock) {
            document = objectHandler.lockDocument(id);
        } else {
            document = objectHandler.findObjectById(id);
        }

        return document;
    }

    public String addNewObject(String event, JsonNode newObject, RulesObject rulesObject)
            throws MongoDBConnectionException {
        return objectHandler.insertObject(newObject, rulesObject, event, null);
    }
}
