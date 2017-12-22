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
package com.ericsson.ei.queryservice.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
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
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessMissedNotification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class QueryServiceTest {

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Value("${database.name}")
    private String aggregationDataBaseName;

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDataBaseName;

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    @Autowired
    private ProcessMissedNotification processMissedNotification;

    static Logger log = (Logger) LoggerFactory.getLogger(QueryServiceTest.class);

    private static String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static String missedNotificationPath = "src/test/resources/MissedNotification.json";
    private static String aggregatedObject;
    private static String missedNotification;

    private static MongodForTestsFactory testsFactory;
    static MongoClient mongoClient = null;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    public static void setUpEmbeddedMongo() throws Exception {
        testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
        mongoClient = testsFactory.newMongo();

        try {
            aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath));
            System.out.println("The aggregatedObject is : " + aggregatedObject);
            missedNotification = FileUtils.readFileToString(new File(missedNotificationPath));
            System.out.println("The missedNotification is : " + missedNotification);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    @BeforeClass
    public static void init() throws Exception {
        setUpEmbeddedMongo();
    }

    @PostConstruct
    public void initMocks() {
        mongoDBHandler.setMongoClient(mongoClient);
        System.out.println("Database connected");
        Document missedDocument = Document.parse(missedNotification);
        Document aggDocument = Document.parse(aggregatedObject);
        mongoClient.getDatabase(missedNotificationDataBaseName).getCollection(missedNotificationCollectionName)
                .insertOne(missedDocument);
        System.out.println("Document Inserted in missed Notification Database");
        mongoClient.getDatabase(aggregationDataBaseName).getCollection(aggregationCollectionName)
                .insertOne(aggDocument);
        System.out.println("Document Inserted in Aggregated Object Database");
    }

    @Test
    public void processMissedNotificationTest() {
        Iterable<Document> responseDB = mongoClient.getDatabase(missedNotificationDataBaseName)
                .getCollection(missedNotificationCollectionName).find();
        Iterator itr = responseDB.iterator();
        String response = itr.next().toString();
        log.info("The inserted doc is : " + response);
        ArrayList<String> result = processMissedNotification.processQueryMissedNotification("Subscription_1");
        log.info("The retrieved data is : " + result.toString());
        ObjectNode record = null;
        JsonNode actual = null;
        try {
            JsonNode tempRecord = new ObjectMapper().readTree(result.get(0));
            record = (ObjectNode) tempRecord;
            record.remove("_id");
            actual = new ObjectMapper().readTree(missedNotification).path("AggregatedObject");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("The result is : " + record.toString());
        assertEquals(record.toString(), actual.toString());
    }

    @Test
    public void processAggregatedObjectTest() {
        Iterable<Document> responseDB = mongoClient.getDatabase(aggregationDataBaseName)
                .getCollection(aggregationCollectionName).find();
        Iterator itr = responseDB.iterator();
        String response = itr.next().toString();
        log.info("The inserted doc is : " + response);
        ArrayList<String> result = processAggregatedObject
                .processQueryAggregatedObject("6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");
        ObjectNode record = null;
        JsonNode actual = null;
        try {
            JsonNode tempRecord = new ObjectMapper().readTree(result.get(0));
            record = (ObjectNode) tempRecord;
            record.remove("_id");
            actual = new ObjectMapper().readTree(aggregatedObject);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("The result is : " + record.toString());
        assertEquals(record.toString(), actual.toString());
    }

}
