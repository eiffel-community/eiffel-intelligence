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

import com.ericsson.ei.App;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessMissedNotification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.bson.BsonDocument;
import org.bson.Document;
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

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class QueryServiceTest {

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(QueryServiceTest.class);

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Value("${spring.data.mongodb.database}")
    private String aggregationDataBaseName;

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDataBaseName;

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    @Autowired
    private ObjectHandler objectHandler;

    @Autowired
    private ProcessMissedNotification processMissedNotification;

    private static String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static String missedNotificationPath = "src/test/resources/MissedNotification.json";
    private static String aggregatedObject;
    private static String missedNotification;

    private static MongodForTestsFactory testsFactory;
    static MongoClient mongoClient = null;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    public static void setUpEmbeddedMongo() throws Exception {
        try {
            testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
            mongoClient = testsFactory.newMongo();
            String port = "" + mongoClient.getAddress().getPort();
            System.setProperty("spring.data.mongodb.port", port);

            aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath));
            LOG.debug("The aggregatedObject is : " + aggregatedObject);
            missedNotification = FileUtils.readFileToString(new File(missedNotificationPath));
            LOG.debug("The missedNotification is : " + missedNotification);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void init() throws Exception {
        setUpEmbeddedMongo();
    }

    @PostConstruct
    public void initMocks() {
        mongoDBHandler.setMongoClient(mongoClient);
        LOG.debug("Database connected");
        // deleting all documents before inserting
        mongoClient.getDatabase(aggregationDataBaseName).getCollection(aggregationCollectionName)
                .deleteMany(new BsonDocument());
        Document missedDocument = Document.parse(missedNotification);
        Document aggDocument = Document.parse(aggregatedObject);
        mongoClient.getDatabase(missedNotificationDataBaseName).getCollection(missedNotificationCollectionName)
                .insertOne(missedDocument);
        LOG.debug("Document Inserted in missed Notification Database");

        JsonNode preparedAggDocument = objectHandler.prepareDocumentForInsertion(aggDocument.getString("id"),
                aggregatedObject);
        aggDocument = Document.parse(preparedAggDocument.toString());
        mongoClient.getDatabase(aggregationDataBaseName).getCollection(aggregationCollectionName)
                .insertOne(aggDocument);
        LOG.debug("Document Inserted in Aggregated Object Database");
    }

    @Test
    public void processMissedNotificationTest() {
        Iterable<Document> responseDB = mongoClient.getDatabase(missedNotificationDataBaseName)
                .getCollection(missedNotificationCollectionName).find();
        Iterator itr = responseDB.iterator();
        String response = itr.next().toString();
        LOG.debug("The inserted doc is : " + response);
        List<String> result = processMissedNotification.processQueryMissedNotification("Subscription_1");
        LOG.debug("The retrieved data is : " + result.toString());
        ObjectNode record = null;
        JsonNode actual = null;

        try {
            JsonNode tempRecord = new ObjectMapper().readTree(result.get(0));
            record = (ObjectNode) tempRecord;
            record.remove("_id");
            actual = new ObjectMapper().readTree(missedNotification).path("AggregatedObject");

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.debug("The result is : " + record.toString());
        assertEquals(record.toString(), actual.toString());
    }

    @Test
    public void deleteMissedNotificationTest() {
        Iterable<Document> responseDB = mongoClient.getDatabase(missedNotificationDataBaseName)
            .getCollection(missedNotificationCollectionName).find();
        Iterator itr = responseDB.iterator();
        String response = itr.next().toString();
        LOG.debug("The inserted doc is : " + response);
        boolean removed = processMissedNotification.deleteMissedNotification("Subscription_1");
        assertEquals(true, removed);
        Iterable<Document> responseDBAfter = mongoClient.getDatabase(missedNotificationDataBaseName)
            .getCollection(missedNotificationCollectionName).find();
        assertEquals(false, responseDBAfter.iterator().hasNext());
    }

    @Test
    public void processAggregatedObjectTest() {
        Iterable<Document> responseDB = mongoClient.getDatabase(aggregationDataBaseName)
                .getCollection(aggregationCollectionName).find();
        Iterator itr = responseDB.iterator();
        String response = itr.next().toString();
        LOG.debug("The inserted doc is : " + response);
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
            LOG.error(e.getMessage(), e);
        }
        LOG.debug("The result is : " + record.toString());
        assertEquals(record.get("aggregatedObject").toString(), actual.toString());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (mongoClient != null)
            mongoClient.close();
        if (testsFactory != null)
            testsFactory.shutdown();
    }
}
