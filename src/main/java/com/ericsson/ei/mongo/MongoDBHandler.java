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
package com.ericsson.ei.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.stereotype.Component;

import com.ericsson.ei.exception.AbortExecutionException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoSocketWriteException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;

import lombok.Getter;
import lombok.Setter;

@Component
public class MongoDBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBHandler.class);

    @Getter
    @Autowired
    private MongoProperties mongoProperties;

    @Getter
    @Setter
    @JsonIgnore
    private MongoClient mongoClient;

    // TODO establish connection automatically when Spring instantiate this
    // based on connection data in properties file
    @PostConstruct
    public void init() throws AbortExecutionException {
        createMongoClient();
    }

    @PreDestroy
    public void close() {
        mongoClient.close();
    }

    /**
     * This method used for the insert the document into collection
     *
     * @param dataBaseName
     * @param collectionName
     * @param input          json String
     * @return
     */
    public void insertDocument(String dataBaseName, String collectionName, String input)
            throws MongoWriteException {
        MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
        if (collection != null) {
            final Document dbObjectInput = Document.parse(input);
            collection.insertOne(dbObjectInput);
            LOGGER.debug(
                    "Object: {}\n was inserted successfully in collection: {} and database {}.",
                    input, collectionName, dataBaseName);
        }
    }

    /**
     * This method is used for the retrieve the all documents from the collection
     *
     * @param dataBaseName
     * @param collectionName
     * @return
     */
    public ArrayList<String> getAllDocuments(String dataBaseName, String collectionName) {
        ArrayList<String> result = new ArrayList<>();
        try {
            MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
            if (collection != null) {
                FindIterable<Document> foundResults = collection.find();
                for (Document document : foundResults) {
                    result.add(JSON.serialize(document));
                }

                if (result.size() != 0) {
                    // This will pass about 10 times/second and most of the times DB will be empty,
                    // this is normal, no need to log
                    LOGGER.debug(
                            "getAllDocuments() :: database: {} and collection: {} fetched No of : {}",
                            dataBaseName, collectionName, result.size());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve documents.", e);
        }
        return result;
    }

    /**
     * This method is used for the retrieve the documents based on the condition
     *
     * @param dataBaseName
     * @param collectionName
     * @param query
     * @return
     */
    public ArrayList<String> find(String dataBaseName, String collectionName,
            MongoQuery query) {
        ArrayList<String> result = new ArrayList<>(0);

        try {
            result = doFind(dataBaseName, collectionName, query);
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve documents.", e);
        }

        return result;
    }

    /**
     * This method is used for update the document in collection and remove the lock in one query.
     * Lock is needed for multi process execution
     *
     * @param dataBaseName
     * @param collectionName
     * @param queryFilter    is a json string
     * @param updateInput    is updated document without lock
     * @return
     */
    public boolean updateDocument(String dataBaseName, String collectionName,
            MongoQuery queryFilter,
            String updateInput) {
        try {
            return doUpdate(dataBaseName, collectionName, queryFilter, updateInput);
        } catch (Exception e) {
            LOGGER.error("Failed to update document.", e);
        }

        return false;
    }

    /**
     * This method is used for lock and return the document that matches the input condition in one
     * query. Lock is needed for multi process execution. This method is executed in a loop.
     *
     * @param dataBaseName
     * @param collectionName
     * @param queryFilter    is a condition for update documents
     * @param updateInput    is updated document without lock
     * @return
     */
    public Document findAndModify(String dataBaseName, String collectionName,
            MongoQuery queryFilter,
            String updateInput) {
        try {
            return doFindAndModify(dataBaseName, collectionName, queryFilter, updateInput);
        } catch (Exception e) {
            LOGGER.error("Failed to update document.", e);
        }
        return null;
    }

    /**
     * This method is used for the delete documents from collection using a condition
     *
     * @param dataBaseName
     * @param collectionName
     * @param query
     * @return
     */
    public boolean dropDocument(String dataBaseName, String collectionName, MongoQuery query) {
        try {
            boolean result = doDrop(dataBaseName, collectionName, query);
            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * This method is used for the create time to live index
     *
     * @param dataBaseName
     * @param collectionName
     * @param fieldName      for index creation field
     * @param ttlValue       seconds
     */
    public void createTTLIndex(String dataBaseName, String collectionName, String fieldName,
            int ttlValue) {
        MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
        IndexOptions indexOptions = new IndexOptions().expireAfter((long) ttlValue,
                TimeUnit.SECONDS);
        collection.createIndex(Indexes.ascending(fieldName), indexOptions);
    }

    /**
     * This method is used to drop a collection.
     *
     * @param dataBaseName   to know which database to drop a collection from
     * @param collectionName to know which collection to drop
     */
    public void dropCollection(String dataBaseName, String collectionName) {
        MongoDatabase db = mongoClient.getDatabase(dataBaseName);
        MongoCollection<Document> mongoCollection = db.getCollection(collectionName);
        mongoCollection.drop();
    }

    /**
     * This method is used to drop a database. For example after testing.
     *
     * @param dataBaseName to know which database to remove
     */
    public void dropDatabase(String databaseName) {
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        db.drop();
    }

    /**
     * Returns a boolean indicating if MongoDB is up and running or not.
     *
     * @return
     */
    public boolean isMongoDBServerUp() {
        try {
            mongoClient.getAddress();
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private void createMongoClient() throws AbortExecutionException {
        if (StringUtils.isBlank(mongoProperties.getUri())) {
            throw new MongoConfigurationException(
                    "Failure to create MongoClient, missing config for spring.data.mongodb.uri:");
        }

        MongoClientURI uri = new MongoClientURI(mongoProperties.getUri());
        mongoClient = new MongoClient(uri);
    }

    private ArrayList<String> doFind(String dataBaseName, String collectionName,
            MongoQuery query) {
        LOGGER.debug(
                "Find and retrieve data from database.\nDatabase: {}\nCollection: {}\nCondition/Query: {}",
                dataBaseName, collectionName, query.getQueryString());

        ArrayList<String> result = new ArrayList<>();

        MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
        if (collection == null) {
            LOGGER.debug("Collection {} is empty in database {}", collectionName, dataBaseName);
            return result;
        }

        BasicDBObject conditionsAsDbObject = BasicDBObject.parse(query.getQueryString());
        FindIterable<Document> foundResults = collection.find(conditionsAsDbObject);
        for (Document document : foundResults) {
            // Currently document.toJson() does not work here since something will add \\\ before
            // all " later on, All get sometihng in mongoDB shoult redurn a JSON object and not a
            // String.
            result.add(JSON.serialize(document));
        }

        if (result.size() != 0) {
            LOGGER.debug("find() :: database: {} and collection: {} fetched No of : {}",
                    dataBaseName, collectionName, result.size());
        } else {
            LOGGER.debug(
                    "find() :: database: {} and collection: {} documents are not found",
                    dataBaseName, collectionName);
        }

        return result;
    }

    private Document doFindAndModify(String dataBaseName, String collectionName,
            MongoQuery queryFilter,
            String updateInput) {
        MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
        if (collection == null) {
            return null;
        }
        final Document dbObjectInput = Document.parse(queryFilter.getQueryString());
        final Document dbObjectUpdateInput = Document.parse(updateInput);
        Document result = collection.findOneAndUpdate(dbObjectInput, dbObjectUpdateInput);
        if (result != null) {
            LOGGER.debug(
                    "updateDocument() :: database: {} and collection: {} updated successfully",
                    dataBaseName, collectionName);
        }
        return result;
    }

    private boolean doUpdate(String dataBaseName, String collectionName, MongoQuery queryFilter,
            String updateInput) {
        MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
        if (collection == null) {
            return false;
        }

        final Document dbObjectInput = Document.parse(queryFilter.getQueryString());
        final Document dbObjectUpdateInput = Document.parse(updateInput);
        UpdateResult updateOne = collection.replaceOne(dbObjectInput, dbObjectUpdateInput);
        boolean updateWasPerformed = updateOne.wasAcknowledged()
                && updateOne.getModifiedCount() > 0;
        LOGGER.debug(
                "updateDocument() :: database: {} and collection: {} is document Updated : {}",
                dataBaseName, collectionName, updateWasPerformed);
        return updateWasPerformed;
    }

    private boolean doDrop(String dataBaseName, String collectionName, MongoQuery query) {
        MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
        if (collection == null) {
            return false;
        }

        final Document dbObjectCondition = Document.parse(query.getQueryString());
        DeleteResult deleteMany = collection.deleteMany(dbObjectCondition);
        if (deleteMany.getDeletedCount() > 0) {
            LOGGER.debug("database: {} and collection: {} deleted No.of records {}",
                    dataBaseName, collectionName, deleteMany.getDeletedCount());
            return true;
        } else {
            LOGGER.debug("database {} and collection: {} No documents found to delete.",
                    dataBaseName, collectionName);
            return false;
        }

    }

    private MongoCollection<Document> getMongoCollection(String databaseName,
            String collectionName) {
        if (mongoClient == null) {
            return null;
        }

        try {
            verifyExistanceOfCollection(databaseName, collectionName);

            MongoDatabase db = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = db.getCollection(collectionName);
            return collection;
        } catch (MongoClientException e) {
            LOGGER.error("Failure when handling Mongo collection: {} ", e.getMessage(), e);
            return null;
        }

    }

    private void verifyExistanceOfCollection(String databaseName, String collectionName) {
        List<String> collectionList = getCollectionList(databaseName);
        if (!collectionList.contains(collectionName)) {
            createCollection(databaseName, collectionName);
        }
    }

    private List<String> getCollectionList(String databaseName) {
        try {
            MongoDatabase db = mongoClient.getDatabase(databaseName);
            List<String> collectionList = db.listCollectionNames().into(new ArrayList<String>());
            return collectionList;
        } catch (MongoInterruptedException | MongoSocketReadException | MongoSocketWriteException
                | MongoCommandException | IllegalStateException e) {
            String message = String.format("Failed to get Mongo collection list, Reason: %s",
                    e.getMessage());
            closeMongoDbConnection();
            throw new MongoClientException(message, e);
        }
    }

    private void createCollection(String databaseName, String collectionName) {
        try {
            LOGGER.debug(
                    "The requested database({}) / collection({}) not found in mongodb, Creating.",
                    databaseName, collectionName);

            MongoDatabase db = mongoClient.getDatabase(databaseName);
            db.createCollection(collectionName);

            LOGGER.debug("done....");
        } catch (MongoCommandException e) {
            checkIfCollectionExistError(databaseName, collectionName, e);
        }
    }

    /**
     * When multiple request on a collection is done within a short period of time a collection migh
     * already have been created between the time MongoDBHandler checked if it existed to the
     * creation of the collection. If so we ignore the error when trying to creeate a collection..
     *
     * @param databaseName
     * @param collectionName
     * @param e
     */
    private void checkIfCollectionExistError(String databaseName, String collectionName,
            MongoCommandException e) {
        String collectionExistsError = String.format("collection '%s.%s' already exists",
                databaseName, collectionName);

        if (e.getMessage().contains(collectionExistsError)) {
            LOGGER.warn("A {}.", collectionExistsError, e);
        } else {
            String message = String.format("Failed to create Mongo collection, Reason: %s",
                    e.getMessage());
            throw new MongoClientException(message, e);
        }
    }

    private void closeMongoDbConnection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
        mongoClient = null;
    }
}
