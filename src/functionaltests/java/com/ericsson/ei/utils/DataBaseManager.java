package com.ericsson.ei.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.stereotype.Component;

//import com.mongodb.MongoClient;
//import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;
import lombok.Getter;

@Component
public class DataBaseManager {
    private static final int MAX_WAIT_TIME_MILLISECONDS = 30000;
    private static final int RETRY_EVERY_X_MILLISECONDS = 1000;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event.object.map.collection.name}")
    private String eventObjectCollectionName;

    @Value("${aggregations.collection.name}")
    private String aggregatedCollectionName;

    @Value("${waitlist.collection.name}")
    private String waitlistCollectionName;

    @Value("${subscriptions.collection.name}")
    private String subscriptionCollectionName;

    @Getter
    @Autowired
    private MongoProperties mongoProperties;

    private MongoClient mongoClient;

    /**
     * Verify that aggregated object contains the expected information.
     *
     * @param checklist list of checklist to check
     * @return list of missing checklist
     * @throws InterruptedException
     */
    public List<String> verifyAggregatedObjectInDB(List<String> checklist)
            throws InterruptedException {
        long stopTime = System.currentTimeMillis() + MAX_WAIT_TIME_MILLISECONDS;
        while (!checklist.isEmpty() && stopTime > System.currentTimeMillis()) {
            checklist = getListOfArgumentsNotFoundInDatabase(checklist);
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
            List<Document> documents = getDocumentsFromCollection(aggregatedCollectionName);
            if (!documents.isEmpty()) {
                return true;
            }
            TimeUnit.MILLISECONDS.sleep(RETRY_EVERY_X_MILLISECONDS);
        }
        return false;
    }

    /**
     * Verify that events are located in the database collection.
     *
     * @param eventsIdList list of events IDs
     * @return list of missing events
     * @throws InterruptedException
     */
    public List<String> verifyEventsInDB(List<String> eventsIdList, int extraCheckDelay)
            throws InterruptedException {
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

    private List<String> getListOfArgumentsNotFoundInDatabase(final List<String> checklist) {
        final List<Document> documents = getDocumentsFromCollection(aggregatedCollectionName);

        List<String> foundValues = new ArrayList<>();
        for (Document document : documents) {
            final List<String> valuesFoundInDocument = getValuesFoundInDocument(checklist,
                    document);
            foundValues.addAll(valuesFoundInDocument);
        }

        List<String> result = new ArrayList<>(checklist);
        result.removeAll(foundValues);
        return result;
    }

    private List<String> getValuesFoundInDocument(final List<String> checklist,
            final Document document) {
        List<String> foundValues = new ArrayList<>();
        for (final String expectedValue : checklist) {
            if (document.toString().contains(expectedValue)) {
                foundValues.add(expectedValue);
            }
        }
        return foundValues;
    }

    /**
     * Checks collection of events against event list.
     *
     * @param checklist list of event IDs
     * @return list of missing events
     */
    private List<String> compareSentEventsWithEventsInDB(final List<String> checklist) {
        final List<Document> documents = getDocumentsFromCollection(eventObjectCollectionName);

        List<String> foundIDs = new ArrayList<>();
        for (Document document : documents) {
            final String documentId = document.get("_id").toString();
            foundIDs.add(documentId);
        }

        List<String> result = new ArrayList<>(checklist);
        result.removeAll(foundIDs);
        return result;
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
     * Get a specific subscription from the database based on the subscription name.
     *
     * @param subscriptionName
     * @return the document as JSON string
     */
    public String getSubscription(String subscriptionName) {
        MongoCollection<Document> collection = getCollection(subscriptionCollectionName);
        Bson filter = Filters.eq("subscriptionName", subscriptionName);
        Document document = collection.find(filter).first();
        return document.toJson();
    }

    /**
     * Returns the size of the waitlist.
     *
     * @return int of the size of the waitlist.
     */
    public int waitListSize() {
        List<Document> documents = getDocumentsFromCollection(waitlistCollectionName);
        return documents.size();
    }

    private List<Document> getDocumentsFromCollection(String collectionName) {
        MongoCollection<Document> collection = getCollection(collectionName);
        List<Document> documents = collection.find().into(new ArrayList<>());
        return documents;
    }

    private MongoCollection<Document> getCollection(String collectionName) {
        //MongoClientURI uri = new MongoClientURI(mongoProperties.getUri());
        //mongoClient = new MongoClient(uri);
        mongoClient = MongoClients.create(mongoProperties.getUri());
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> collection = db.getCollection(collectionName);
        return collection;
    }

}
