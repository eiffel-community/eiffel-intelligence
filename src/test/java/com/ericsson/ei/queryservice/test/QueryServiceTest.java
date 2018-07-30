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

import com.ericsson.ei.App;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessMissedNotification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = App.class)
public class QueryServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(QueryServiceTest.class);

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

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private static String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static String missedNotificationPath = "src/test/resources/MissedNotification.json";
    private static String aggregatedObject;
    private static String missedNotification;

    @BeforeClass
    public static void init() throws IOException {
        aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath), "UTF-8");
        LOG.debug("The aggregatedObject is : " + aggregatedObject);
        missedNotification = FileUtils.readFileToString(new File(missedNotificationPath), "UTF-8");
        LOG.debug("The missedNotification is : " + missedNotification);
    }

    @PostConstruct
    public void initMocks() {
        // deleting all documents before inserting
        mongoDBHandler.dropCollection(aggregationDataBaseName, aggregationCollectionName);
        mongoDBHandler.insertDocument(missedNotificationDataBaseName, missedNotificationCollectionName, missedNotification);
        LOG.debug("Document Inserted in missed Notification Database");
        Document aggDocument = Document.parse(aggregatedObject);
        JsonNode preparedAggDocument = objectHandler.prepareDocumentForInsertion(aggDocument.getString("id"),
                aggregatedObject);
        mongoDBHandler.insertDocument(aggregationDataBaseName, aggregationCollectionName, preparedAggDocument.toString());
        LOG.debug("Document Inserted in Aggregated Object Database");
    }

    @Test
    public void processMissedNotificationTest() {
        Iterator itr = mongoDBHandler.getAllDocuments(missedNotificationDataBaseName, missedNotificationCollectionName).iterator();
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
        Iterator itr = mongoDBHandler.getAllDocuments(missedNotificationDataBaseName, missedNotificationCollectionName).iterator();
        String response = itr.next().toString();
        LOG.debug("The inserted doc is : " + response);
        boolean removed = processMissedNotification.deleteMissedNotification("Subscription_1");
        assertTrue(removed);
        Iterator responseDBAfter = mongoDBHandler.getAllDocuments(missedNotificationDataBaseName, missedNotificationCollectionName).iterator();
        assertFalse(responseDBAfter.hasNext());
    }

    @Test
    public void processAggregatedObjectTest() {
        Iterator itr = mongoDBHandler.getAllDocuments(aggregationDataBaseName, aggregationCollectionName).iterator();
        String response = itr.next().toString();
        LOG.debug("The inserted doc is : " + response);
        List<String> result = processAggregatedObject
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
}