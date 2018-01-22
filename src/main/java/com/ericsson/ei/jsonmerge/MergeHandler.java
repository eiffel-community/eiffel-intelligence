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

	@Value("${mergeidmarker}")
	private String mergeIdMarker;

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
			String aggregatedObject = getAggregatedObject(id, true);
			String mergeRule = getMergeRules(rules);
			if (mergeRule != null && !mergeRule.isEmpty()) {
				String updatedRule = replaceIdMarkerInRules(mergeRule, mergeId);
				String ruleForMerge = jmesPathInterface.runRuleOnEvent(updatedRule, event).toString();
				String mergePath = prepareMergePrepareObject.getMergePath(aggregatedObject, ruleForMerge);
				preparedToMergeObject = prepareMergePrepareObject.addMissingLevels(aggregatedObject,
						objectToMerge.toString(), ruleForMerge, mergePath);
			} else {
				preparedToMergeObject = objectToMerge.toString();
			}

			System.out.println("PREPARED OBJECT: " + preparedToMergeObject);

			mergedObject = mergeContentToObject(aggregatedObject, preparedToMergeObject);
			log.debug("Merged Aggregated Object:\n" + mergedObject);
		} catch (Exception e) {
			// TODO: don't catch naked Exception class
			log.info(e.getMessage(), e);
		} finally {
			// unlocking of document will be performed, when mergedObject will
			// be inserted to database
			objectHandler.updateObject(mergedObject, rules, event, id);
		}

		return mergedObject;
	}

	public String mergeObject(String id, String mergeId, RulesObject rules, String event, JsonNode objectToMerge,
			String mergePath) {
		String mergedObject = null;
		String preparedToMergeObject;
		try {
			// lock and get the AggregatedObject
			String aggregatedObject = getAggregatedObject(id, true);
			System.out.println("\n\n\n");
			System.out.println("should merge at: " + mergePath);
			System.out.println("BEFORE:");
			System.out.println(aggregatedObject);

			// String mergeRule = getMergeRules(rules);
			if (mergePath != null && !mergePath.isEmpty()) {
				preparedToMergeObject = prepareMergePrepareObject.addMissingLevels(aggregatedObject,
						objectToMerge.toString(), "", mergePath);
			} else {
				preparedToMergeObject = objectToMerge.toString();
			}

			System.out.println("PREPARED OBJECT: " + preparedToMergeObject);

			mergedObject = mergeContentToObject(aggregatedObject, preparedToMergeObject);
			log.debug("Merged Aggregated Object:\n" + mergedObject);
		} catch (Exception e) {
			// TODO: don't catch naked Exception class
			log.info(e.getMessage(), e);
		} finally {
			System.out.println("AFTER:");
			System.out.println(mergedObject);
			System.out.println("\n\n\n");
			// unlocking of document will be performed, when mergedObject will
			// be inserted to database
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

	public String mergeContentToObject(String aggregatedObject, String preparedObject) {
		JSONObject aggregatedJsonObject = null;
		try {
			aggregatedJsonObject = new JSONObject(aggregatedObject);
			JSONObject preparedJsonObject = new JSONObject(preparedObject);
			updateJsonObject(aggregatedJsonObject, preparedJsonObject);
		} catch (Exception e) {
			log.info(e.getMessage(), e);
		}
		return aggregatedJsonObject == null ? null : aggregatedJsonObject.toString();
	}

	private void updateJsonObject(JSONObject aggregatedJsonObject, JSONObject preparedJsonObject) {
		try {
			Iterator<String> preparedJsonKeys = preparedJsonObject != null ? preparedJsonObject.keys()
					: new JSONObject().keys();

			System.out.println("PREPARED OBJECT BEFORE WHILE: " + preparedJsonObject);
			while (preparedJsonKeys.hasNext()) {
				String preparedJsonKey = preparedJsonKeys.next();
				System.out.println("PREPARED KEY: " + preparedJsonKey);
				if (aggregatedJsonObject.has(preparedJsonKey)) {
					Class valueClass = aggregatedJsonObject.get(preparedJsonKey).getClass();
					if (valueClass.equals(JSONObject.class)) {
						updateJsonObject((JSONObject) aggregatedJsonObject.get(preparedJsonKey),
								(JSONObject) preparedJsonObject.get(preparedJsonKey));
						// final JSONObject o1 = getAsT(aggregatedJsonObject,
						// preparedJsonKey, JSONObject.class);
						// final JSONObject o2 = getAsT(preparedJsonObject,
						// preparedJsonKey, JSONObject.class);
						// updateJsonObject(o1, o2);
					} else if (valueClass.equals(JSONArray.class)) {
						updateJsonObject((JSONArray) aggregatedJsonObject.get(preparedJsonKey),
								(JSONArray) preparedJsonObject.get(preparedJsonKey));
						// final JSONArray a1 = getAsT(aggregatedJsonObject,
						// preparedJsonKey, JSONArray.class);
						// final JSONArray a2 = getAsT(preparedJsonObject,
						// preparedJsonKey, JSONArray.class);
						// updateJsonObject(a1, a2);
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

	private void updateJsonObject(JSONArray aggregatedJsonObject, JSONArray preparedJsonObject) {
		if (preparedJsonObject.length() > aggregatedJsonObject.length()) {
			aggregatedJsonObject.put(new JSONObject());
		}
		for (int i = 0; i < preparedJsonObject.length(); i++) {
			try {
				final Object eFromAgg = aggregatedJsonObject.get(i);
				final Object eFromPrep = preparedJsonObject.get(i);
				if (eFromAgg instanceof JSONObject) {
					System.out.println("INDEX: " + i + " in prep array: " + preparedJsonObject);
					System.out.println("INDEX: " + i + " in agg array: " + aggregatedJsonObject);

					updateJsonObject((JSONObject) (eFromAgg.equals(null) ? new JSONObject() : eFromAgg),
							(JSONObject) (eFromPrep.equals(null) ? new JSONObject() : eFromPrep));

				} else if (eFromAgg instanceof JSONArray) {

					updateJsonObject((JSONArray) (eFromAgg.equals(null) ? new JSONArray() : eFromAgg),
							(JSONArray) (eFromPrep.equals(null) ? new JSONArray() : eFromPrep));
				} else {
					// TODO: wtf is this?
					Object element = aggregatedJsonObject.get(i);
					element = preparedJsonObject.get(i);
				}
			} catch (JSONException e) {
				log.info(e.getMessage(), e);
			}
		}
	}

	/**
	 *
	 * @param jsonObject
	 *            the object in which the key should be gotten from.
	 * @param key
	 * @param clazz
	 *            the type we expect the key to be
	 * @param <T>
	 *            the type
	 * @return an object of type T if found in jsonObject, null otherwise.
	 * @throws JSONException
	 */
	// private <T> T getAsT(JSONObject jsonObject, String key, Class<T> clazz)
	// throws JSONException {
	// Object o = jsonObject.get(key);
	// if (clazz.isInstance(o)) {
	// return (T) o;
	// }
	//
	// return null;
	// }

	/**
	 * This method set lock property in document in database and returns the
	 * aggregated document which will be further modified.
	 * 
	 * @param id
	 *            String to search in database and lock this document.
	 */
	public String getAggregatedObject(String id, boolean withLock) {
		try {
			String document = "";
			if (withLock) {
				document = objectHandler.lockDocument(id);
			} else {
				document = objectHandler.findObjectById(id);
			}

			JsonNode result = objectHandler.getAggregatedObject(document);
			if (result != null) {
				return result.toString();
			}
		} catch (Exception e) {
			log.info(e.getMessage(), e);
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
