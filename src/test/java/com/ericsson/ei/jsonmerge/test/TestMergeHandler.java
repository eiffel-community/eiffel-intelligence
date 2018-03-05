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
package com.ericsson.ei.jsonmerge.test;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.jsonmerge.MergePrepare;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

import java.io.File;

import org.apache.commons.io.FileUtils;

@RunWith(Parameterized.class)
public class TestMergeHandler {
	public static final String testDataPath = "src/test/resources/MergeHandlerData.json";
	public String rule;
	public String id;
	public String updatedRule;
	public String event;
	public JsonNode objectToMerge;
	public String aggregatedObject;
	public String preparedObject;
	public String mergeObjectResult;

	static Logger log = (Logger) LoggerFactory.getLogger(TestMergeHandler.class);

	public TestMergeHandler(String rule, String id, String updatedRule, String event, String aggregatedObject,
			String objectToMerge, String preparedObject, String mergeObjectResult) {
		ObjectMapper objectmapper = new ObjectMapper();
		try {
			this.objectToMerge = objectmapper.readTree((new JSONObject(objectToMerge)).toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		this.rule = rule;
		this.id = id;
		this.updatedRule = updatedRule;
		this.event = event;
		this.aggregatedObject = aggregatedObject;
		this.preparedObject = preparedObject;
		this.mergeObjectResult = mergeObjectResult;
	}

	@Test
	public void replaceIdMarkerInRules() {
		MergeHandler mergeHandlerObject = new MergeHandler();
		// TODO read marker from application.properties
		mergeHandlerObject.setMergeIdMarker("%IdentifyRules%");
		String result = (String) mergeHandlerObject.replaceIdMarkerInRules(rule, id);
		assertEquals(updatedRule, result);
	}

	@Test
	public void mergeContentToObject() {
		String output = new String("output");
		String result = new String("result");
		MergeHandler mocked = mock(MergeHandler.class);
		when(mocked.getAggregatedObject(id, true)).thenReturn(aggregatedObject);
		when(mocked.mergeContentToObject(aggregatedObject, preparedObject)).thenCallRealMethod();
		Mockito.doCallRealMethod().when(mocked).setJmesPathInterface(Mockito.any(JmesPathInterface.class));
		Mockito.doCallRealMethod().when(mocked).setPrepareMergePrepareObject(Mockito.any(MergePrepare.class));
		mocked.setJmesPathInterface(new JmesPathInterface());
		mocked.setPrepareMergePrepareObject(new MergePrepare());
		try {
			output = new JSONObject(mergeObjectResult).toString();
			result = new JSONObject(mocked.mergeContentToObject(aggregatedObject, preparedObject)).toString();
		} catch (JSONException e) {
			log.error(e.getMessage(), e);
		}
		assertEquals(output, result);
	}

	@Parameters
	public static Collection<Object[]> inputTestData() {
		String testData = null;
		Collection<Object[]> baseList = new ArrayList<Object[]>();
		try {
			testData = FileUtils.readFileToString(new File(testDataPath), "UTF-8");
			JSONArray testDataJson = new JSONArray(testData);
			for (int i = 0; i < testDataJson.length(); i++) {
				ArrayList<String> childList = new ArrayList<String>();
				for (int k = 0; k < ((JSONArray) testDataJson.get(i)).length(); k++) {
					childList.add(((String) ((JSONArray) testDataJson.get(i)).get(k)));
				}
				baseList.add(childList.toArray());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return baseList;
	}
}