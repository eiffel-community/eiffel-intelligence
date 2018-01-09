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
package com.ericsson.ei.subscriptionhandler.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
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
import com.ericsson.ei.subscriptionhandler.RunSubscription;
import com.ericsson.ei.subscriptionhandler.SubscriptionHandler;
import com.ericsson.ei.subscriptionhandler.SubscriptionRepeatController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class SubscriptionHandlerTest {

    @Autowired
    private RunSubscription runSubscription;

    @Autowired
    private SubscriptionHandler handler;

    private static String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static String subscriptionPath = "src/test/resources/SubscriptionObject.json";
    private static String subscriptionRepeatFlagTruePath = "src/test/resources/SubscriptionObjectRepeatFlagTrue.json";
    private static String subscriptionRepeatFlagFalsePath = "src/test/resources/SubscriptionObjectRepeatFlagFalse.json";
    private static String aggregatedObject;
    private static String subscriptionData;
    private static String subscriptionRepeatFlagTrueData;
    private static String subscriptionRepeatFlagFalseData;

    static Logger log = (Logger) LoggerFactory.getLogger(SubscriptionHandlerTest.class);

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private static MongodForTestsFactory testsFactory;
    static MongoClient mongoClient = null;

    static String host = "localhost";
    static int port = 27017;
    private static String dataBaseName = "MissedNotification";
    private static String collectionName = "Notification";

    public static void setUpEmbeddedMongo() throws Exception {
        testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
        mongoClient = testsFactory.newMongo();

        try {
            aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath), "UTF-8");
            subscriptionData = FileUtils.readFileToString(new File(subscriptionPath), "UTF-8");
            subscriptionRepeatFlagTrueData = FileUtils.readFileToString(new File(subscriptionRepeatFlagTruePath), "UTF-8");
            subscriptionRepeatFlagFalseData = FileUtils.readFileToString(new File(subscriptionRepeatFlagFalsePath), "UTF-8");
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    @BeforeClass
    public static void init() throws Exception {
        setUpEmbeddedMongo();
    }
    
    @Before
    public void cleanSubscriptionsRepeatValueHashMap() {
    	SubscriptionRepeatController.getSubscriptionsHashMap().clear();
    }

    @PostConstruct
    public void initMocks() {
        mongoDBHandler.setMongoClient(mongoClient);
        System.out.println("Database connected");
    }


    @Test
    public void runSubscriptionOnObjectTest() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode subscriptionJson = null;
        ArrayNode requirementNode = null;
        Iterator<JsonNode> requirementIterator = null;
        try {
            subscriptionJson = mapper.readTree(subscriptionData);
            requirementNode = (ArrayNode) subscriptionJson.get("requirements");
            requirementIterator = requirementNode.elements();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        boolean output = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator,
                subscriptionJson);
        assertEquals(output, true);
    }
    
    @Test
    public void runSubscriptionOnObjectRepeatFlagFalseTest() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode subscriptionJson = null;
        ArrayNode requirementNode = null;
        Iterator<JsonNode> requirementIterator = null;
        Iterator<JsonNode> requirementIterator2 = null;
        try {
            subscriptionJson = mapper.readTree(subscriptionRepeatFlagFalseData);
            requirementNode = (ArrayNode) subscriptionJson.get("requirements");
            requirementIterator = requirementNode.elements();
            requirementIterator2 = requirementNode.elements();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        boolean output1 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator,
                subscriptionJson);
        boolean output2 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator2,
                subscriptionJson);
        assertEquals(true, output1);
        assertEquals(false, output2);
    }
    
    @Test
    public void runSubscriptionOnObjectRepeatFlagTrueTest() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode subscriptionJson = null;
        ArrayNode requirementNode = null;
        Iterator<JsonNode> requirementIterator = null;
        Iterator<JsonNode> requirementIterator2 = null;
        try {
            subscriptionJson = mapper.readTree(subscriptionRepeatFlagTrueData);
            requirementNode = (ArrayNode) subscriptionJson.get("requirements");
            requirementIterator = requirementNode.elements();
            requirementIterator2 = requirementNode.elements();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        boolean output1 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator,
                subscriptionJson);
        boolean output2 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator2,
                subscriptionJson);
        assertEquals(true, output1);
        assertEquals(true, output2);
    }

    @Test
    public void MissedNotificationTest() {
        handler.extractConditions(aggregatedObject, subscriptionData);
        Iterable<String> outputDoc = mongoDBHandler.getAllDocuments(dataBaseName, collectionName);
        Iterator itr = outputDoc.iterator();
        String data = itr.next().toString();
        JsonNode jsonResult = null;
        JsonNode expectedOutput = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            expectedOutput = mapper.readTree(aggregatedObject);
            jsonResult = mapper.readTree(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        JsonNode output = jsonResult.get("AggregatedObject");
        assertEquals(expectedOutput.toString(), output.toString());
    }

    @AfterClass
    public static void close() {
        testsFactory.shutdown();
        mongoClient.close();
    }
}
