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
package com.ericsson.ei.subscription;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ericsson.ei.controller.SubscriptionController;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.services.ISubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(value = SubscriptionController.class, secure = false)
public class SubscriptionRestAPITest {
    
    @Autowired
    private MockMvc mockMvc;
    static JSONArray jsonArray = null;
    private static final String subscriptionJsonPath = "src/test/resources/subscription.json";
    ObjectMapper mapper = new ObjectMapper();
    @MockBean
    private ISubscriptionService subscriptionService;
    
    @BeforeClass
    public static void setMongoDB() throws IOException, JSONException {
        String readFileToString = FileUtils.readFileToString(new File(subscriptionJsonPath), "UTF-8");
        jsonArray = new JSONArray(readFileToString);
    }
    
    @Test
    public void addSubscription() throws Exception {
        Mockito.when(subscriptionService.doSubscriptionExist(Mockito.anyString())).thenReturn(false);
        Mockito.when(subscriptionService.addSubscription(Mockito.any(Subscription.class))).thenReturn(false);
        // Send subscription as body to /subscriptions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/subscriptions").accept(MediaType.APPLICATION_JSON)
                .content(jsonArray.getJSONObject(0).toString()).contentType(MediaType.APPLICATION_JSON);
        
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        
        SubscriptionResponse subscriptionResponse = mapper
                .readValue(result.getResponse().getContentAsString().toString(), SubscriptionResponse.class);
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals("Inserted Successfully", subscriptionResponse.getMsg());
    }
    
    @Test
    public void updateSubscription() throws Exception {
        Mockito.when(subscriptionService.doSubscriptionExist(Mockito.anyString())).thenReturn(true);
        Mockito.when(subscriptionService.modifySubscription(Mockito.any(Subscription.class), Mockito.anyString()))
                .thenReturn(false);
        // Send subscription as body to /subscriptions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.put("/subscriptions")
                .accept(MediaType.APPLICATION_JSON).content(jsonArray.getJSONObject(0).toString())
                .contentType(MediaType.APPLICATION_JSON);
        
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        
        SubscriptionResponse subscriptionResponse = mapper
                .readValue(result.getResponse().getContentAsString().toString(), SubscriptionResponse.class);
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals("Updated Successfully", subscriptionResponse.getMsg());
    }
    
    @Test
    public void updateSubscriptionFailWhenSubscriptionDoNotExist() throws Exception {
        Mockito.when(subscriptionService.doSubscriptionExist(Mockito.anyString())).thenReturn(false);
        // Send subscription as body to /subscriptions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.put("/subscriptions")
                .accept(MediaType.APPLICATION_JSON).content(jsonArray.getJSONObject(0).toString())
                .contentType(MediaType.APPLICATION_JSON);
        
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        
        SubscriptionResponse subscriptionResponse = mapper
                .readValue(result.getResponse().getContentAsString().toString(), SubscriptionResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertEquals("Subscription can't be found", subscriptionResponse.getMsg());
    }
    
    @Test
    public void getSubScriptionByName() throws Exception {
        Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        Mockito.when(subscriptionService.getSubscription(Mockito.anyString())).thenReturn(subscription2);
        // Send subscription as body to /subscriptions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/subscriptions/Subscription_Test")
                .accept(MediaType.APPLICATION_JSON);
        
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        
        Subscription[] subscription = mapper.readValue(result.getResponse().getContentAsString().toString(),
                Subscription[].class);
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals("Subscription_Test", subscription[0].getSubscriptionName());
    }
    
    @Test
    public void getSubScriptionByNameNotFound() throws Exception {
        Mockito.when(subscriptionService.getSubscription(Mockito.anyString())).thenThrow(
                new SubscriptionNotFoundException("No record found for the Subscription Name:Subscription_Test"));
        // Send subscription as body to /subscriptions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/subscriptions/Subscription_Test")
                .accept(MediaType.APPLICATION_JSON);
        
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals("[]", result.getResponse().getContentAsString());
    }
    
    @Test
    public void deleteSubScriptionByName() throws Exception {
        Mockito.when(subscriptionService.deleteSubscription(Mockito.anyString())).thenReturn(true);
        // Send subscription as body to /subscriptions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/subscriptions/Subscription_Test")
                .accept(MediaType.APPLICATION_JSON);
        
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        
        SubscriptionResponse subscriptionResponse = mapper
                .readValue(result.getResponse().getContentAsString().toString(), SubscriptionResponse.class);
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals("Deleted Successfully", subscriptionResponse.getMsg());
    }
    
    @Test
    public void deleteSubScriptionByNameNotFound() throws Exception {
        Mockito.when(subscriptionService.deleteSubscription(Mockito.anyString())).thenReturn(false);
        // Send subscription as body to /subscriptions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/subscriptions/Subscription_Test")
                .accept(MediaType.APPLICATION_JSON);
        
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        
        SubscriptionResponse subscriptionResponse = mapper
                .readValue(result.getResponse().getContentAsString().toString(), SubscriptionResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertEquals("Record not found for delete", subscriptionResponse.getMsg());
    }
}
