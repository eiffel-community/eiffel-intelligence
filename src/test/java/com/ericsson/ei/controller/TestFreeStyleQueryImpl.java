/*
   Copyright 2018 Ericsson AB.
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
package com.ericsson.ei.controller;

import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.apache.qpid.util.FileUtils;
import org.bson.Document;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestFreeStyleQueryImpl {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(TestFreeStyleQueryImpl.class);
    private static final String inputPath = "src/test/resources/AggregatedObject.json";
    private static final String REQUEST = "testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43";
    private static final String QUERY = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"} }";
    private static String input;
    private static final String DB_NAME = "MissedNotification";
    private static final String DB_COLLECTION = "Notification";

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private ProcessQueryParams unitUnderTest;

    @Before
    public void setUp() {
        input = FileUtils.readFileAsString(new File(inputPath));
        try (MongoClient mongoClient = new MongoClient()) {
            MongoDatabase db = mongoClient.getDatabase(DB_NAME);
            com.mongodb.client.MongoCollection<Document> collection = db.getCollection(DB_COLLECTION);
            final Document dbObjectInput = Document.parse(input);
            collection.insertOne(dbObjectInput);
            if (collection.count() != 0) {
                System.out.println("Data Inserted successfully in both the Collections");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void filterFormParamTest() throws Exception {
        JSONObject inputJArr = new JSONObject(input);
        LOGGER.info("The input string is : " + inputJArr.toString());
        DB db = new MongoClient().getDB(DB_NAME);
        Jongo jongo = new Jongo(db);
        MongoCollection aggObjects = jongo.getCollection(DB_COLLECTION);
        Document one = aggObjects.findOne().as(Document.class);
        JSONObject json = new JSONObject(one.toJson());
        LOGGER.info("Expect Output for FilterFormParamTest : " + json.toString());
        System.out.println(json.toString());
        JSONObject output = null;
        try {
            JsonNode inputCriteria = new ObjectMapper().readTree(QUERY);
            JSONArray result = unitUnderTest.filterFormParam(inputCriteria);
            output = result.getJSONObject(0);
            LOGGER.info("Output for FilterFormParamTest is : " + output.toString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertNotNull(output);
    }

    @Test
    public void filterQueryParamTest() throws JSONException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost:" + serverPort);
        request.setRequestURI("/query/free?request=");
        request.setQueryString("testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");
        String url = request.getRequestURL() + request.getQueryString();
        DB db = new MongoClient().getDB(DB_NAME);
        Jongo jongo = new Jongo(db);
        MongoCollection aggObjects = jongo.getCollection(DB_COLLECTION);
        Document one = aggObjects.findOne().as(Document.class);
        JSONObject json = new JSONObject(one.toJson());
        LOGGER.info("Expect Output for FilterQueryParamTest : " + json.toString());
        JSONObject output = null;
        try {
            JSONArray result = unitUnderTest.filterQueryParam(REQUEST);
            output = result.getJSONObject(0);
            LOGGER.info("Returned output from ProcessQueryParams : " + output.toString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertThat(url, is("http://localhost:" + serverPort + "/query/free?request=testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43"));
        assertNotNull(output);
    }
}