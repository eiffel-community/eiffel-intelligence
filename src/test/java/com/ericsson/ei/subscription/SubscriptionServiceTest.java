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

import com.ericsson.ei.App;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.services.ISubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {App.class})
public class SubscriptionServiceTest {

    private static final String subscriptionJsonPath = "src/test/resources/subscription_CLME.json";

    @Value("${spring.data.mongodb.database}")
    private String dataBaseName;

    @Value("${subscription.collection.repeatFlagHandlerName}")
    private String repeatFlagHandlerCollection;

    private String subscriptionName;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @MockBean
    private Authentication authentication;

    @MockBean
    private SecurityContext securityContext;

    private ObjectMapper mapper = new ObjectMapper();
    private static JSONArray jsonArray = null;

    @BeforeClass
    public static void setMongoDB() throws IOException, JSONException {
        String readFileToString = FileUtils.readFileToString(new File(subscriptionJsonPath), "UTF-8");
        jsonArray = new JSONArray(readFileToString);
    }

    @Test
    public void testInsertSubscription() throws IOException, JSONException {
        Subscription subscription = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        assertTrue(subscriptionService.addSubscription(subscription));
        // deleting the test data
        deleteSubscriptionsByName(subscription.getSubscriptionName());
    }

    @Test
    public void testUpdateSubscription() throws JSONException, IOException, SubscriptionNotFoundException {
        Subscription subscription;
        // Insert Subscription
        Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        String expectedSubscriptionName = subscription2.getSubscriptionName();
        String expectedUserName = subscription2.getUserName();
        subscriptionService.addSubscription(subscription2);
        // Fetch the inserted subscription
        subscription2 = subscriptionService.getSubscription(expectedSubscriptionName);
        subscriptionName = subscription2.getSubscriptionName();
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("ABC");
        assertEquals(subscriptionName, expectedSubscriptionName);
        assertEquals(authentication.getName(), expectedUserName);
        // Updating subscription2(subscriptionName=Subscription_Test) with
        // the subscription(subscriptionName=Subscription_Test_Modify)
        subscription = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        String expectedModifiedSubscriptionName = subscription2.getSubscriptionName();
        boolean addSubscription = subscriptionService.modifySubscription(subscription, subscriptionName);
        // test update done successfully
        assertTrue(addSubscription);
        subscription = subscriptionService.getSubscription(expectedModifiedSubscriptionName);
        subscriptionName = subscription.getSubscriptionName();
        assertEquals(subscriptionName, expectedModifiedSubscriptionName);
        // deleting the test data
        deleteSubscriptionsByName(subscriptionName);
    }

    @Test
    public void testGetAllSubscriptions() throws JSONException, IOException, SubscriptionNotFoundException {
        List<Subscription> subscriptions;
        // Insert Subscription
        Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        subscriptionService.addSubscription(subscription2);
        subscriptions = subscriptionService.getSubscriptions();
        assertTrue(subscriptions.size() > 0);
        // deleting the test data
        deleteSubscriptionsByName(subscription2.getSubscriptionName());
    }

    @Test
    public void testGetSubscriptionsByName() {
        Subscription subscription;
        try {
            // Insert Subscription
            Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            subscriptionService.addSubscription(subscription2);
            String expectedSubscriptionName = subscription2.getSubscriptionName();
            subscription = subscriptionService.getSubscription(expectedSubscriptionName);
            subscriptionName = subscription.getSubscriptionName();
            assertEquals(subscriptionName, expectedSubscriptionName);
            // deleting the test data
            deleteSubscriptionsByName(subscriptionName);
        } catch (SubscriptionNotFoundException | IOException | JSONException e) {
        }
    }

    @Test
    public void testDoSubscriptionExist() throws IOException, JSONException {
        boolean doSubscriptionExist;
        // Insert Subscription
        Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        subscriptionService.addSubscription(subscription2);
        subscriptionName = subscription2.getSubscriptionName();
        doSubscriptionExist = subscriptionService.doSubscriptionExist(subscriptionName);
        assertTrue(doSubscriptionExist);
        // deleting the test data
        deleteSubscriptionsByName(subscriptionName);
    }

