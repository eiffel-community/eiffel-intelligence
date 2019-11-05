package com.ericsson.ei.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
public class ControllerTestBaseClass {

    @Autowired
    private MockMvc mockMvc;

    protected void assertOkResponseStatus(String endpoint) throws Throwable {
        mockMvc.perform(MockMvcRequestBuilders.get(endpoint)
                                              .accept(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andReturn();
    }

    protected void assertExpectedResponse(String endpoint, String responseBody) throws Throwable {
        mockMvc.perform(MockMvcRequestBuilders.get(endpoint)
                                              .accept(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(content().string(responseBody))
               .andReturn();
    }

    protected MvcResult performMockMvcRequest(String endpoint, String body) throws Throwable {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(endpoint)
                                                              .accept(MediaType.ALL)
                                                              .content(body)
                                                              .contentType(
                                                                      MediaType.APPLICATION_JSON);

        return mockMvc.perform(requestBuilder).andReturn();
    }

}