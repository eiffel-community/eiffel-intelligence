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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.wnameless.json.flattener.JsonFlattener;

import lombok.Setter;

@Component
public class MergePrepare {

    @Setter
    @Autowired
    JmesPathInterface jmesPathInterface;

    private static final Logger LOGGER = LoggerFactory.getLogger(MergePrepare.class);

    public String getValueFromRule(String mergeRule) {
        String ruleValue = "";
        try {
            JSONObject ruleJSONObject = new JSONObject(mergeRule);
            if (ruleJSONObject.keys().hasNext()) {
                String ruleKey = ruleJSONObject.keys().next();
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
                return getValueFromRule(ruleJSONArray.get(1).toString());
            } catch (Exception ne) {
                LOGGER.info("Failed to get value from rule.", ne);
            }
        }
        return ruleValue;
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
        LOGGER.debug("mergeRules are : {}\n originObject is : {}", mergeRule, originObject);
        try {
            JSONArray ruleJSONArray = new JSONArray(mergeRule);
            String firstRule = ruleJSONArray.get(0).toString();
            String secondRule = ruleJSONArray.get(1).toString();
            String firstPath = getMergePath(originObject, firstRule, false);
            String firstPathTrimmed = trimLastInPath(firstPath, ".");

            if (propertyExist(stringObject, firstPathTrimmed, secondRule)) {
                if (!firstPath.isEmpty()) {
                    String firstPathNoIndexes = removeArrayIndexes(firstPath);
                    String[] firstPathSubstrings = firstPathNoIndexes.split("\\.");
                    ArrayList<String> fp = new ArrayList<String>(Arrays.asList(firstPathSubstrings));
                    fp.remove(fp.size() - 1);
                    firstPathTrimmed = StringUtils.join(fp, ":{");
                    String secondRuleComplete = "{" + firstPathTrimmed + ":" + secondRule + "}";
                    for (int i = 1; i < fp.size(); i++) {
                        secondRuleComplete += "}";
                    }

                    return getMergePath(originObject, secondRuleComplete, false);
                } else {
                    return getMergePath(originObject, secondRule, false);
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
        } catch (Exception e) {
            LOGGER.error("Failed to get merge path from merge rules.", e);
        }
        return "";
    }

    public String trimLastInPath(String path, String delimiter) {
        String[] firstPathSubstrings = path.split("\\.");
        ArrayList<String> fp = new ArrayList<String>(Arrays.asList(firstPathSubstrings));
        fp.remove(fp.size() - 1);
        return StringUtils.join(fp, delimiter);
    }

    public String removeArrayIndexes(String path) {
        return StringUtils.removePattern(path, "(\\.0|\\.[1-9][0-9]*)");
    }

    public String makeJmespathArrayIndexes(String path) {
        try {
            String resembled = "";
            JSONArray mergePathArray = new JSONArray(path.split("\\."));
            for (int i = 0; i < mergePathArray.length(); i++) {
                String pathElement = mergePathArray.get(i).toString();

                if (isNumeric(pathElement)) {
                    resembled += "[" + pathElement + "]";
                } else {
                    if (!resembled.isEmpty())
                        resembled += ".";
                    resembled += pathElement;
                }
            }
            return resembled;
        } catch (Exception e) {
            LOGGER.error("Failed to make JMESPath array indexes.", e);
        }

        return path;
    }

    /**
     * Example 1:
     * 
     * originObject: {id: eventId, type: eventType, test_cases:[{event_id:
     * testcaseid1, test_data: testcase1data},{event_id: testcaseid2, test_data:
     * testcase2data}]}
     * 
     * mergeRule: "{event_id: testcaseid2}",
     * 
     * resulting path is : test_cases.1.event_id
     * 
     * Example 2:
     * 
     * originObject: {id:eventId, fakeArray:[{event_id:fakeId,
     * fake_data:also_fake}],
     * level1:{property1:p1value,level2:{property2:p2value,lvl2Array:[{oneElem:oneElemValue}]}},type:eventType,
     * test_cases: [{event_id: testcaseid1, test_data: testcase1data}, {event_id:
     * testcaseid2, test_data: testcase2data}]}
     * 
     * mergeRule: [{property2:p2value},{lvl2Array:[{nextElem:nextElemValue}]}]
     * 
     * resulting path is: level1.level2.lvl2Array.1.nextElem
     * 
     * @param originObject   as a JSON structure
     * @param mergeRule      as a JSON structure
     * @param skipPathSearch
     * @return path in dot notation to an element in the originObject
     */
    public String getMergePath(String originObject, String mergeRule, boolean skipPathSearch) {
        String mergePath = "";
        if (mergeRule == null || mergeRule.isEmpty()) {
            return mergePath;
        }
        String stringObject = "";
        String stringRule = "";
        JSONObject objectJSONObject = null;
        try {
            objectJSONObject = new JSONObject(originObject);
            stringObject = objectJSONObject.toString();
            Object ruleJSONObject;

            // condition to avoid un-necessary exception to print in the log
            if(mergeRule.startsWith("{")) {
            	ruleJSONObject = new JSONObject(mergeRule);
            } else {
            	return getMergePathFromArrayMergeRules(originObject, mergeRule, stringObject);
            }
            // hack to remove quotes
            stringRule = ruleJSONObject.toString();
            // if we have an array with only one JSON object we remove the
            // square brackets
            stringRule = stringRule.replaceAll("\\[\\{", "{");
            stringRule = stringRule.replaceAll("\\}\\]", "}");
        } catch (JSONException e) {
            LOGGER.warn("Failed to parse JSON.", e);
            return getMergePathFromArrayMergeRules(originObject, mergeRule, stringObject);
        }

        // flatten the stringObject to check if it contains parts of the rules
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(stringObject);
        // flatten the rule to check if it matches any key in the aggregated object
        String flattenRule = JsonFlattener.flatten(stringRule);
        String[] rulePair = flattenRule.split(":");
        String ruleValue = rulePair[1];
        ruleValue = destringify(ruleValue);
        String ruleKey = destringify(rulePair[0]);
        // the rule path is split in elements
        String[] ruleKeyFactors = ruleKey.split("\\.");
        String ruleKeyLast = "";
        if (ruleKeyFactors.length > 1) {
            ruleKeyLast = ruleKeyFactors[ruleKeyFactors.length - 1];
            // exclude last element since it will be contained by
            // the sub element to be found
            ruleKeyFactors = Arrays.copyOf(ruleKeyFactors, ruleKeyFactors.length - 1);
        }
        String lastRuleFactor = null;
        if (ruleKeyFactors.length > 0)
            lastRuleFactor = ruleKeyFactors[ruleKeyFactors.length - 1];
        ArrayList<String> pathsWithValue = new ArrayList<String>();
        ArrayList<String> pathsContainingRule = new ArrayList<String>();

        if (skipPathSearch) {
            int pos = ruleKey.lastIndexOf(".");
            if (pos > 0) {
                ruleKey = ruleKey.substring(0, pos);
            }
            JsonNode jsonResult = jmesPathInterface.runRuleOnEvent(ruleKey, originObject);
            if (!(jsonResult instanceof NullNode)) {
                mergePath = ruleKey;
            }
        } else {
            // identify all the paths in the aggregated object containing the merge rule
            // value
            for (Map.Entry<String, Object> entry : flattenJson.entrySet()) {
                String entryKey = entry.getKey();
                Object entryValue = entry.getValue();
                if (entryValue != null && entryValue.equals(ruleValue)) {
                    pathsWithValue.add(destringify(entryKey));
                }

                int factorCount = 0;
                // identify all the paths in the aggregated object containing the elements of
                // the rule path
                for (String factor : ruleKeyFactors) {
                    if (entryKey.contains(factor)) {
                        factorCount++;
                    }
                    if (factorCount == ruleKeyFactors.length) {
                        pathsContainingRule.add(destringify(entryKey));
                    }
                }
            }
        }

        // if only one path contains the merge rule value return it
        if (pathsWithValue.size() == 1) {
            return pathsWithValue.get(0);
        } else if (pathsWithValue.size() > 1) {
            // this situation should not occur but it may
            // and since I do not like to fail we return
            // one of the alternatives.
            String winingPath = "";
            for (String path : pathsWithValue) {
                // return the path that matches the exact merge rule key if possible
                if (path.equals(ruleKey))
                    return path;

                if (path.length() > winingPath.length())
                    winingPath = path;
            }
            // returns the longest path with the merge rule value
            return winingPath;
        } else {
            // if no match for merge rule value we get the longest path
            // containing all the elements in the merge rule key. Formatted
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
                if (longestCommonString.startsWith(".")) {
                    longestCommonString = "";
                }
                mergePath = longestCommonString;
            }

            // clean the path and make it in dot nation
            if (!mergePath.isEmpty()) {
                try {
                    ObjectMapper objectmapper = new ObjectMapper();
                    JsonNode parsedJson = objectmapper.readTree(objectJSONObject.toString());
                    mergePath = "/" + mergePath.replaceAll("\\.", "\\/");
                    Object value = parsedJson.at(mergePath);
                    if (value instanceof ArrayNode) {
                        int arraySize = ((ArrayNode) value).size();
                        mergePath += "." + arraySize++;
                    }
                    if (!StringUtils.isAllBlank(ruleKeyLast)) {
                        mergePath += "." + ruleKeyLast;
                    }

                    mergePath = mergePath.replaceFirst("\\/", "");
                    mergePath = mergePath.replaceAll("\\/", "\\.");
                } catch (Exception e) {
                    LOGGER.error("Failed to parse JSON.", e);
                }
            }
        }
        return mergePath;
    }

    /**
     * This method can not be generalized since it removes the last element in the
     * path before doing the check.
     *
     * @param originObject
     * @param path
     * @param targetObject
     * @return
     */
    public boolean propertyExist(String originObject, String path, String targetObject) {

        JsonNode value = propertyValue(originObject, path, targetObject);
        if (value == null)
            return false;

        return true;
    }

    /**
     * This method can not be generalized since it removes the last element in the
     * path before doing the check.
     *
     * @param originObject
     * @param path
     * @param targetObject
     * @return
     */
    public JsonNode propertyValue(String originObject, String path, String targetObject) {
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
            if (jsonResult == null) {
                LOGGER.warn("Failed to get property from object '{}', result is null."
                        + "\nThis may be cause of none values in the Rules file.", originObject);
                return null;
            }
            return jsonResult.get(firstKey);
        } catch (Exception e) {
            LOGGER.error("Failed to get property value.", e);
        }

        return null;
    }

