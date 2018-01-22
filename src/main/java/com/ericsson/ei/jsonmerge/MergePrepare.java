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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.wnameless.json.flattener.JsonFlattener;

import lombok.Setter;

import java.util.*;

@Component
public class MergePrepare {

    @Setter
    @Autowired
    JmesPathInterface jmesPathInterface;

    static Logger log = (Logger) LoggerFactory.getLogger(MergePrepare.class);

    public String getValueFromRule(String mergeRule) {
        String ruleValue = "";
        try {
            JSONObject ruleJSONObject = new JSONObject(mergeRule);
            if (ruleJSONObject.keys().hasNext()) {
                String ruleKey = (String) ruleJSONObject.keys().next();
                Object value = ruleJSONObject.get(ruleKey);
                if (value.getClass() == JSONObject.class)
                    return getValueFromRule(value.toString());
                else if (value.getClass() == JSONArray.class) {
                    return getValueFromRule(((JSONArray) value).get(0).toString());
                }
                return ruleJSONObject.getString(ruleKey);
            }
        } catch (JSONException e) {
            try {
                JSONArray ruleJSONArray = new JSONArray(mergeRule);
                return getValueFromRule(ruleJSONArray.getString(1));
            } catch (Exception ne) {
                log.info(ne.getMessage(), ne);
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return ruleValue;
    }

    public boolean pathContainsMergeRule(String path, String mergeRule) {
        String stringRule = "";
        try {
            JSONObject ruleJSONObject = new JSONObject(mergeRule);
            stringRule = ruleJSONObject.toString();
        } catch (JSONException e) {
            log.info(e.getMessage(), e);
        }
        String flattenRule = JsonFlattener.flatten(stringRule);
        flattenRule = destringify(flattenRule);
        String[] rulePair = flattenRule.split(":");
        String[] ruleKeyFactors = rulePair[0].split(".");
        if (ruleKeyFactors.length == 0 && !rulePair[0].isEmpty())
            ruleKeyFactors = new String[] { rulePair[0] };
        for (String factor : ruleKeyFactors) {
            int count = 0;
            if (path.contains(factor))
                count++;
            if (count == ruleKeyFactors.length)
                return true;
        }
        return false;
    }

    public static String destringify(String str) {
        str = str.replaceAll("\"", "");
        str = str.replaceAll("\\{", "");
        str = str.replaceAll("\\}", "");
        str = str.replaceAll("\\]", "");
        str = str.replaceAll("\\[", ".");
        return str;
    }

    public static String longestCommonSubstring(String s1, String s2) {
        int start = 0;
        int max = 0;
        for (int i = 0; i < s1.length(); i++) {
            for (int j = 0; j < s2.length(); j++) {
                int x = 0;
                while (s1.charAt(i + x) == s2.charAt(j + x)) {
                    x++;
                    if (((i + x) >= s1.length()) || ((j + x) >= s2.length()))
                        break;
                }
                if (x > max) {
                    max = x;
                    start = i;
                }
            }
        }
        return s1.substring(start, (start + max));
    }

    // TODO fix so that we do not need to pass both originObject and
    // stringObject which are
    // different representations of the same object.
    public String getMergePathFromArrayMergeRules(String originObject, String mergeRule, String stringObject) {
        try {
            JSONArray ruleJSONArray = new JSONArray(mergeRule);
            String firstRule = ruleJSONArray.getString(0);
            String secondRule = ruleJSONArray.getString(1);
            String firstPath = getMergePath(originObject, firstRule);
            String firstPathTrimmed = trimLastInPath(firstPath, ".");

            if (propertyExist(stringObject, firstPathTrimmed, secondRule)) {
                if (!firstPath.isEmpty()) {
                    String firstPathNoIndexes = StringUtils.removePattern(firstPath, "(\\.0|\\.[1-9][0-9]*)");
                    String[] firstPathSubstrings = firstPathNoIndexes.split("\\.");
                    ArrayList<String> fp = new ArrayList(Arrays.asList(firstPathSubstrings));
                    fp.remove(fp.size() - 1);
                    firstPathTrimmed = StringUtils.join(fp, ":{");
                    String secondRuleComplete = "{" + firstPathTrimmed + ":" + secondRule + "}";
                    for (int i = 1; i < fp.size(); i++) {
                        secondRuleComplete += "}";
                    }

                    return getMergePath(originObject, secondRuleComplete);
                } else {
                    return getMergePath(originObject, secondRule);
                }
            } else {
                String flattenRule = JsonFlattener.flatten(secondRule);
                String[] rulePair = flattenRule.split(":");
                String ruleKey = destringify(rulePair[0]);
                if (firstPathTrimmed.isEmpty()) {
                    return ruleKey;
                }
                String finalPath = firstPathTrimmed + "." + ruleKey;
                return finalPath;
            }
        } catch (Exception ne) {
            log.info(ne.getMessage(), ne);
        }
        return "";
    }

    public String trimLastInPath(String path, String delimiter) {
        String[] firstPathSubstrings = path.split("\\.");
        ArrayList<String> fp = new ArrayList(Arrays.asList(firstPathSubstrings));
        fp.remove(fp.size() - 1);
        return StringUtils.join(fp, delimiter);
    }

    public String getMergePath(String originObject, String mergeRule) {
        String mergePath = "";
        String stringObject = "";
        String stringRule = "";
        JSONObject objectJSONObject = null;
        try {
            objectJSONObject = new JSONObject(originObject);
            stringObject = objectJSONObject.toString();
            Object ruleJSONObject = new JSONObject(mergeRule);
            // hack to remove quotes
            stringRule = ruleJSONObject.toString();
            stringRule = stringRule.replaceAll("\\[\\{", "{");
            stringRule = stringRule.replaceAll("\\}\\]", "}");
        } catch (JSONException e) {
            return getMergePathFromArrayMergeRules(originObject, mergeRule, stringObject);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(stringObject);
        String flattenRule = JsonFlattener.flatten(stringRule);
        String[] rulePair = flattenRule.split(":");
        String ruleValue = rulePair[1];
        ruleValue = destringify(ruleValue);
        String ruleKey = destringify(rulePair[0]);
        String[] ruleKeyFactors = ruleKey.split("\\.");
        String ruleKeyLast = "";
        if (ruleKeyFactors.length > 1) {
            ruleKeyLast = ruleKeyFactors[ruleKeyFactors.length - 1];
            // exclude last element
            ruleKeyFactors = Arrays.copyOf(ruleKeyFactors, ruleKeyFactors.length - 1);
        }
        String lastRuleFactor = null;
        if (ruleKeyFactors.length > 0)
            lastRuleFactor = ruleKeyFactors[ruleKeyFactors.length - 1];
        ArrayList<String> pathsWithValue = new ArrayList<String>();
        ArrayList<String> pathsContainingRule = new ArrayList<String>();

        for (Map.Entry<String, Object> entry : flattenJson.entrySet()) {
            String entryKey = entry.getKey();
            Object entryValue = entry.getValue();
            if (entryValue != null && entryValue.equals(ruleValue)) {
                pathsWithValue.add(destringify(entryKey));
            }

            int factorCount = 0;
            for (String factor : ruleKeyFactors) {
                if (entryKey.contains(factor)) {
                    factorCount++;
                }
                if (factorCount == ruleKeyFactors.length) {
                    pathsContainingRule.add(destringify(entryKey));
                }
            }
        }

        if (pathsWithValue.size() == 1) {
            return pathsWithValue.get(0);
        } else if (pathsWithValue.size() > 1) {
            // this situation should not occur but it may
            // and since I do not like to fail we return
            // one of the alternatives.
            String winingPath = "";
            for (String path : pathsWithValue) {
                if (path.equals(ruleKey))
                    return path;
                if (path.length() > winingPath.length())
                    winingPath = path;
            }
            return winingPath;
        } else {
            if (pathsContainingRule.size() == 1) {
                mergePath = pathsContainingRule.get(0);
                // we need to cut away all properties after last level
                String[] mergeParts = mergePath.split(lastRuleFactor);
                mergePath = mergeParts[0] + lastRuleFactor;
            } else if (pathsContainingRule.size() > 1) {
                String longestCommonString = "";
                for (int index = 0; index < pathsContainingRule.size() - 1; index++) {
                    if (index == 0) {
                        String s1 = pathsContainingRule.get(index);
                        String s2 = pathsContainingRule.get(index + 1);
                        longestCommonString = longestCommonSubstring(s1, s2);
                    } else {
                        String s1 = pathsContainingRule.get(index + 1);
                        longestCommonString = longestCommonSubstring(s1, longestCommonString);
                    }
                }
                if (longestCommonString.endsWith(".")) {
                    longestCommonString = longestCommonString.substring(0, longestCommonString.length() - 1);
                }
                // remove index at the end
                String pattern = "\\.\\d*$";
                longestCommonString = longestCommonString.replaceAll(pattern, "");
                // if (longestCommonString.matches(".*\\.0")) {
                // longestCommonString = longestCommonString.substring(0,
                // longestCommonString.length() - 2);
                // int breakHere = 0;
                // }
                if (longestCommonString.startsWith(".")) {
                    longestCommonString = "";
                }
                mergePath = longestCommonString;
            }

            if (!mergePath.isEmpty()) {
                try {
                    ObjectMapper objectmapper = new ObjectMapper();
                    JsonNode parsedJson = objectmapper.readTree(objectJSONObject.toString());
                    // Object value = objectJSONObject.get(mergePath);
                    mergePath = "/" + mergePath.replaceAll("\\.", "\\/");
                    Object value = parsedJson.at(mergePath);
                    if (value instanceof ArrayNode) {
                        int arraySize = ((ArrayNode) value).size();
                        mergePath += "." + arraySize++ + "." + ruleKeyLast;
                    } else {
                        mergePath += "." + ruleKeyLast;
                    }
                    mergePath = mergePath.replaceFirst("\\/", "");
                    mergePath = mergePath.replaceAll("\\/", "\\.");

                } catch (Exception e) {
                    log.info(e.getMessage(), e);
                }
            }
        }
        return mergePath;
    }

    /**
     * This method can not be generalized since it removes the last element in
     * the path before doing the check.
     * 
     * @param originObject
     * @param path
     * @param targetObject
     * @return
     */
    public boolean propertyExist(String originObject, String path, String targetObject) {
        String fixedPath = path;
        if (path != null) {
            fixedPath = path.replaceAll("(\\.0|\\.[1-9][0-9]*)", "[$1]");
            fixedPath = fixedPath.replaceAll("\\[\\.", "[");
        }

        try {
            String firstKey = destringify(targetObject.split(":")[0]);
            JsonNode jsonResult = null;
            ObjectMapper objectMapper = new ObjectMapper();
            if (path.isEmpty()) {
                jsonResult = objectMapper.readTree(originObject);
            } else {
                jsonResult = jmesPathInterface.runRuleOnEvent(fixedPath, originObject);
            }
            JsonNode value = jsonResult.get(firstKey);
            if (value == null)
                return false;
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

        return true;
    }

    public String addMissingLevels(String originObject, String objectToMerge, String mergeRule, String mergePath) {

        JSONObject newObject = new JSONObject();
        try {
            JSONArray mergePathArray = new JSONArray(mergePath.split("\\."));
            JSONObject mergeObject = new JSONObject(objectToMerge);
            if (!mergePath.isEmpty() && !mergeRule.isEmpty()) {
                String ruleKey = (String) mergePathArray.get(mergePathArray.length() - 1);
                String ruleValue = getValueFromRule(mergeRule);
                mergeObject.put(ruleKey, ruleValue);
            }

            if (mergePathArray.length() == 1)
                return mergeObject.toString();

            for (int i = 1; i < mergePathArray.length(); i++) {
                int mergePathIndex = mergePathArray.length() - (1 + i);
                String pathElement = mergePathArray.get(mergePathIndex).toString();
                if (isNumeric(pathElement)) {
                    int index = Integer.parseInt(pathElement);
                    int arraySize = getOriginObjectArraySize(originObject, mergePathArray, mergePathIndex, pathElement);
                    JSONArray mergeArray = new JSONArray();
                    if (arraySize == 0 && index == 0) {
                        mergeArray.put(mergeObject);
                    } else {
                        for (int k = 0; k < arraySize; k++) {
                            if (k == Integer.parseInt(pathElement)) {
                                mergeArray.put(mergeObject);
                            } else {
                                mergeArray.put(new JSONObject());
                            }
                        }
                    }
                    i++;
                    pathElement = mergePathArray.get(mergePathArray.length() - (1 + i)).toString();
                    newObject = new JSONObject();
                    newObject.put(pathElement, mergeArray);
                    mergeObject = newObject;
                } else {
                    newObject = new JSONObject();
                    newObject.put(pathElement, mergeObject);
                    mergeObject = newObject;
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return newObject.toString();
    }

    private boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    private int getOriginObjectArraySize(String originObject, JSONArray mergePathArray, int mergePathIndex,
            String pathElement) {
        int size = 0;
        try {
            JSONObject originJSONObject = new JSONObject(originObject);
            Object valueForKey = null;
            for (int i = 0; i < mergePathIndex; i++) {
                String key = mergePathArray.getString(i);
                if (valueForKey == null) {
                    valueForKey = originJSONObject.get(key);
                } else {
                    if (valueForKey instanceof JSONObject) {
                        valueForKey = ((JSONObject) valueForKey).get(key);
                    } else if (valueForKey instanceof JSONArray) {
                        valueForKey = ((JSONArray) valueForKey).get(Integer.parseInt(key));
                    }
                }
            }
            if (valueForKey.getClass().equals(JSONArray.class)) {
                size = ((JSONArray) valueForKey).length();
                if ((Integer.parseInt(pathElement) + 1) > size) {
                    return Integer.parseInt(pathElement) + 1;
                }
                return size;
            }
        } catch (JSONException e) {
            log.info(e.getMessage(), e);
        }

        return size;
    }
}
