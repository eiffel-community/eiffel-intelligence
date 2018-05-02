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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.services.ISubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class SubscriptionServiceTest {

    final static Logger LOGGER = (Logger) LoggerFactory.getLogger(SubscriptionServiceTest.class);

    @Value("${spring.data.mongodb.database}") private String dataBaseName;
    
    @Value("${subscription.collection.repeatFlagHandlerName}") private String repeatFlagHandlerCollection;
    
    String subscriptionName;
    String userName = "ABC"; // initialized with "ABC", as test json file has this name as userName

    @Autowired
    private ISubscriptionService subscriptionService;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private static MongodForTestsFactory testsFactory;

    private ObjectMapper mapper = new ObjectMapper();

    private static final String subscriptionJsonPath = "src/test/resources/subscription_CLME.json";
    private static final String subscriptionJsonPath_du = "src/test/resources/subscription_single_differentUser.json";

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
    public void testInsertSubscription() {
        Subscription subscription;
        try {
            subscription = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            boolean addSubscription = subscriptionService.addSubscription(subscription);
            assertEquals(addSubscription, true);
            // deleting the test data
            deleteSubscriptionsByName(subscription.getSubscriptionName(), userName);
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
            String expectedUserName = subscription2.getUserName();
            subscriptionService.addSubscription(subscription2);
            // Fetch the inserted subscription
            subscription2 = null;
            subscription2 = subscriptionService.getSubscription(expectedSubscriptionName, userName);
            subscriptionName = subscription2.getSubscriptionName();

            assertEquals(subscriptionName, expectedSubscriptionName);
            assertEquals(userName, expectedUserName);
            // Updating subscription2(subscriptionName=Subscription_Test) with
            // the subscription(subscriptionName=Subscription_Test_Modify)
            subscription = mapper.readValue(jsonArray.getJSONObject(1).toString(), Subscription.class);
            String expectedModifiedSubscriptionName = subscription2.getSubscriptionName();

            boolean addSubscription = subscriptionService.modifySubscription(subscription, subscriptionName, userName);

            // test update done successfully
            assertEquals(addSubscription, true);
            subscription = null;
            subscription = subscriptionService.getSubscription(expectedModifiedSubscriptionName, userName);
            subscriptionName = subscription.getSubscriptionName();
            assertEquals(subscriptionName, expectedModifiedSubscriptionName);
            assertEquals(userName, expectedModifiedSubscriptionName);

            // deleting the test data
            deleteSubscriptionsByName(subscriptionName, userName);
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
            deleteSubscriptionsByName(subscription2.getSubscriptionName(), userName);
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
            subscription = subscriptionService.getSubscription(expectedSubscriptionName, userName);
            subscriptionName = subscription.getSubscriptionName();
            assertEquals(subscriptionName, expectedSubscriptionName);
            // deleting the test data
            deleteSubscriptionsByName(subscriptionName, userName);
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
            doSubscriptionExist = subscriptionService.doSubscriptionExist(subscriptionName, userName);
            assertEquals(doSubscriptionExist, true);
            // deleting the test data
            deleteSubscriptionsByName(subscriptionName, userName);
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
            boolean deleteSubscription = subscriptionService.deleteSubscription(expectedSubscriptionName, userName);
            assertEquals(deleteSubscription, true);
        } catch (IOException | JSONException e) {
        }
    }
    
    @Test
    public void testDeleteSubscriptionsByNameAndCleanUpOfRepeatHandlerDb() {
        // Insert Subscription
        Subscription subscription2;
        try {
            subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            
            
            subscriptionService.addSubscription(subscription2);
            String expectedSubscriptionName = subscription2.getSubscriptionName();
            
            // Inserting a matched subscription AggrObjIds document to RepeatHandlerDb database collection.
            BasicDBObject docInput = new BasicDBObject();
            docInput.put("subscriptionId", expectedSubscriptionName);
            mongoDBHandler.insertDocument(dataBaseName, RepeatFlagHandlerCollection, docInput.toString());
            
            boolean deleteSubscription = subscriptionService.deleteSubscription(expectedSubscriptionName);
            assertEquals(deleteSubscription, true);
            
            // Checking if it removes the Subscription Matched AggrObjIds document from RepeatHandlerDb database collection.
            String subscriptionIdMatchedAggrIdObjQuery =  "{ \"subscriptionId\" : \"" + expectedSubscriptionName + "\"}";
            ArrayList<String> result = mongoDBHandler.find(dataBaseName, RepeatFlagHandlerCollection, subscriptionIdMatchedAggrIdObjQuery);

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

          assertEquals(subscriptionName, expectedSubscriptionName);
          
          // Inserting a matched subscription AggrObjIds document to RepeatHandlerDb database collection.
          BasicDBObject docInput = new BasicDBObject();
          docInput.put("subscriptionId", subscriptionName);
          mongoDBHandler.insertDocument(dataBaseName, RepeatFlagHandlerCollection, docInput.toString());
          
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
          
          // Checking if it removes the Subscription Matched AggrObjIds document from RepeatHandlerDb database collection.
          String subscriptionIdMatchedAggrIdObjQuery =  "{ \"subscriptionId\" : \"" + subscriptionName + "\"}";
          ArrayList<String> result = mongoDBHandler.find(dataBaseName, RepeatFlagHandlerCollection, subscriptionIdMatchedAggrIdObjQuery);

          assertEquals("[]", result.toString());

          // deleting the test data
          deleteSubscriptionsByName(subscriptionName);
      } catch (Exception e) {
      }
  }
    

    @Test(expected = SubscriptionNotFoundException.class)
    public void testExceptionGetSubscriptionsByName() throws SubscriptionNotFoundException {
        subscriptionService.getSubscription("Subscription_Test1238586455", userName);
    }

    private void deleteSubscriptionsByName(String subscriptionName, String userName) {
        subscriptionService.deleteSubscription(subscriptionName, userName);
    }

    @Test
    public void testGetSubscriptionsForSpecificUser() {
        Subscription subscription;
        try {
            // Insert first Subscription
            Subscription subscription2 = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            subscriptionService.addSubscription(subscription2);
            // extracting subscription & user name.....must be "subscription_test" & "ABC"
            // respectively
            String expectedSubscriptionName = subscription2.getSubscriptionName();
            String expectedUserName = subscription2.getUserName();

            // inserting second subscription with the same name but different user
            // name(postfix "du" stands for....)
            Subscription subscription2_du = mapper.readValue(jsonArray_du.getJSONObject(0).toString(),
                    Subscription.class);
            subscriptionService.addSubscription(subscription2_du);
            // extracting subscription & user name.....must be "subscription_test" & "DEF"
            // respectively
            String expectedSubscriptionName_du = subscription2_du.getSubscriptionName();
            String expectedUserName_du = subscription2_du.getUserName();

            // extracting subscription from db with:getSubscription("subscription_test" ,
            // "ABC")
            subscription = subscriptionService.getSubscription(expectedSubscriptionName, expectedUserName);
            subscriptionName = subscription.getSubscriptionName();
            userName = subscription.getUserName();
            assertEquals(expectedSubscriptionName, subscriptionName);
            assertEquals(expectedUserName, userName);

            // extracting subscription from db with:getSubscription("subscription_test"
            // ,"DEF")
            subscription = subscriptionService.getSubscription(expectedSubscriptionName_du, expectedUserName_du);
            subscriptionName = subscription.getSubscriptionName();
            userName = subscription.getUserName();
            assertEquals(expectedSubscriptionName_du, subscriptionName);
            assertEquals(expectedUserName_du, userName);

            // deleting the test data
            deleteSubscriptionsByName(subscriptionName, expectedUserName);
            deleteSubscriptionsByName(subscriptionName, expectedUserName_du);

        } catch (SubscriptionNotFoundException | IOException | JSONException e) {
        }
    }

}
