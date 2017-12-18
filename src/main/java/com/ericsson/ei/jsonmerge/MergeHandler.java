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

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Component
public class MergeHandler {

    static Logger log = (Logger) LoggerFactory.getLogger(MergeHandler.class);

    @Value("${mergeidmarker}") private String mergeIdMarker;

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

    public void setMergeIdMarker(String marker) {
        mergeIdMarker = marker;
    }

    public String mergeObject(String id, String mergeId, RulesObject rules, String event, JsonNode objectToMerge) {
        String mergedObject = null;
        String preparedToMergeObject;
        try {
            // lock and get the AggregatedObject
            String aggregatedObject = getAggregatedObject(id);
            String mergeRule = getMergeRules(rules);
            if (mergeRule != null && !mergeRule.isEmpty()) {
                String updatedRule = replaceIdMarkerInRules(mergeRule, mergeId);
                String ruleForMerge = jmesPathInterface.runRuleOnEvent(updatedRule, event).toString();
                String mergePath = prepareMergePrepareObject.getMergePath(aggregatedObject, ruleForMerge);
                preparedToMergeObject = prepareMergePrepareObject.addMissingLevels(aggregatedObject,
                                                                                   objectToMerge.toString(),
                                                                                   ruleForMerge, mergePath);
            } else {
                preparedToMergeObject = objectToMerge.toString();
            }

            mergedObject = mergeContentToObject(aggregatedObject, preparedToMergeObject);
            log.debug("Merged Aggregated Object:\n" + mergedObject);
        } catch (Exception e) {
            // TODO: don't catch naked Exception class
            log.info(e.getMessage(), e);
        } finally {
            // unlocking of document will be performed, when mergedObject will be inserted to database
            objectHandler.updateObject(mergedObject, rules, event, id);
        }

        return mergedObject;
    }

    protected String getMergeRules(RulesObject rules) {
        return rules.getMergeRules();
    }

    public String replaceIdMarkerInRules(String rule, String id) {

        if (rule.contains(mergeIdMarker)) {
            String updatedRule = rule.replaceAll(mergeIdMarker, "\"" + id + "\"");
            updatedRule = "`" + updatedRule + "`";
            return updatedRule;
        }
        return rule;
    }

    public String mergeContentToObject(String aggregatedObject, String preparedObject){
        JSONObject aggregatedJsonObject = null;
        try {
            aggregatedJsonObject = new JSONObject(aggregatedObject);
            JSONObject preparedJsonObject = new JSONObject(preparedObject);
            updateJsonObject(aggregatedJsonObject, preparedJsonObject);
        }catch (Exception e){
            log.info(e.getMessage(),e);
        }
        return aggregatedJsonObject == null ? null : aggregatedJsonObject.toString();
    }

    private void updateJsonObject(JSONObject aggregatedJsonObject, JSONObject preparedJsonObject) {
        try {
            Iterator<String> preparedJsonKeys = preparedJsonObject.keys();
            while (preparedJsonKeys.hasNext()) {
                String preparedJsonKey = (String) preparedJsonKeys.next();
                if (aggregatedJsonObject.has(preparedJsonKey)) {
                    Class valueClass = aggregatedJsonObject.get(preparedJsonKey).getClass();
                    if (valueClass.equals(JSONObject.class)) {
                        updateJsonObject((JSONObject) aggregatedJsonObject.get(preparedJsonKey),
                                         (JSONObject) preparedJsonObject.get(preparedJsonKey));
                    } else if (valueClass.equals(JSONArray.class)) {
                        updateJsonObject((JSONArray) aggregatedJsonObject.get(preparedJsonKey),
                                         (JSONArray) preparedJsonObject.get(preparedJsonKey));
                    } else {
                        aggregatedJsonObject.put(preparedJsonKey, preparedJsonObject.get(preparedJsonKey));
                    }
                } else {
                    aggregatedJsonObject.put(preparedJsonKey, preparedJsonObject.get(preparedJsonKey));
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    private void updateJsonObject(JSONArray aggregatedJsonObject, JSONArray preparedJsonObject){
        if (preparedJsonObject.length() > aggregatedJsonObject.length()){
            aggregatedJsonObject.put(new JSONObject());
        }
        for (int i=0; i<preparedJsonObject.length(); i++){
            try {
                if (aggregatedJsonObject.get(i).getClass().equals(JSONObject.class )){
                    updateJsonObject((JSONObject) aggregatedJsonObject.get(i),
                            (JSONObject) preparedJsonObject.get(i));
                }else if(aggregatedJsonObject.get(i).getClass().equals(JSONArray.class )){
                    updateJsonObject((JSONArray) aggregatedJsonObject.get(i),
                            (JSONArray) preparedJsonObject.get(i));
                }else{
                    Object element = aggregatedJsonObject.get(i);
                    element = preparedJsonObject.get(i);
                }
            } catch (JSONException e) {
                log.info(e.getMessage(),e);
            }
        }
    }

    /**
     * This method set lock property in document in database and returns the aggregated document which will be
     * further modified.
     * @param id String to search in database and lock this document.
     */
    public String getAggregatedObject(String id){
        try {
            String document = objectHandler.lockDocument(id);
            JsonNode result = objectHandler.getAggregatedObject(document);
            if (result != null)
                return result.toString();
        }catch (Exception e){
            log.info(e.getMessage(),e);
        }
        return null;
    }

    public void addNewObject(String event, String newObject, RulesObject rulesObject) {
        objectHandler.insertObject(newObject, rulesObject, event, null);
    }

    public void addNewObject(String event, JsonNode newObject, RulesObject rulesObject) {
        objectHandler.insertObject(newObject, rulesObject, event, null);
    }
}
