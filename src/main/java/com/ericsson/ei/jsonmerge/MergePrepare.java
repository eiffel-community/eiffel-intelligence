package com.ericsson.ei.jsonmerge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MergePrepare {

    static Logger log = (Logger) LoggerFactory.getLogger(MergePrepare.class);

    public String getKeyFromRule(String mergeRule){
        String ruleKey = "";
        try {
            JSONObject ruleJSONObject = new JSONObject(mergeRule);
            if (ruleJSONObject.keys().hasNext()){
                ruleKey = (String) ruleJSONObject.keys().next();
                if (ruleJSONObject.get(ruleKey).getClass() == JSONObject.class)
                    ruleKey = new StringBuilder().append(ruleKey).
                            append('.').append(getKeyFromRule(ruleJSONObject.
                            get(ruleKey).toString())).toString();
            }
        }catch (Exception e){
            log.info(e.getMessage(),e);
        }
        return ruleKey;
    }

    public String getValueFromRule(String mergeRule){
        String ruleValue = "";
        try {
            JSONObject ruleJSONObject = new JSONObject(mergeRule);
            if (ruleJSONObject.keys().hasNext()){
                String ruleKey = (String) ruleJSONObject.keys().next();
                if (ruleJSONObject.get(ruleKey).getClass() == JSONObject.class)
                    return getValueFromRule(ruleJSONObject.get(ruleKey).toString());
                return ruleJSONObject.getString(ruleKey);
            }
        }catch (Exception e){
            log.info(e.getMessage(),e);
        }
        return ruleValue;
    }

    public String getMergePath(String originObject, String mergeRule){
        String mergePath = "";
        try {
            String ruleKey = "";
            try{
                JSONObject originJSONObject = new JSONObject(originObject);
                JSONObject ruleJSONObject = new JSONObject(mergeRule);
                if (ruleJSONObject.keys().hasNext()){
                    ruleKey = (String) ruleJSONObject.keys().next();
                }
                Iterator <String> originObjectKeys = originJSONObject.keys();
                while(originObjectKeys.hasNext()) {
                    String originObjectKey = originObjectKeys.next();
                    if (originObjectKey.equals(ruleKey)){
                        if (ruleJSONObject.get(ruleKey).getClass().
                                equals(String.class)){
                            if (ruleJSONObject.get(ruleKey).
                                    equals(originJSONObject.get(originObjectKey))){
                                return originObjectKey;
                            }
                        } else {
                            return originObjectKey + '.' +
                                    getMergePath(originJSONObject.
                                                    get(originObjectKey).toString(),
                                            ruleJSONObject.get(ruleKey).toString());
                        }
                    } else{
                        mergePath = getMergePath(originJSONObject.get(originObjectKey).
                                toString(), mergeRule);
                        if (!mergePath.isEmpty()){
                            return originObjectKey + '.' + mergePath;
                        }
                    }
                }
            } catch (JSONException JSONObjectException){
                try {
                    JSONArray originJSONArray = new JSONArray(originObject);
                    int i;
                    for (i = 0; i < originJSONArray.length(); i++) {
                        mergePath = getMergePath(originJSONArray.get(i).
                                toString(), mergeRule);
                        if (!mergePath.isEmpty()) {
                            return Integer.toString(i) + '.' + mergePath;
                        }
                    }
                    ruleKey = (String) getKeyFromRule(mergeRule.toString());
                    if (!ruleKey.isEmpty()) {
                        return Integer.toString(i) + '.' + ruleKey;
                    }
                }catch (JSONException JSONArrayException) {
                    return mergePath;
                }
            }
        } catch (Exception e){
            log.info(e.getMessage(),e);
        }
        return mergePath;
    }

    public String addMissingLevels (String originObject, String objectToMerge,
                                    String mergeRule, String mergePath) {
        JSONObject newObject = new JSONObject();
        try {
            JSONArray mergePathArray = new JSONArray(mergePath.split("\\."));
            JSONObject mergeObject = new JSONObject(objectToMerge);
            String ruleKey = (String) mergePathArray.get(mergePathArray.length()-1);
            String ruleValue = getValueFromRule(mergeRule);
            mergeObject.put(ruleKey, ruleValue);
            for (int i = 1; i < mergePathArray.length(); i++) {
                int mergePathIndex = mergePathArray.length() - (1 + i);
                String pathElement = mergePathArray.get(mergePathIndex).toString();
                if (isNumeric(pathElement)){
                    int arraySize = getOriginObjectArraySize(originObject, mergePathArray, mergePathIndex, pathElement);
                    JSONArray mergeArray = new JSONArray();
                    for (int k = 0; k < arraySize; k++) {
                        if (k == Integer.parseInt(pathElement)){
                            mergeArray.put(mergeObject);
                        }else{
                            mergeArray.put(new JSONObject());
                        }
                    }
                    i++;
                    pathElement = mergePathArray.get(mergePathArray.length() - (1 + i)).toString();
                    newObject.put(pathElement, mergeArray);
                    mergeObject = newObject;
                }else{
                    newObject.put(pathElement, mergeObject);
                    mergeObject = newObject;
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
        return newObject.toString();
    }

    private boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    private int getOriginObjectArraySize(String originObject,
                                         JSONArray mergePathArray,
                                         int mergePathIndex,
                                         String pathElement){
        int size = 0;
        try {
            JSONObject originJSONObject = new JSONObject(originObject);
            for (int i = 0; i <= mergePathIndex; i++) {
                String key = mergePathArray.getString(i);
                if (originJSONObject.get(key).getClass().equals(JSONArray.class)) {
                    size = ((JSONArray) originJSONObject.get(key)).length();
                    if ((Integer.parseInt(pathElement) + 1) > size){
                        return Integer.parseInt(pathElement) + 1;
                    }
                    return size;
                }
            }
        }catch (JSONException e){
            log.info(e.getMessage(),e);
        }

        return size;
    }
}
