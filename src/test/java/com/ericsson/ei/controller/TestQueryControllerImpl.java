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

import com.ericsson.ei.App;
import com.ericsson.ei.queryservice.ProcessQueryParams;

import org.apache.qpid.util.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.SocketUtils;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        App.class,
        EmbeddedMongoAutoConfiguration.class // <--- Don't forget THIS
    })
@AutoConfigureMockMvc
public class TestQueryControllerImpl {
    private static final String inputPath = "src/test/resources/AggregatedObject.json";
    private static final String QUERY = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"} }";
    private static String input;

    @MockBean
    private ProcessQueryParams unitUnderTest;

    @Autowired
    private MockMvc mockMvc;

    @BeforeClass
    public static void init() {
        int port = SocketUtils.findAvailableTcpPort();
        System.setProperty("spring.data.mongodb.port", "" + port);
    }

    @Before
    public void setUp() {
        input = FileUtils.readFileAsString(new File(inputPath));
    }

    @Test
    public void filterFormParamTest() throws Exception {

        JSONArray inputObj = new JSONArray("[" + input + "]");
        when(unitUnderTest.filterFormParam(any(JSONObject.class), any(JSONObject.class))).thenReturn(inputObj);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/query")
                .accept(MediaType.ALL)
                .content(QUERY)
                .contentType(MediaType.APPLICATION_JSON);

         MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        assertEquals(inputObj.toString(), result.getResponse().getContentAsString());
    }
}
