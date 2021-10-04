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

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.mongo.MongoCondition;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.mongodb.BasicDBObject;
//import com.mongodb.util.JSON;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: SubscriptionRepeatDbHandlerTest",
        "failed.notifications.collection.name: SubscriptionRepeatDbHandlerTest-failedNotifications",
        "rabbitmq.exchange.name: SubscriptionRepeatDbHandlerTest-exchange",
        "rabbitmq.queue.suffix: SubscriptionRepeatDbHandlerTest" })
@RunWith(SpringJUnit4ClassRunner.class)
public class SubscriptionRepeatDbHandlerTest extends FunctionalTestBase {

    static Logger log = LoggerFactory.getLogger(SubscriptionRepeatDbHandlerTest.class);

    private static SubscriptionRepeatDbHandler subsRepeatDbHandler = new SubscriptionRepeatDbHandler();

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private static String subRepeatFlagDataBaseName = "SubscriptionRepeatDbHandlerTest";
    private static String subRepeatFlagCollectionName = "subscription_repeat_handler";

    @PostConstruct
    public void init() throws Exception {
        subsRepeatDbHandler.mongoDbHandler = mongoDBHandler;
        subsRepeatDbHandler.dataBaseName = subRepeatFlagDataBaseName;
        subsRepeatDbHandler.collectionName = subRepeatFlagCollectionName;
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

        final MongoCondition subscriptionQuery = MongoCondition.subscriptionCondition(subscriptionId);

        subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId, requirementId, aggrObjId);

        BasicDBObject dbResult = BasicDBObject.parse(mongoDBHandler
                                                                          .find(subRepeatFlagDataBaseName,
                                                                                  subRepeatFlagCollectionName,
                                                                                  subscriptionQuery)
                                                                          .get(0)
                                                                          .toString());

        assertEquals(subscriptionId, dbResult.get("subscriptionId").toString());

        String actual = dbResult.get("requirements").toString();
        String expected = "{ \"0\" : [ " + "\"" + aggrObjId + "\"" + "]}";

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

        final MongoCondition subscriptionQuery2 = MongoCondition.subscriptionCondition(subscriptionId2);

        subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId, requirementId, aggrObjId);

        subsRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionId2, requirementId2, aggrObjId2);

        BasicDBObject dbResult = BasicDBObject.parse(mongoDBHandler
                                                                          .find(subRepeatFlagDataBaseName,
                                                                                  subRepeatFlagCollectionName,
                                                                                  subscriptionQuery2)
                                                                          .get(0)
                                                                          .toString());

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

        final MongoCondition subscriptionQuery = MongoCondition.subscriptionCondition(subscriptionId);

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

        BasicDBObject dbResult = BasicDBObject.parse(mongoDBHandler
                                                                          .find(subRepeatFlagDataBaseName,
                                                                                  subRepeatFlagCollectionName,
                                                                                  subscriptionQuery)
                                                                          .get(0)
                                                                          .toString());

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
