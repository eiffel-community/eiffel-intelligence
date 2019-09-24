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
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessMissedNotification;
import com.ericsson.ei.test.utils.TestConfigs;
import com.ericsson.ei.utils.TestContextInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: QueryServiceTest",
        "failed.notification.database-name: QueryServiceRESTAPITest-failedNotifications",
        "rabbitmq.exchange.name: QueryServiceTest-exchange",
        "rabbitmq.consumerName: QueryServiceTest"})
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class QueryServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(QueryServiceTest.class);

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Value("${spring.data.mongodb.database}")
    private String aggregationDataBaseName;

    @Value("${failed.notification.collection-name}")
    private String failedNotificationCollectionName;

    @Value("${failed.notification.database-name}")
    private String failedNotificationDatabaseName;

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    @Autowired
    private ObjectHandler objectHandler;

    @Autowired
    private ProcessMissedNotification processMissedNotification;

    private static String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static String failedNotificationPath = "src/test/resources/MissedNotification.json";
    private static String aggregatedObject;
    private static String missedNotification;
    static MongoClient mongoClient = null;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @PostConstruct
    public void initMocks() throws Exception {
        initializeData();
        mongoClient = TestConfigs.getMongoClient();
        mongoDBHandler.setMongoClient(mongoClient);
        LOG.debug("Database connected");
        // deleting all documents before inserting
        mongoClient.getDatabase(aggregationDataBaseName).getCollection(aggregationCollectionName)
                .deleteMany(new BsonDocument());
        Document missedDocument = Document.parse(missedNotification);
        Document aggDocument = Document.parse(aggregatedObject);
        mongoClient.getDatabase(failedNotificationDatabaseName).getCollection(failedNotificationCollectionName)
                .insertOne(missedDocument);
        LOG.debug("Document Inserted in missed Notification Database");

        BasicDBObject preparedAggDocument = objectHandler.prepareDocumentForInsertion(aggDocument.getString("id"),
                aggregatedObject);
        aggDocument = Document.parse(preparedAggDocument.toString());
        mongoClient.getDatabase(aggregationDataBaseName).getCollection(aggregationCollectionName)
                .insertOne(aggDocument);
        LOG.debug("Document Inserted in Aggregated Object Database");
    }

    public void initializeData() throws Exception {
        aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath));
        LOG.debug("The aggregatedObject is : " + aggregatedObject);
        missedNotification = FileUtils.readFileToString(new File(failedNotificationPath));
        LOG.debug("The missedNotification is : " + missedNotification);
    }

    @Test
    public void processMissedNotificationTest() {
        Iterable<Document> responseDB = mongoClient.getDatabase(failedNotificationDatabaseName)
                .getCollection(failedNotificationCollectionName).find();
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
            actual = new ObjectMapper().readTree(missedNotification);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.debug("The result is : " + record.toString());
        assertEquals(record.toString(), actual.toString());
    }

    @Test
    public void deleteMissedNotificationTest() {
        Iterable<Document> responseDB = mongoClient.getDatabase(failedNotificationDatabaseName)
                .getCollection(failedNotificationCollectionName).find();
        Iterator itr = responseDB.iterator();
        String response = itr.next().toString();
        LOG.debug("The inserted doc is : " + response);
        boolean removed = processMissedNotification.deleteMissedNotification("Subscription_1");
        assertEquals(true, removed);
        Iterable<Document> responseDBAfter = mongoClient.getDatabase(failedNotificationDatabaseName)
                .getCollection(failedNotificationCollectionName).find();
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
        assertEquals(record.toString(), actual.toString());
    }
}
