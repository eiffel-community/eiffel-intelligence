package com.ericsson.ei.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(value = SubscriptiontemplateController.class, secure = false)
public class TestSubscriptionTemplateControllerImpl {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptiontemplateController subscriptiontemplateController;

    @Test
    public void testResponseStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/download/subscriptiontemplate")
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();
    }
}