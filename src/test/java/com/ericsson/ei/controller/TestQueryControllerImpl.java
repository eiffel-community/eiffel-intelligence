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
import org.apache.qpid.util.FileUtils;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestQueryControllerImpl {
    private static final String inputPath = "src/test/resources/AggregatedObject.json";
    private static final String REQUEST = "testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43";
    private static final String QUERY = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"} }";
    private static String input;

    @MockBean
    private ProcessQueryParams unitUnderTest;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        input = FileUtils.readFileAsString(new File(inputPath));
    }

    @Test
    public void filterFormParamTest() throws Exception {
        JSONArray inputObj = new JSONArray("[" + input + "]");
        when(unitUnderTest.filterFormParam(any(JsonNode.class))).thenReturn(inputObj);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/query")
                .param("request", QUERY))
                .andReturn();
        assertEquals(inputObj.toString(), result.getResponse().getContentAsString());
    }

    @Test
    public void filterQueryParamTest() throws Exception {
        JSONArray inputObj = new JSONArray("[" + input + "]");
        when(unitUnderTest.filterQueryParam(anyString())).thenReturn(inputObj);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/query")
                .param("request", REQUEST))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        assertEquals(inputObj.toString(), result.getResponse().getContentAsString());
    }
}
