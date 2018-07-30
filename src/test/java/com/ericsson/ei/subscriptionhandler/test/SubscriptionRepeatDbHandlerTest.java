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
package com.ericsson.ei.subscriptionhandler.test;


import com.ericsson.ei.App;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.subscriptionhandler.SubscriptionRepeatDbHandler;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {App.class})
public class SubscriptionRepeatDbHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRepeatDbHandlerTest.class);

    @Autowired
    private SubscriptionRepeatDbHandler subsRepeatDbHandler;
    private static String subRepeatFlagDataBaseName = "eiffel_intelligence";
    private static String subRepeatFlagCollectionName = "subscription_repeat_handler";

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @After
    public void beforeTests() {
        mongoDBHandler.dropCollection(subRepeatFlagDataBaseName, subRepeatFlagCollectionName);
    }

    @Test
    public void addOneNewMatchedAggrIdToDatabase() {
        String subscriptionId = "12345";
        int requirementId = 0;
        String aggrObjId = "99999";
        String subscriptionQuery = "{\"subscriptionId\" : \"" + subscriptionId + "\"}";
        try {
            subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId, requirementId, aggrObjId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        BasicDBObject dbResult = (BasicDBObject) JSON.parse(mongoDBHandler.find(subRepeatFlagDataBaseName, subRepeatFlagCollectionName, subscriptionQuery).get(0).toString());
        assertEquals(subscriptionId, dbResult.get("subscriptionId").toString());
        String actual = dbResult.get("requirements").toString();
        String expected = "{ \"0\" : [ " + "\"" + aggrObjId + "\"" + "]}";
        boolean result = true;
        if (expected == actual) {
            result = false;
            LOGGER.error("\nACTUAL: |" + actual + "|\nEXPECTED: |" + expected + "|");

        }
        assertTrue(result);
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
        try {
            subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId, requirementId, aggrObjId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        try {
            subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId2, requirementId2, aggrObjId2);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        BasicDBObject dbResult = (BasicDBObject) JSON.parse(mongoDBHandler.find(subRepeatFlagDataBaseName, subRepeatFlagCollectionName, subscriptionQuery2).get(0).toString());
        assertEquals(subscriptionId2, dbResult.get("subscriptionId").toString());
        String actual = dbResult.get("requirements").toString();
        String expected = "{ \"" + requirementId2 + "\" : [ \"" + aggrObjId2 + "\"]}";
        String msg = "\nACTUAL  : |" + actual + "|\nEXPECTED: |" + expected + "|";
        boolean result = true;
        if (!expected.equals(actual)) {
            result = false;
            LOGGER.error(msg);
        }
        assertTrue(msg, result);
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
            LOGGER.error(e.getMessage());
        }
        try {
            subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId2, requirementId2, aggrObjId2);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        BasicDBObject dbResult = (BasicDBObject) JSON.parse(mongoDBHandler.find(subRepeatFlagDataBaseName, subRepeatFlagCollectionName, subscriptionQuery).get(0).toString());
        assertEquals(subscriptionId2, dbResult.get("subscriptionId").toString());
        LOGGER.debug("DB REQUIREMENTS: " + dbResult.get("requirements").toString());
        LOGGER.debug("DB Content: " + dbResult.toString());
        String actual = dbResult.get("requirements").toString();
        String expected = "{ \"0\" : [ \"" + aggrObjId + "\" , \"" + aggrObjId2 + "\"]}";
        String msg = "\nACTUAL  : |" + actual + "|\nEXPECTED: |" + expected + "|";
        boolean result = true;
        if (!expected.equals(actual)) {
            result = false;
            LOGGER.error(msg);
        }
        assertTrue(msg, result);
    }
}