    /**
     * 
     * objectToMerge needs to be inflated with levels specified by mergePath
     * 
     * 
     * Example 1:
     * 
     * originObject is {level1:{level2:{property1:value1}}}
     * 
     * mergepath is level1.level2.property1;
     * 
     * mergeRule is {level1.property1:value1}
     * 
     * objectToMerge is {property2:value2, property3:value3}
     * 
     * Then the result from the method will be
     * 
     * {level1:{level2:{property1:value1, property2:value2, property3:value3}}}
     * 
     * Example 2:
     * 
     * originObject is {level1:{level2:{property1:value1}}}
     * 
     * mergepath is level1.level2.property1;
     * 
     * mergeRule is [{level1.property1:value1},{propertyArray:[{property3:value3}]}]
     * 
     * Then the result from the method will be
     * 
     * {level1:{level2:{property1:value1, propertyArray:[{property3:value3}]}}}
     * 
     * Example 3:
     * 
     * originObject {level1:{level2:{property1:value1,
     * propertyArray:[{property2:value2}]}}}
     * 
     * mergepath is level1.level2.propertyArray.0.property2
     * 
     * mergeRule is [{level1.property1:value1},{propertyArray:[{property3:value3}]}]
     * 
     * Then the result from the method will be
     * 
     * {level1:{level2:{property1:value1, propertyArray:[{}, {property3:value3}]}}}
     * 
     * @param originObject  the existing aggregated object
     * @param objectToMerge the object to be merged
     * @param mergeRule
     * @param mergePath     the path in dot notation
     * @return
     */
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
            LOGGER.error(
                    "addMissingLevels failed for arguments:\n" + "originObject was : {}\n" + "objectTomerge was: {}\n"
                            + "mergeRule was: {}\n" + "mergePath was: {}\n",
                    originObject, objectToMerge, mergeRule, mergePath, e);
        }
        return newObject.toString();
    }

    private boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    /**
     * @param originObject   the existing aggregated object
     * 
     *                       Example: {id: eventId, type: eventType,
     *                       test_cases:[{event_id: testcaseid1, test_data:
     *                       testcase1data},{event_id: testcaseid2, test_data:
     *                       testcase2data}, {event_id: testcaseid3, test_data:
     *                       testcase3data}]}
     * 
     * @param mergePathArray the final mergePath
     * 
     *                       Example: ["test_cases","2","event_id"]
     * 
     * @param mergePathIndex the index in the mergePathArray of the value giving the
     *                       index of the found array element
     * 
     *                       Example: 1
     * 
     * @param pathElement    the index of the array element found with the rules in
     *                       string format
     * 
     *                       Example: "2"
     * 
     * @return the size of the array pointed by mergePathArray
     * 
     *         Example: 3
     */
    private int getOriginObjectArraySize(String originObject, JSONArray mergePathArray, int mergePathIndex,
            String pathElement) {
        int size = 0;
        try {
            JSONObject originJSONObject = new JSONObject(originObject);
            Object valueForKey = null;
            for (int i = 0; i < mergePathIndex; i++) {
                String key = mergePathArray.get(i).toString();
                if (valueForKey == null && originJSONObject.has(key)) {
                    valueForKey = originJSONObject.get(key);
                } else {
                    if (valueForKey instanceof JSONObject && ((JSONObject) valueForKey).has(key)) {
                        valueForKey = ((JSONObject) valueForKey).get(key);
                    } else if (valueForKey instanceof JSONArray) {
                        valueForKey = ((JSONArray) valueForKey).get(Integer.parseInt(key));
                    }
                }
            }
            if (valueForKey != null && valueForKey.getClass().equals(JSONArray.class)) {
                size = ((JSONArray) valueForKey).length();
                if ((Integer.parseInt(pathElement) + 1) > size) {
                    return Integer.parseInt(pathElement) + 1;
                }
                return size;
            }
        } catch (JSONException e) {
            LOGGER.error("Failed to get object array size.", e);
        }

        return size;
    }
}
