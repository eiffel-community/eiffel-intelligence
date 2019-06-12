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
package com.ericsson.ei.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.expression.AccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;
import com.ericsson.eiffelcommons.subscriptionobject.SubscriptionObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class SubscriptionServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(SubscriptionServiceTest.class);

    private static final String subscriptionJsonPath = "src/test/resources/subscription_CLME.json";
    private static final String subscriptionJsonPath_du = "src/test/resources/subscription_single_differentUser.json";

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

    private static MongodForTestsFactory testsFactory;

    private ObjectMapper mapper = new ObjectMapper();

    static JSONArray jsonArray = null;
    static JSONArray jsonArray_du = null;
    static MongoClient mongoClient = null;

    @BeforeClass
    public static void setMongoDB() throws IOException, JSONException {
        try {
            testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
            mongoClient = testsFactory.newMongo();
            String port = "" + mongoClient.getAddress().getPort();
            System.setProperty("spring.data.mongodb.port", port);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }
        String readFileToString = FileUtils.readFileToString(new File(subscriptionJsonPath), "UTF-8");
        jsonArray = new JSONArray(readFileToString);
        String readFileToString_du = FileUtils.readFileToString(new File(subscriptionJsonPath_du), "UTF-8");
        jsonArray_du = new JSONArray(readFileToString_du);

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getJSONObject(i).toString());
        }
    }

    @PostConstruct
    public void init() {
        mongoDBHandler.setMongoClient(mongoClient);
    }

    @AfterClass
    public static void tearDownMongoDB() throws Exception {
        if (mongoClient != null)
            mongoClient.close();
        if (testsFactory != null)
            testsFactory.shutdown();
    }

    @Test
    public void testInsertSubscription() throws Exception {
        Subscription subscription = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
        subscriptionService.addSubscription(subscription);
        // deleting the test data
        deleteSubscriptionsByName(subscription.getSubscriptionName());
    }

    @Test
    public void testUpdateSubscription() {
        Subscription subscription;
        try {
            // Insert Subscription
            Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            String expectedSubscriptionName = subscription2.getSubscriptionName();
            String expectedUserName = subscription2.getUserName();
            subscriptionService.addSubscription(subscription2);
            // Fetch the inserted subscription
            subscription2 = null;
            subscription2 = subscriptionService.getSubscription(expectedSubscriptionName);
            subscriptionName = subscription2.getSubscriptionName();

            SecurityContextHolder.setContext(securityContext);
            Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
            Mockito.when(authentication.getName()).thenReturn("ABC");

            assertEquals(subscriptionName, expectedSubscriptionName);
            assertEquals(authentication.getName(), expectedUserName);

            // Updating subscription2(subscriptionName=Subscription_Test) with
            // the subscription(subscriptionName=Subscription_Test_Modify)
            subscription = mapper.readValue(jsonArray.getJSONObject(1).toString(), Subscription.class);
            String expectedModifiedSubscriptionName = subscription2.getSubscriptionName();

            boolean addSubscription = subscriptionService.modifySubscription(subscription, subscriptionName);

            // test update done successfully
            assertEquals(addSubscription, true);
            subscription = null;
            subscription = subscriptionService.getSubscription(expectedModifiedSubscriptionName);
            subscriptionName = subscription.getSubscriptionName();
            assertEquals(subscriptionName, expectedModifiedSubscriptionName);

            assertEquals(authentication.getName(), expectedModifiedSubscriptionName);

            // deleting the test data
            deleteSubscriptionsByName(subscriptionName);
        } catch (Exception e) {
        }
    }

    @Test
    public void testGetAllSubscriptions() throws AccessException {
        List<Subscription> subscriptions;
        try {
            // Insert Subscription
            Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            subscriptionService.addSubscription(subscription2);

            subscriptions = subscriptionService.getSubscriptions();
            assertTrue(subscriptions.size() > 0);

            // deleting the test data
            deleteSubscriptionsByName(subscription2.getSubscriptionName());
        } catch (SubscriptionNotFoundException | IOException | JSONException e) {
        }
    }

    @Test
    public void testGetSubscriptionsByName() throws AccessException {
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
    public void testDoSubscriptionExist() throws AccessException {
        boolean doSubscriptionExist;
        try {
            // Insert Subscription
            Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            subscriptionService.addSubscription(subscription2);

            subscriptionName = subscription2.getSubscriptionName();
            doSubscriptionExist = subscriptionService.doSubscriptionExist(subscriptionName);
            assertEquals(doSubscriptionExist, true);
            // deleting the test data
            deleteSubscriptionsByName(subscriptionName);
        } catch (IOException | JSONException e) {
        }
    }

    @Test
    public void testDeleteSubscriptionsByName() throws AccessException {
        // Insert Subscription
        Subscription subscription2;
        try {
            subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);

            subscriptionService.addSubscription(subscription2);
            String expectedSubscriptionName = subscription2.getSubscriptionName();
            SecurityContextHolder.setContext(securityContext);
            Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
            Mockito.when(authentication.getName()).thenReturn("ABC");

            boolean isDeleted = subscriptionService.deleteSubscription(expectedSubscriptionName);
            assertEquals("Subscription should have been deleted: ", true, isDeleted);
        } catch (IOException | JSONException e) {
        }
    }

    @Test
    public void testDeleteSubscriptionsMissingLdapUserName() throws AccessException, IOException {
        String subscriptionName = "test_name";
        SubscriptionObject subscriptionObject = new RestPostSubscriptionObject(subscriptionName);
        String subscriptionString = subscriptionObject.toString();

        // Remove ldapUserName key
        String subscriptionStringWithoutLdapUserName = subscriptionString.replace("ldapUserName", "anotherKey");
        Subscription subscription = mapper.readValue(subscriptionStringWithoutLdapUserName, Subscription.class);

        // Ensure ldapUserName is null
        String ldapUserName = subscription.getLdapUserName();
        assertNull(ldapUserName);

        // Create subscription in mongo db.
        subscriptionService.addSubscription(subscription);

        // Delete subscription
        boolean isDeleted = subscriptionService.deleteSubscription(subscription.getSubscriptionName());
        assertEquals("Subscription should have been deleted: ", true, isDeleted);
    }

    @Test
    public void testDeleteSubscriptionsByNameAndCleanUpOfRepeatHandlerDb() throws AccessException {
        // Insert Subscription
        Subscription subscription2;
        try {
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
            assertEquals(deleteSubscription, true);

            // Checking if it removes the Subscription Matched AggrObjIds
            // document from RepeatHandlerDb database collection.
            String subscriptionIdMatchedAggrIdObjQuery = "{ \"subscriptionId\" : \"" + expectedSubscriptionName + "\"}";
            ArrayList<String> result = mongoDBHandler.find(dataBaseName, repeatFlagHandlerCollection,
                    subscriptionIdMatchedAggrIdObjQuery);

            assertEquals("[]", result.toString());
        } catch (IOException | JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testUpdateSubscriptionAndCleanUpOfRepeatHandlerDb() {
        Subscription subscription;
        try {
            // Insert Subscription
            Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            String expectedSubscriptionName = subscription2.getSubscriptionName();
            subscriptionService.addSubscription(subscription2);
            // Fetch the inserted subscription
            subscription2 = null;
            subscription2 = subscriptionService.getSubscription(expectedSubscriptionName);
            subscriptionName = subscription2.getSubscriptionName();
            String subscriptionUserName = subscription2.getUserName();

            assertEquals(subscriptionName, expectedSubscriptionName);

            // Inserting a matched subscription AggrObjIds document to
            // RepeatHandlerDb database collection.
            BasicDBObject docInput = new BasicDBObject();
            docInput.put("subscriptionId", subscriptionName);
            mongoDBHandler.insertDocument(dataBaseName, repeatFlagHandlerCollection, docInput.toString());

            // Updating subscription2(subscriptionName=Subscription_Test) with
            // the subscription(subscriptionName=Subscription_Test_Modify)
            subscription = mapper.readValue(jsonArray.getJSONObject(1).toString(), Subscription.class);
            String expectedModifiedSubscriptionName = subscription2.getSubscriptionName();
            boolean addSubscription = subscriptionService.modifySubscription(subscription, subscriptionName);

            // test update done successfully
            assertEquals(addSubscription, true);
            subscription = null;
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
        } catch (Exception e) {
        }
    }

    @Test(expected = SubscriptionNotFoundException.class)
    public void testExceptionGetSubscriptionsByName() throws SubscriptionNotFoundException {
        subscriptionService.getSubscription("Subscription_Test1238586455");
    }

    private void deleteSubscriptionsByName(String subscriptionName) throws AccessException {
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getName()).thenReturn("ABC");
        subscriptionService.deleteSubscription(subscriptionName);
    }
}
