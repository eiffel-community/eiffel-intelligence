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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.wnameless.json.flattener.JsonFlattener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class MergePrepare {

	private static Logger log = (Logger) LoggerFactory.getLogger(MergePrepare.class);

	public String getKeyFromRule(String mergeRule) {
		String ruleKey = "";
		try {
			JSONObject ruleJSONObject = new JSONObject(mergeRule);
			if (ruleJSONObject.keys().hasNext()) {
				ruleKey = (String) ruleJSONObject.keys().next();
				if (ruleJSONObject.get(ruleKey).getClass() == JSONObject.class) {
					ruleKey = ruleKey + '.' + getKeyFromRule(ruleJSONObject.get(ruleKey).toString());
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage(), e);
		}
		return ruleKey;
	}

	public String getValueFromRule(String mergeRule) {
		String ruleValue = "";
		try {
			JSONObject ruleJSONObject = new JSONObject(mergeRule);
			if (ruleJSONObject.keys().hasNext()) {
				String ruleKey = (String) ruleJSONObject.keys().next();
				if (ruleJSONObject.get(ruleKey).getClass() == JSONObject.class) {
					return getValueFromRule(ruleJSONObject.get(ruleKey).toString());
				}
				return ruleJSONObject.getString(ruleKey);
			}
		} catch (Exception e) {
			log.info(e.getMessage(), e);
		}
		return ruleValue;
	}

	public String getMergePathOld(String originObject, String mergeRule) {
		String mergePath = "";
		try {
			String ruleKey = "";
			try {
				JSONObject originJSONObject = new JSONObject(originObject);
				JSONObject ruleJSONObject = new JSONObject(mergeRule);
				if (ruleJSONObject.keys().hasNext()) {
					ruleKey = (String) ruleJSONObject.keys().next();
				}
				Iterator<String> originObjectKeys = originJSONObject.keys();
				while (originObjectKeys.hasNext()) {
					String originObjectKey = originObjectKeys.next();
					if (originObjectKey.equals(ruleKey)) {
						if (ruleJSONObject.get(ruleKey).getClass().equals(String.class)) {
							if (ruleJSONObject.get(ruleKey).equals(originJSONObject.get(originObjectKey))) {
								return originObjectKey;
							}
						} else {
							return originObjectKey + '.'
									+ getMergePathOld(originJSONObject.get(originObjectKey).toString(),
											ruleJSONObject.get(ruleKey).toString());
						}
					} else {
						Object keyObject = originJSONObject.get(originObjectKey);
						// if (keyObject instanceof JSONObject) {
						mergePath = getMergePathOld(keyObject.toString(), mergeRule);
						if (!mergePath.isEmpty() && pathContainsMergeRule(mergePath, mergeRule)) {
							return originObjectKey + '.' + mergePath;
							// }
						}
					}
				}
			} catch (JSONException JSONObjectException) {
				try {
					JSONArray originJSONArray = new JSONArray(originObject);
					int i;
					for (i = 0; i < originJSONArray.length(); i++) {
						mergePath = getMergePathOld(originJSONArray.get(i).toString(), mergeRule);
						if (!mergePath.isEmpty()) {
							return Integer.toString(i) + '.' + mergePath;
						}
					}
					ruleKey = getKeyFromRule(mergeRule);
					if (!ruleKey.isEmpty()) {
						return Integer.toString(i) + '.' + ruleKey;
					}
				} catch (JSONException JSONArrayException) {
					return mergePath;
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage(), e);
		}
		return mergePath;
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
		if (ruleKeyFactors.length == 0 && !rulePair[0].isEmpty()) {
			ruleKeyFactors = new String[] { rulePair[0] };
		}
		for (String factor : ruleKeyFactors) {
			int count = 0;
			if (path.contains(factor)) {
				count++;
			}
			if (count == ruleKeyFactors.length) {
				return true;
			}
		}
		return false;
	}

	public static String destringify(String str) {
		str = str.replaceAll("\"", "");
		str = str.replaceAll("\\{", "");
		str = str.replaceAll("}", "");
		str = str.replaceAll("]", "");
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
					if (i + x >= s1.length() || j + x >= s2.length()) {
						break;
					}
				}
				if (x > max) {
					max = x;
					start = i;
				}
			}
		}
		return s1.substring(start, (start + max));
	}

	public String getMergePath(String originObject, String mergeRule) {
		String mergePath = "";
		String stringObject = "";
		String stringRule = "";
		JSONObject objectJSONObject = null;
		try {
			objectJSONObject = new JSONObject(originObject);
			stringObject = objectJSONObject.toString();
			JSONObject ruleJSONObject = new JSONObject(mergeRule);
			stringRule = ruleJSONObject.toString();
		} catch (JSONException e) {
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
		if (ruleKeyFactors.length > 0) {
			lastRuleFactor = ruleKeyFactors[ruleKeyFactors.length - 1];
		}
		List<String> pathsWithValue = new ArrayList<>();
		Map<String, String[]> commonRuleStrings = new HashMap<>();
		List<String> pathsContainingRule = new ArrayList<>();

		for (Map.Entry<String, Object> entry : flattenJson.entrySet()) {
			String entryKey = entry.getKey();
			Object entryValue = entry.getValue();
			if (entryValue != null && entryValue.equals(ruleValue)) {
				pathsWithValue.add(destringify(entryKey));
			}

			int factorCount = 0;
			for (String factor : ruleKeyFactors) {
				// if (entryKey.endsWith(lastRuleFactor)) {

				if (entryKey.contains(factor)) {
					factorCount++;
				}
				if (factorCount == ruleKeyFactors.length) {
					pathsContainingRule.add(destringify(entryKey));
				}
				// }
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
				if (path.length() > winingPath.length()) {
					winingPath = path;
				}
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
				if (longestCommonString.startsWith(".")) {
					longestCommonString = "";
				}
				// remove index at the end
				String pattern = "\\.\\d*$";
				longestCommonString = longestCommonString.replaceAll(pattern, "");
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
						mergePath += "." + arraySize + "." + ruleKeyLast;
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

	public String addMissingLevels(String originObject, String objectToMerge, String mergeRule, String mergePath) {
		if (mergePath.isEmpty()) {
			return objectToMerge;
		}

		JSONObject newObject = new JSONObject();
		try {
			JSONArray mergePathArray = new JSONArray(mergePath.split("\\."));
			JSONObject mergeObject = new JSONObject(objectToMerge);
			if (!mergeRule.isEmpty()) {
				String ruleKey = (String) mergePathArray.get(mergePathArray.length() - 1);
				String ruleValue = getValueFromRule(mergeRule);
				mergeObject.put(ruleKey, ruleValue);
			}

			if (mergePathArray.length() == 1) {
				return mergeObject.toString();
			}

			for (int i = 1; i < mergePathArray.length(); i++) {
				int mergePathIndex = mergePathArray.length() - (1 + i);
				String pathElement = mergePathArray.get(mergePathIndex).toString();
				if (isNumeric(pathElement)) {
					int arraySize = getOriginObjectArraySize(originObject, mergePathArray, mergePathIndex, pathElement);
					JSONArray mergeArray = new JSONArray();
					for (int k = 0; k < arraySize; k++) {
						if (k == Integer.parseInt(pathElement)) {
							mergeArray.put(mergeObject);
						} else {
							mergeArray.put(new JSONObject());
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
