package com.ericsson.ei.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;
import lombok.Getter;

@Component
public class DataBaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataBaseManager.class);

    private static final int MAX_WAIT_TIME_MILLISECONDS = 30000;
    private static final int RETRY_EVERY_X_MILLISECONDS = 1000;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String eventObjectCollectionName;

    @Value("${aggregated.collection.name}")
    private String aggregatedCollectionName;

    @Value("${waitlist.collection.name}")
    private String waitlistCollectionName;

    @Getter
    @Autowired
    private MongoProperties mongoProperties;

    MongoClient mongoClient;

    /**
     * Verify that aggregated object contains the expected information.
     *
     * @param checklist
     *            list of checklist to check
     * @return list of missing checklist
     * @throws InterruptedException
     */
    public List<String> verifyAggregatedObjectInDB(List<String> checklist) throws InterruptedException {
        long stopTime = System.currentTimeMillis() + MAX_WAIT_TIME_MILLISECONDS;
        while (!checklist.isEmpty() && stopTime > System.currentTimeMillis()) {
            checklist = compareArgumentsWithAggregatedObjectInDB(checklist);
            if (checklist.isEmpty()) {
                break;
            }
            TimeUnit.MILLISECONDS.sleep(RETRY_EVERY_X_MILLISECONDS);
        }
        return checklist;
    }

    /**
     * Verify that aggregated objects exists or not.
     *
     * @return boolean whether aggregated objects exists or not.
     * @throws InterruptedException
     */
    public boolean verifyAggregatedObjectExistsInDB() throws InterruptedException {
        long stopTime = System.currentTimeMillis() + MAX_WAIT_TIME_MILLISECONDS;
        while (stopTime > System.currentTimeMillis()) {
            MongoCollection<Document> collection = getCollection(aggregatedCollectionName);
            List<Document> documents = collection.find().into(new ArrayList<>());
            TimeUnit.MILLISECONDS.sleep(RETRY_EVERY_X_MILLISECONDS);
            if (!documents.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks that aggregated object contains specified arguments.
     *
     * @param checklist
     *            list of arguments
     * @return list of missing arguments
     */
    private List<String> compareArgumentsWithAggregatedObjectInDB(List<String> checklist) {
        MongoCollection<Document> collection = getCollection(aggregatedCollectionName);
        List<Document> documents = collection.find().into(new ArrayList<>());
        for (Document document : documents) {
            for (String expectedValue : new ArrayList<>(checklist)) {
                if (document.toString().contains(expectedValue)) {
                    checklist.remove(expectedValue);
                }
            }
        }
        return checklist;
    }

    /**
     * Verify that events are located in the database collection.
     *
     * @param eventsIdList
     *            list of events IDs
     * @return list of missing events
     * @throws InterruptedException
     */
    public List<String> verifyEventsInDB(List<String> eventsIdList, int extraCheckDelay) throws InterruptedException {
        long stopTime = System.currentTimeMillis() + MAX_WAIT_TIME_MILLISECONDS + extraCheckDelay;
        while (!eventsIdList.isEmpty() && stopTime > System.currentTimeMillis()) {
            eventsIdList = compareSentEventsWithEventsInDB(eventsIdList);
            if (eventsIdList.isEmpty()) {
                break;
            }
            TimeUnit.MILLISECONDS.sleep(RETRY_EVERY_X_MILLISECONDS);
        }
        return eventsIdList;
    }

    /**
     * Checks collection of events against event list.
     *
     * @param checklist
     *            list of event IDs
     * @return list of missing events
     */
    private List<String> compareSentEventsWithEventsInDB(List<String> checklist) {
        try {
            MongoCollection<Document> collection = getCollection(eventObjectCollectionName);
            List<Document> documents = collection.find().into(new ArrayList<>());

            for (Document document : documents) {
                for (String expectedID : new ArrayList<>(checklist)) {
                    if (expectedID.equals(document.get("_id").toString())) {
                        checklist.remove(expectedID);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get documents ", e);
        }
        return checklist;
    }

    /**
     * Retrieve a value from a database query result
     *
     * @param key
     * @param index
     * @return String value matching the given key
     *
     */
    public String getValueFromQuery(List<String> databaseQueryResult, String key, int index) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(databaseQueryResult.get(index)).getAsJsonObject();
        return jsonObject.get(key).toString();
    }

    /**
     * Returns the size of the waitlist.
     *
     * @return int of the size of the waitlist.
     */
    public int waitListSize() {
        MongoCollection<Document> collection = getCollection(waitlistCollectionName);
        List<Document> documents = collection.find().into(new ArrayList<>());
        return documents.size();
    }

    private MongoCollection<Document> getCollection(String collectionName) {
        MongoClientURI uri = new MongoClientURI(mongoProperties.getUri());
        mongoClient = new MongoClient(uri);
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionName);
        return collection;
    }
}
