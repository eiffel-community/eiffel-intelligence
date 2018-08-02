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
import com.ericsson.ei.MongoClientInitializer;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.PostConstruct;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {App.class})
@AutoConfigureMockMvc
@ContextConfiguration(initializers = MongoClientInitializer.class)
@TestExecutionListeners(value = {DependencyInjectionTestExecutionListener.class})
public class TestAuthControllerImpl {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @PostConstruct
    public void setUp() {
        mongoDBHandler.setMongoClient(MongoClientInitializer.getMongoClient());
    }

    @Test
    public void testGetAuth() throws Exception {
        String responseBody = new JSONObject().put("security", false).toString();
        mockMvc.perform(MockMvcRequestBuilders.get("/auth")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(responseBody))
                .andReturn();
    }

    @Test
    public void testGetLogin() throws Exception {
        String responseBody = new JSONObject().put("user", "anonymousUser").toString();
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/login")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(responseBody))
                .andReturn();
    }

    @Test
    public void testGetCheckStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/checkStatus")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
    }
}