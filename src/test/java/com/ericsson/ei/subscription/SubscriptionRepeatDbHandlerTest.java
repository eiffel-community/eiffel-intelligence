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
package com.ericsson.ei.subscription;


import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        App.class
    })
public class SubscriptionRepeatDbHandlerTest {


    static Logger log = (Logger) LoggerFactory.getLogger(SubscriptionRepeatDbHandlerTest.class);

    private static SubscriptionRepeatDbHandler subsRepeatDbHandler = new SubscriptionRepeatDbHandler();

    @Autowired
    private static MongoDBHandler mongoDBHandler;

    private static MongoClient mongoClient;
    private static MongodForTestsFactory testsFactory;

    private static String subRepeatFlagDataBaseName = "eiffel_intelligence";
    private static String subRepeatFlagCollectionName = "subscription_repeat_handler";




    @BeforeClass
    public static void init() throws Exception {
    testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
    mongoClient = testsFactory.newMongo();
    mongoDBHandler = new MongoDBHandler();
    mongoDBHandler.setMongoClient(mongoClient);
    String port = "" + mongoClient.getAddress().getPort();
    System.setProperty("spring.data.mongodb.port", port);
    subsRepeatDbHandler.mongoDbHandler = mongoDBHandler;

    subsRepeatDbHandler.dataBaseName = subRepeatFlagDataBaseName;
    subsRepeatDbHandler.collectionName = subRepeatFlagCollectionName;
    }

    @AfterClass
    public static void close() {
        testsFactory.shutdown();
        mongoClient.close();
    }

    @Before
    public void beforeTests() {
        mongoDBHandler.dropCollection(subRepeatFlagDataBaseName, subRepeatFlagCollectionName);
    }

    @Test
    public void addOneNewMatchedAggrIdToDatabase() {

        String subscriptionId = "12345";
        int requirementId = 0;
        String aggrObjId = "99999";

        String subscriptionQuery = "{\"subscriptionId\" : \"" + subscriptionId + "\"}";

        subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId, requirementId, aggrObjId);

        BasicDBObject dbResult = (BasicDBObject) JSON.parse(mongoDBHandler.find(subRepeatFlagDataBaseName, subRepeatFlagCollectionName, subscriptionQuery).get(0).toString());

        assertEquals(subscriptionId, dbResult.get("subscriptionId").toString());

        String actual = dbResult.get("requirements").toString();
        String expected = "{ \"0\" : [ " +  "\"" + aggrObjId + "\"" + "]}";


        boolean result = true;
        if (expected == actual) {
            result = false;
            log.error("\nACTUAL: |" + actual + "|\nEXPECTED: |" + expected + "|");

        }
        assertEquals(true, result);
    }

    @Test
    public void addTwoNewMatchedAggrIdToDatabase() {

        String subscriptionId = "12345";
        int requirementId = 0;
        String aggrObjId = "99999";

          String subscriptionId2 = "98754";
          int requirementId2 = 1;
        String aggrObjId2 = "45678";

        String subscriptionQuery2 = "{\"subscriptionId\" : \"" + subscriptionId2 + "\"}";

        subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId, requirementId, aggrObjId);

        subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId2, requirementId2, aggrObjId2);

        BasicDBObject dbResult = (BasicDBObject) JSON.parse(mongoDBHandler.find(subRepeatFlagDataBaseName, subRepeatFlagCollectionName, subscriptionQuery2).get(0).toString());

        assertEquals(subscriptionId2, dbResult.get("subscriptionId").toString());

        String actual = dbResult.get("requirements").toString();
        String expected = "{ \"" + requirementId2 + "\" : [ \"" + aggrObjId2 + "\"]}";


        String msg = "\nACTUAL  : |" + actual + "|\nEXPECTED: |" + expected + "|";
        boolean result = true;
        if (!expected.equals(actual)) {
            result = false;
            log.error(msg);
        }

        assertEquals(msg, result, true);
    }

    @Test
    public void addTwoNewSameMatchedAggrIdToDatabase() {

        String subscriptionId = "12345";
        int requirementId = 0;
        String aggrObjId = "99999";

          String subscriptionId2 = "12345";
          int requirementId2 = 0;
        String aggrObjId2 = "99998";

        String subscriptionQuery = "{\"subscriptionId\" : \"" + subscriptionId + "\"}";


        try {
            subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId, requirementId, aggrObjId);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        try {
            subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId2, requirementId2, aggrObjId2);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        BasicDBObject dbResult = (BasicDBObject) JSON.parse(mongoDBHandler.find(subRepeatFlagDataBaseName, subRepeatFlagCollectionName, subscriptionQuery).get(0).toString());

        assertEquals(subscriptionId2, dbResult.get("subscriptionId").toString());
        log.error("DB REQUIREMENTS: " + dbResult.get("requirements").toString());
        log.error("DB Content: " + dbResult.toString());


        String actual = dbResult.get("requirements").toString();
        String expected = "{ \"0\" : [ \"" + aggrObjId + "\" , \"" + aggrObjId2 + "\"]}";
        String msg = "\nACTUAL  : |" + actual + "|\nEXPECTED: |" + expected + "|";
        boolean result = true;
        if (!expected.equals(actual)) {
            result = false;
            log.error(msg);
        }

        assertEquals(msg, result, true);
    }
}
