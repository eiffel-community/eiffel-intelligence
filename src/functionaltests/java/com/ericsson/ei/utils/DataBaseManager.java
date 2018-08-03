package com.ericsson.ei.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;
import lombok.Getter;

@Component
public class DataBaseManager {

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String collection;

    @Value("${aggregated.collection.name}")
    private String aggregatedCollectionName;

    @Value("${waitlist.collection.name}")
    private String waitlistCollectionName;

    @Autowired
    private MongoProperties mongoProperties;

    private MongoClient mongoClient;

    public int getMongoDbPort() {
        return mongoProperties.getPort();
    }

    public String getMongoDbHost() {
        return mongoProperties.getHost();
    }

    /**
     * Verify that aggregated object contains the expected information.
     *
     * @param checklist
     *            list of checklist to check
     * @return list of missing checklist
     * @throws InterruptedException
     */
    public List<String> verifyAggregatedObjectInDB(List<String> checklist) throws InterruptedException {
        long stopTime = System.currentTimeMillis() + 30000;
        while (!checklist.isEmpty() && stopTime > System.currentTimeMillis()) {
            checklist = compareArgumentsWithAggregatedObjectInDB(checklist);
            if (checklist.isEmpty()) {
                break;
            }
            TimeUnit.MILLISECONDS.sleep(1000);
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
        long stopTime = System.currentTimeMillis() + 30000;
        while (stopTime > System.currentTimeMillis()) {
            mongoClient = new MongoClient(getMongoDbHost(), getMongoDbPort());
            MongoDatabase db = mongoClient.getDatabase(database);
            MongoCollection<Document> table = db.getCollection(aggregatedCollectionName);
            List<Document> documents = table.find().into(new ArrayList<>());
            TimeUnit.MILLISECONDS.sleep(1000);
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
        mongoClient = new MongoClient(getMongoDbHost(), getMongoDbPort());
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> table = db.getCollection(aggregatedCollectionName);
        List<Document> documents = table.find().into(new ArrayList<>());
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
    public List<String> verifyEventsInDB(List<String> eventsIdList) throws InterruptedException {
        long stopTime = System.currentTimeMillis() + 30000;
        while (!eventsIdList.isEmpty() && stopTime > System.currentTimeMillis()) {
            eventsIdList = compareSentEventsWithEventsInDB(eventsIdList);
            if (eventsIdList.isEmpty()) {
                break;
            }
            TimeUnit.MILLISECONDS.sleep(1000);
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
        mongoClient = new MongoClient(getMongoDbHost(), getMongoDbPort());
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> table = db.getCollection(collection);
        List<Document> documents = table.find().into(new ArrayList<>());
        for (Document document : documents) {
            for (String expectedID : new ArrayList<>(checklist)) {
                if (expectedID.equals(document.get("_id").toString())) {
                    checklist.remove(expectedID);
                }
            }
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
        mongoClient = new MongoClient(getMongoDbHost(), getMongoDbPort());
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> table = db.getCollection(waitlistCollectionName);
        List<Document> documents = table.find().into(new ArrayList<>());
        return documents.size();
    }
}
