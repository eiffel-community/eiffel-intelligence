package com.ericsson.ei.controller;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.SocketUtils;

import com.ericsson.ei.App;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        App.class, 
        EmbeddedMongoAutoConfiguration.class // <--- Don't forget THIS
    })
@AutoConfigureMockMvc
public class TestLoginControllerImpl {

    @Autowired
    private MockMvc mockMvc;

    @BeforeClass
    public static void init() {
        int port = SocketUtils.findAvailableTcpPort();
        System.setProperty("spring.data.mongodb.port", "" + port);
    }
    
    @Test
    public void testResponseStatus() throws Exception {
        String responseBody = "{\"user\":\"anonymousUser\"}";
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/login")
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().string(responseBody))
            .andReturn();
    }

}
