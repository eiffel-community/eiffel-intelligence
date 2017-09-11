/***********************************************************************
 *                                                                     *
 * Copyright Ericsson AB 2017                                          *
 *                                                                     *
 * No part of this software may be reproduced in any form without the  *
 * written permission of the copyright owner.                          *
 *                                                                     *
 ***********************************************************************/
package com.ericsson.ei.queryservice.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.qpid.util.FileUtils;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class ProcessQueryParamsTest extends MongoHarness {

	@Value("${aggregationCollectionName}")
	private String aggregationCollectionName;

	@Value("${aggregationDataBaseName}")
	private String aggregationDataBaseName;

	@Value("${missedNotificationCollectionName}")
	private String missedNotificationCollectionName;

	@Value("${missedNotificationDataBaseName}")
	private String missedNotificationDataBaseName;

	@Autowired
	private ProcessQueryParams unitUnderTest;

	static Logger log = (Logger) LoggerFactory.getLogger(ProcessQueryParamsTest.class);

	private static final String inputPath = "src/test/resources/AggregatedInput.json";

	String query = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b44\"} }";

	@BeforeClass
	public static void setup() {
		// insert events into the database
		String input = FileUtils.readFileAsString(new File(inputPath));
		Document document = Document.parse(input);
		final MongoClient mongo = new MongoClient(host, port);
		final MongoDatabase db = mongo.getDatabase("demo");
		db.createCollection("aggObject");
		col = db.getCollection("aggObject");
		System.out.println("Database connected");
		col.insertOne(document);
	}

	@Test
	public void filterFormParamTest() throws JSONException, JsonProcessingException, IOException {
		String input = FileUtils.readFileAsString(new File(inputPath));
		JSONObject inputJArr = new JSONObject(input);
		log.info("The input string is : " + inputJArr.toString());
		JsonNode inputCriteria = null;
		JSONArray result = null;

		Iterable<Document> outputDoc = col.find().projection(new Document("_id", 0));
		Iterator itr = outputDoc.iterator();
		JSONObject json = new JSONObject(JSON.serialize(itr.next()));
		System.out.println("The extracted output is : " + json.toString());
		log.info("Expect Output for FilterFormParamTest : " + json.toString());
		System.out.println(json.toString());
		JSONObject output = null;
		try {
			inputCriteria = new ObjectMapper().readTree(query);
			result = unitUnderTest.filterFormParam(inputCriteria);
			output = result.getJSONObject(0);
			output.remove("_id");
			log.info("Output for FilterFormParamTest is : " + output.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		assertEquals(output.toString(), json.toString());

	}

	@Test
	public void filterQueryParamTest() throws JsonProcessingException, IOException, JSONException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("localhost:8080");
		request.setRequestURI("/ei/query");
		request.setQueryString(
				"testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b44");

		String url = request.getRequestURL() + "?" + request.getQueryString();

		JsonNode inputCriteria = null;
		JSONArray result = null;
		Iterable<Document> outputDoc = col.find().projection(new Document("_id", 0));
		Iterator itr = outputDoc.iterator();
		JSONObject json = new JSONObject(JSON.serialize(itr.next()));
		System.out.println("The extracted output is : " + json.toString());
		log.info("Expect Output for FilterQueryParamTest : " + json.toString());
		JSONObject output = null;
		try {
			inputCriteria = new ObjectMapper().readTree(query);
			result = unitUnderTest.filterQueryParam(request);
			output = result.getJSONObject(0);
			output.remove("_id");
			log.info("Returned output from ProcessQueryParams : " + output.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		assertThat(url, is(
				"http://localhost:8080/ei/query?testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b44"));

		assertEquals(output.toString(), json.toString());
	}

}
