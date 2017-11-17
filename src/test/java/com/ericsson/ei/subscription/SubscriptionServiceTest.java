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
import static org.junit.Assert.assertTrue;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.services.ISubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SubscriptionServiceTest {

    String subscriptionName;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private static MongodForTestsFactory testsFactory;

    ObjectMapper mapper = new ObjectMapper();

    private static final String subscriptionJsonPath = "src/test/resources/subscription.json";

    static JSONArray jsonArray = null;
    static MongoClient mongoClient = null;

    @BeforeClass
    public static void setMongoDB() throws IOException, JSONException {
        testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
        String readFileToString = FileUtils.readFileToString(new File(subscriptionJsonPath), "UTF-8");
        jsonArray = new JSONArray(readFileToString);
        mongoClient = testsFactory.newMongo();

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getJSONObject(i).toString());
        }
    }

    @PostConstruct
    public void initMocks() {
        mongoDBHandler.setMongoClient(mongoClient);
    }

    @AfterClass
    public static void tearDownMongoDB() throws Exception {
        testsFactory.shutdown();
        mongoClient.close();
    }

    @Test
    public void testInsertSubscription() {
        Subscription subscription;
        try {
            subscription = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            boolean addSubscription = subscriptionService.addSubscription(subscription);
            assertEquals(addSubscription, true);
            // deleting the test data
            deleteSubscriptionsByName(subscription.getSubscriptionName());
        } catch (Exception e) {
        }
    }

    @Test
    public void testUpdateSubscription() {
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

            assertEquals(subscriptionName, expectedSubscriptionName);
            // Updating subscription2(subscriptionName=Subscription_Test) with
            // the subscription(subscriptionName=Subscription_Test_Modify)
            subscription = mapper.readValue(jsonArray.getJSONObject(1).toString(), Subscription.class);
            String expectedModifiedSubscriptionName = subscription2.getSubscriptionName();
            boolean addSubscription = subscriptionService.modifySubscription(subscription,
                    subscriptionName);

            // test update done successfully
            assertEquals(addSubscription, true);
            subscription = null;
            subscription = subscriptionService.getSubscription(expectedModifiedSubscriptionName);
            subscriptionName = subscription.getSubscriptionName();
            assertEquals(subscriptionName, expectedModifiedSubscriptionName);

            // deleting the test data
            deleteSubscriptionsByName(subscriptionName);
        } catch (Exception e) {
        }
    }

    @Test
    public void testGetAllSubscriptions() {
        List<Subscription> subscriptions;
        try {
            // Insert Subscription
            Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            subscriptionService.addSubscription(subscription2);

            subscriptions = subscriptionService.getSubscription();
            assertTrue(subscriptions.size() > 0);

            // deleting the test data
            deleteSubscriptionsByName(subscription2.getSubscriptionName());
        } catch (SubscriptionNotFoundException | IOException | JSONException e) {
        }
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
    public void testdoSubscriptionExist() {
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
    public void testDeleteSubscriptionsByName() {
        // Insert Subscription
        Subscription subscription2;
        try {
            subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);

            subscriptionService.addSubscription(subscription2);
            String expectedSubscriptionName = subscription2.getSubscriptionName();
            boolean deleteSubscription = subscriptionService.deleteSubscription(expectedSubscriptionName);
            assertEquals(deleteSubscription, true);
        } catch (IOException | JSONException e) {
        }
    }

    @Test(expected = SubscriptionNotFoundException.class)
    public void testExceptionGetSubscriptionsByName() throws SubscriptionNotFoundException {
        subscriptionService.getSubscription("Subscription_Test1238586455");
    }

    private void deleteSubscriptionsByName(String subscriptionName) {
        subscriptionService.deleteSubscription(subscriptionName);
    }
}
