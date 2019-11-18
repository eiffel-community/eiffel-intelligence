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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.qpid.server.util.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;

import com.ericsson.ei.App;
import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.ericsson.ei.utils.TestContextInitializer;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: TestQueryControllerImpl",
        "failed.notification.collection-name: TestQueryControllerImpl-failedNotifications",
        "rabbitmq.exchange.name: TestQueryControllerImpl-exchange",
        "rabbitmq.consumerName: TestQueryControllerImpl" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
@AutoConfigureMockMvc
public class TestQueryControllerImpl extends ControllerTestBaseClass {
    private static final String INPUT_PATH = "src/test/resources/AggregatedObject.json";
    private static final String QUERY = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"} }";
    private static String INPUT;
    private final String QUERY_ENDPOINT = "/query";

    @MockBean
    private ProcessQueryParams unitUnderTest;

    @Before
    public void setUp() {
        INPUT = FileUtils.readFileAsString(new File(INPUT_PATH));
    }

    @Test
    public void filterFormParamTest() throws Throwable {
        JSONArray inputObj = new JSONArray("[" + INPUT + "]");
        when(unitUnderTest.runQuery(any(JSONObject.class), any(JSONObject.class), any(String.class)))
                .thenReturn(inputObj);

        MvcResult result = performMockMvcRequest(EntryPointConstantsUtils.AGGREGATED_OBJECTS + QUERY_ENDPOINT, QUERY);
        assertEquals(inputObj.toString(), result.getResponse().getContentAsString());


    }
}
