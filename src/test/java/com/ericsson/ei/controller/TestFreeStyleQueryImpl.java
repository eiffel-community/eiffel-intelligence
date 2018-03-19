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
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
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
            DB db = mongoClient.getDB(DB_NAME);
            DBCollection collection = db.getCollection(DB_COLLECTION);
            DBObject dbObjectInput = (DBObject) JSON.parse(input);
            WriteResult result = collection.insert(dbObjectInput);
            if (result.wasAcknowledged()) {
                LOGGER.debug("Data Inserted successfully in both the Collections");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Test
    public void filterFormParamTest() throws Exception {
        JSONObject inputJArr = new JSONObject(input);
        LOGGER.debug("The input string is : " + inputJArr.toString());
        JSONObject output = null;
        try {
            JsonNode inputCriteria = new ObjectMapper().readTree(QUERY);
            JSONArray result = unitUnderTest.filterFormParam(inputCriteria);
            output = result.getJSONObject(0);
            LOGGER.debug("Output for FilterFormParamTest is : " + output.toString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertNotNull(output);
    }

    @Test
    public void filterQueryParamTest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost:" + serverPort);
        request.setRequestURI("/query/free?request=");
        request.setQueryString("testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");
        String url = request.getRequestURL() + request.getQueryString();
        JSONObject output = null;
        try {
            JSONArray result = unitUnderTest.filterQueryParam(REQUEST);
            output = result.getJSONObject(0);
            LOGGER.debug("Returned output from ProcessQueryParams : " + output.toString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        assertThat(url, is("http://localhost:" + serverPort + "/query/free?request=testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43"));
        assertNotNull(output);
    }
}