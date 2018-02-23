/***********************************************************************
 *                                                                     *
 * Copyright Ericsson AB 2018                                          *
 *                                                                     *
 * No part of this software may be reproduced in any form without the  *
 * written permission of the copyright owner.                          *
 *                                                                     *
 ***********************************************************************/
package com.ericsson.ei.flowtests;

import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.qpid.util.FileUtils;
import org.bson.Document;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
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
    private static final String DEMO = "demo";
    private static final String AGG_OBJECT = "aggObject";
    private static final String REQUEST = "testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43";
    private static final String QUERY = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"} }";
    private static String input;

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private ProcessQueryParams unitUnderTest;

    @BeforeClass
    public static void insertData() {
        input = FileUtils.readFileAsString(new File(inputPath));
        MongoClient mongoClient = new MongoClient();
        try {
            DB db = mongoClient.getDB(DEMO);
            DBCollection table = db.getCollection(AGG_OBJECT);
            DBObject dbObjectInput = (DBObject) JSON.parse(input);
            WriteResult result1 = table.insert(dbObjectInput);
            if (result1.wasAcknowledged()) {
                LOGGER.info(" Data Inserted successfully in both the Collections");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void filterFormParamTest() throws JSONException, Exception {
        JSONObject inputJArr = new JSONObject(input);
        LOGGER.info("The input string is : " + inputJArr.toString());

        DB db = new MongoClient().getDB(DEMO);
        Jongo jongo = new Jongo(db);
        MongoCollection aggObjects = jongo.getCollection(AGG_OBJECT);
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
        request.setRequestURI("/freeStyleQuery?request=");
        request.setQueryString("testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");

        String url = request.getRequestURL() + request.getQueryString();

        DB db = new MongoClient().getDB(DEMO);
        Jongo jongo = new Jongo(db);
        MongoCollection aggObjects = jongo.getCollection(AGG_OBJECT);
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
        assertThat(url, is("http://localhost:" + serverPort + "/freeStyleQuery?request=testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43"));
        assertNotNull(output);
    }
}