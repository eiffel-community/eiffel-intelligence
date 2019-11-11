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
package com.ericsson.ei.rules.test;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.ericsson.ei.controller.RuleTestControllerImpl;
import com.ericsson.ei.services.IRuleCheckService;
import com.ericsson.ei.utils.TestContextInitializer;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: TestRulesRestAPI",
        "failed.notification.collection-name: TestRulesService-missedNotifications",
        "rabbitmq.exchange.name: TestRulesRestAPI-exchange",
        "rabbitmq.consumerName: TestRulesRestAPI" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
@AutoConfigureMockMvc
public class TestRulesRestAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRulesRestAPI.class);
    private static final String INPUT_FILE_PATH = "src/test/resources/EiffelArtifactCreatedEvent.json";
    private static final String EXTRACTION_RULE_FILE_PATH = "src/test/resources/ExtractionRule.txt";
    private static final String EVENTS = "src/test/resources/AggregateListEvents.json";
    private static final String RULES = "src/test/resources/AggregateListRules.json";
    private static final String AGGREGATED_RESULT_OBJECT = "src/test/resources/AggregateResultObject.json";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IRuleCheckService ruleCheckService;

    @Value("${testaggregated.enabled:false}")
    private Boolean testEnable;

    @Test
    public void testJmesPathRestApi() throws Exception {
        String jsonInput = null;
        String extractionRules_test = null;
        try {
            jsonInput = FileUtils.readFileToString(new File(INPUT_FILE_PATH), "UTF-8");
            extractionRules_test = FileUtils.readFileToString(new File(EXTRACTION_RULE_FILE_PATH), "UTF-8");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        String requestBody = new JSONObject().put("rule", new JSONObject(extractionRules_test))
                .put("event", new JSONObject(jsonInput)).toString();

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/rule-test/run-single-rule").accept(MediaType.ALL)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        String resultStr = result.getResponse().getContentAsString();
        JSONObject obj = new JSONObject(resultStr);

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals("e90daae3-bf3f-4b0a-b899-67834fd5ebd0", obj.getString("id"));
        long timeV = 1484061386383L;
        assertEquals(timeV, obj.getLong("time"));

    }

    @Test
    public void testAggregationRestApi() throws Exception {
        String aggregatedResult = null;
        JSONArray jsonInput = null;
        JSONArray extractionRules_test = null;
        JSONArray expectedAggObject = null;
        try {
            jsonInput = new JSONArray(FileUtils.readFileToString(new File(EVENTS), "UTF-8"));
            extractionRules_test = new JSONArray(FileUtils.readFileToString(new File(RULES), "UTF-8"));
            aggregatedResult = FileUtils.readFileToString(new File(AGGREGATED_RESULT_OBJECT), "UTF-8");
            expectedAggObject = new JSONArray(aggregatedResult);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        String body = "{\"listRulesJson\":" + extractionRules_test.toString() + ",\"listEventsJson\":"
                + jsonInput.toString() + "}";
        Mockito.when(
                ruleCheckService.prepareAggregatedObject(Mockito.any(JSONArray.class), Mockito.any(JSONArray.class)))
                .thenReturn(aggregatedResult);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/rule-test/run-full-aggregation")
                .accept(MediaType.ALL).content(body).contentType(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        String resultStr = result.getResponse().getContentAsString();
        if (testEnable) {
            JSONArray actualAggObject = new JSONArray(resultStr);
            assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
            assertEquals(expectedAggObject.toString(), actualAggObject.toString());
        } else {
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), result.getResponse().getStatus());
        }
    }

    /**
     * TestRulePageEnabled should always be false when pushed to Github.
     *
     * @throws Exception
     */
    @Test
    public void testGetTestRulePageEnabledAPI_ensurePropertyFalse() throws Exception {
        String responseBody = new JSONObject().put("status", false).toString();
        mockMvc.perform(MockMvcRequestBuilders.get("/rule-test")
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk())
                .andExpect(content().string(responseBody)).andReturn();
    }

    @Test
    public void testGetTestRulePageEnabledAPI_setPropertyTrue() throws Exception {
        String responseBody = new JSONObject().put("status", true).toString();
        RuleTestControllerImpl ruleCheckControllerImpl = new RuleTestControllerImpl();
        ruleCheckControllerImpl.setTestEnabled(true);
        ResponseEntity<?> responseEntity = ruleCheckControllerImpl.getRuleTest(null);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(responseBody, responseEntity.getBody().toString());

    }

    @Test
    public void testGetTestRulePageEnabledAPI_setPropertyFalse() throws Exception {
        String responseBody = new JSONObject().put("status", false).toString();
        RuleTestControllerImpl ruleCheckControllerImpl = new RuleTestControllerImpl();
        ruleCheckControllerImpl.setTestEnabled(false);
        ResponseEntity<?> responseEntity = ruleCheckControllerImpl.getRuleTest(null);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(responseBody, responseEntity.getBody().toString());
    }
}