    @Test
    public void testDeleteSubscriptionsByName() throws IOException, JSONException {
        // Insert Subscription
        Subscription subscription2;
        subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        subscriptionService.addSubscription(subscription2);
        String expectedSubscriptionName = subscription2.getSubscriptionName();
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("ABC");
        boolean deleteSubscription = subscriptionService.deleteSubscription(expectedSubscriptionName);
        assertTrue(deleteSubscription);
    }

    @Test
    public void testDeleteSubscriptionsByNameAndCleanUpOfRepeatHandlerDb() throws IOException, JSONException {
        // Insert Subscription
        Subscription subscription2;
        subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        subscriptionService.addSubscription(subscription2);
        String expectedSubscriptionName = subscription2.getSubscriptionName();
        // Inserting a matched subscription AggrObjIds document to
        // RepeatHandlerDb database collection.
        BasicDBObject docInput = new BasicDBObject();
        docInput.put("subscriptionId", expectedSubscriptionName);
        mongoDBHandler.insertDocument(dataBaseName, repeatFlagHandlerCollection, docInput.toString());
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("ABC");
        boolean deleteSubscription = subscriptionService.deleteSubscription(expectedSubscriptionName);
        assertTrue(deleteSubscription);
        // Checking if it removes the Subscription Matched AggrObjIds
        // document from RepeatHandlerDb database collection.
        String subscriptionIdMatchedAggrIdObjQuery = "{ \"subscriptionId\" : \"" + expectedSubscriptionName + "\"}";
        List<String> result = mongoDBHandler.find(dataBaseName, repeatFlagHandlerCollection,
                subscriptionIdMatchedAggrIdObjQuery);
        assertEquals("[]", result.toString());
    }

    @Test
    public void testUpdateSubscriptionAndCleanUpOfRepeatHandlerDb() throws IOException, JSONException, SubscriptionNotFoundException {
        Subscription subscription;
        // Insert Subscription
        Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        String expectedSubscriptionName = subscription2.getSubscriptionName();
        subscriptionService.addSubscription(subscription2);
        // Fetch the inserted subscription
        subscription2 = subscriptionService.getSubscription(expectedSubscriptionName);
        subscriptionName = subscription2.getSubscriptionName();
        assertEquals(subscriptionName, expectedSubscriptionName);
        // Inserting a matched subscription AggrObjIds document to
        // RepeatHandlerDb database collection.
        BasicDBObject docInput = new BasicDBObject();
        docInput.put("subscriptionId", subscriptionName);
        mongoDBHandler.insertDocument(dataBaseName, repeatFlagHandlerCollection, docInput.toString());
        // Updating subscription2(subscriptionName=Subscription_Test) with
        // the subscription(subscriptionName=Subscription_Test_Modify)
        subscription = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        String expectedModifiedSubscriptionName = subscription2.getSubscriptionName();
        boolean addSubscription = subscriptionService.modifySubscription(subscription, subscriptionName);
        // test update done successfully
        assertTrue(addSubscription);
        subscription = subscriptionService.getSubscription(expectedModifiedSubscriptionName);
        subscriptionName = subscription.getSubscriptionName();
        assertEquals(subscriptionName, expectedModifiedSubscriptionName);
        // Checking if it removes the Subscription Matched AggrObjIds
        // document from RepeatHandlerDb database collection.
        String subscriptionIdMatchedAggrIdObjQuery = "{ \"subscriptionId\" : \"" + subscriptionName + "\"}";
        List<String> result = mongoDBHandler.find(dataBaseName, repeatFlagHandlerCollection,
                subscriptionIdMatchedAggrIdObjQuery);
        assertEquals("[]", result.toString());
        // deleting the test data
        deleteSubscriptionsByName(subscriptionName);
    }

    @Test(expected = SubscriptionNotFoundException.class)
    public void testExceptionGetSubscriptionsByName() throws SubscriptionNotFoundException {
        subscriptionService.getSubscription("Subscription_Test1238586455");
    }

    private void deleteSubscriptionsByName(String subscriptionName) {
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("ABC");
        subscriptionService.deleteSubscription(subscriptionName);
    }
}