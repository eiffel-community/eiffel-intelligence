package com.ericsson.ei.jsonmerge;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesObject;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MergeHandler {

    static Logger log = (Logger) LoggerFactory.getLogger(MergeHandler.class);


    @Value("${mergeidmarker}") private String mergeIdMarker;

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private MergePrepare prepareMergePrepareObject;


    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmesPathInterface = jmesPathInterface;
    }

    public void setPrepareMergePrepareObject(MergePrepare prepareMergePrepareObject) {
        this.prepareMergePrepareObject = prepareMergePrepareObject;
    }

    public void setMergeIdMarker(String marker) {
        mergeIdMarker = marker;
    }

    public String mergeObject(String id, RulesObject rules, String event, JsonNode objectToMerge){
        String mergedObject = null;
        String preparedToMergeObject;
        try{
            String aggregatedObject = (String) getAggregatedObject(id);
            String mergeRule = (String) rules.getMergeRules();
            if (mergeRule != null && !mergeRule.isEmpty()){
                String updatedRule = (String) replaceIdMarkerInRules(mergeRule, id);
                String ruleForMerge = jmesPathInterface.runRuleOnEvent(updatedRule, event).toString();
                String mergePath = (String) prepareMergePrepareObject.getMergePath(aggregatedObject, ruleForMerge);
                preparedToMergeObject = (String) prepareMergePrepareObject.addMissingLevels(aggregatedObject,
                        objectToMerge.toString(), ruleForMerge, mergePath);
            }else{
                preparedToMergeObject = objectToMerge.toString();
            }
            mergedObject = (String) mergeContentToObject(aggregatedObject, preparedToMergeObject);
        }catch (Exception e){
            log.info(e.getMessage(),e);
        }
        return mergedObject;
    }

    public String replaceIdMarkerInRules(String rule, String id){
        String literal = "`\"" + id + "\"`";
        String updatedRule = rule.replaceAll(mergeIdMarker, literal);
        return updatedRule;
    }

    public String mergeContentToObject(String aggregatedObject, String preparedObject){
        try {
            JSONObject agregatedJsonObject = new JSONObject(aggregatedObject);
            JSONObject preparedJsonObject = new JSONObject(preparedObject);
            Iterator <String> preparedJsonKeys = preparedJsonObject.keys();
            while(preparedJsonKeys.hasNext()) {
                String preparedJsonKey = (String) preparedJsonKeys.next();
                if (agregatedJsonObject.get(preparedJsonKey).getClass().equals(JSONObject.class )){
                    updateJsonObject((JSONObject) agregatedJsonObject.get(preparedJsonKey),
                            (JSONObject) preparedJsonObject.get(preparedJsonKey));
                }else if(agregatedJsonObject.get(preparedJsonKey).getClass().equals(JSONArray.class )){
                    updateJsonObject((JSONArray) agregatedJsonObject.get(preparedJsonKey),
                            (JSONArray) preparedJsonObject.get(preparedJsonKey));
                }else{
                    agregatedJsonObject.put(preparedJsonKey, preparedJsonObject.get(preparedJsonKey));
                }
            }
            return agregatedJsonObject.toString();
        }catch (Exception e){
            log.info(e.getMessage(),e);
        }
        return null;

    }
    private void updateJsonObject(JSONObject aggregatedJsonObject, JSONObject preparedJsonObject){
        Iterator <String> preparedJsonKeys = preparedJsonObject.keys();
        while(preparedJsonKeys.hasNext()) {
            String preparedJsonKey = preparedJsonKeys.next();
            try {
                if (preparedJsonObject.get(preparedJsonKey).getClass().equals(JSONObject.class )){
                    updateJsonObject((JSONObject) aggregatedJsonObject.get(preparedJsonKey),
                            (JSONObject) preparedJsonObject.get(preparedJsonKey));
                }else if(preparedJsonObject.get(preparedJsonKey).getClass().equals(JSONArray.class )){
                    updateJsonObject((JSONArray) aggregatedJsonObject.get(preparedJsonKey),
                            (JSONArray) preparedJsonObject.get(preparedJsonKey));
                }else{
                    aggregatedJsonObject.put(preparedJsonKey, preparedJsonObject.get(preparedJsonKey));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
   public String getAggregatedObject(String id){
        try {
            //  Method fetches the aggregated object from database based on given id
            String aggregatedObject = new String("{id:eventId,type:eventType,test_cases:[{event_id:testcaseid1,test_data:testcase1data},{event_id:`testcaseid2`,test_data:testcase2data}]}");
            return aggregatedObject;
        }catch (Exception e){
            log.info(e.getMessage(),e);
        }
        return null;
    }
}
