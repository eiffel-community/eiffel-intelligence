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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ericsson.ei.App;
import com.ericsson.ei.utils.TestContextInitializer;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: TestAuthControllerImpl",
        "failed.notification.database-name: TestAuthControllerImpl-failedNotifications",
        "rabbitmq.exchange.name: TestAuthControllerImpl-exchange",
        "rabbitmq.consumerName: TestAuthControllerImpl" })
@ContextConfiguration(
        classes = App.class,
        loader = SpringBootContextLoader.class,
        initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
@AutoConfigureMockMvc
public class TestAuthControllerImpl {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAuth() throws Exception {
        String responseBody = new JSONObject().put("security", false).toString();
        mockMvc.perform(MockMvcRequestBuilders.get("/auth").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andExpect(content().string(responseBody)).andReturn();
    }

    @Test
    public void testGetLogin() throws Exception {
        String responseBody = new JSONObject().put("user", "anonymousUser").toString();
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/login").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andExpect(content().string(responseBody)).andReturn();
    }

}
