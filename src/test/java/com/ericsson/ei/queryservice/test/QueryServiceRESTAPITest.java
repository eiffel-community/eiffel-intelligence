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
package com.ericsson.ei.queryservice.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ericsson.ei.App;
import com.ericsson.ei.controller.FailedNotificationControllerImpl;
import com.ericsson.ei.controller.QueryAggregatedObjectController;
import com.ericsson.ei.controller.QueryAggregatedObjectControllerImpl;
import com.ericsson.ei.utils.TestContextInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestPropertySource(properties = { "spring.data.mongodb.database: QueryServiceRESTAPITest",
        "missedNotificationDataBaseName: QueryServiceRESTAPITest-missedNotifications",
        "rabbitmq.exchange.name: QueryServiceRESTAPITest-exchange", "rabbitmq.consumerName: QueryServiceRESTAPITest" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(value = QueryAggregatedObjectController.class, secure = false)
public class QueryServiceRESTAPITest {

    @Autowired
    private MockMvc mockMvc;

    static JSONArray jsonArray = null;

    static Logger LOGGER = LoggerFactory.getLogger(QueryServiceRESTAPITest.class);

    private static final String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static final String missedNotificationPath = "src/test/resources/MissedNotification.json";
    private static final String aggregatedOutputPath = "src/test/resources/AggregatedOutput.json";
    private static final String missedNotificationOutputPath = "src/test/resources/MissedNotificationOutput.json";
    private static String aggregatedObject;
    private static String missedNotification;

    ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private QueryAggregatedObjectControllerImpl aggregatedObjectController;

    @MockBean
    private FailedNotificationControllerImpl failedNotificationsController;

    @BeforeClass
    public static void init() throws IOException, JSONException {
        aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath), "UTF-8");
        missedNotification = FileUtils.readFileToString(new File(missedNotificationPath), "UTF-8");
    }

    @Test
    public void getQueryAggregatedObjectTest() throws Exception {
        ArrayList<String> response = new ArrayList<String>();
        response.add(aggregatedObject);
        String expectedOutputWithSquareBrackets = FileUtils.readFileToString(new File(aggregatedOutputPath), "UTF-8");
        String expectedOutputString = (expectedOutputWithSquareBrackets.substring(1,
                expectedOutputWithSquareBrackets.length() - 1));
        JsonNode expectedOutput = mapper.readTree(expectedOutputString);

        Mockito.when(aggregatedObjectController.getQueryAggregatedObject(Mockito.anyString()))
                .thenReturn(new ResponseEntity(response.get(0), HttpStatus.OK));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/queryAggregatedObject")
                .accept(MediaType.APPLICATION_JSON).param("ID", "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43")
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult result = result = mockMvc.perform(requestBuilder).andReturn();
        String output_string = result.getResponse().getContentAsString().toString();
        JsonNode output = mapper.readTree(output_string);
        LOGGER.info("The Output is : " + output);

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(expectedOutput, output);
    }

    @Test
    public void getQueryMissedNotificationsTest() throws Exception {
        ArrayList<String> response = new ArrayList<String>();
        response.add(missedNotification);
        String expectedOutputWithSquareBrackets = FileUtils.readFileToString(new File(missedNotificationOutputPath),
                "UTF-8");
        String expectedOutput_string = (expectedOutputWithSquareBrackets.substring(1,
                expectedOutputWithSquareBrackets.length() - 1));
        JsonNode expectedOutput = mapper.readTree(expectedOutput_string);
        LOGGER.info("The expected output is : " + expectedOutput.toString());

        Mockito.when(failedNotificationsController.getFailedNotifications(Mockito.anyString()))
                .thenReturn(new ResponseEntity(response.get(0), HttpStatus.OK));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/failed-notifications?")
                .accept(MediaType.APPLICATION_JSON).param("SubscriptionName", "Subscription_1");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        String outputString = result.getResponse().getContentAsString().toString();
        JsonNode output = mapper.readTree(outputString);
        LOGGER.info("The Output is : " + output);

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(expectedOutput, output);
    }
}
