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
import java.util.Map;
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
import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.handlers.DateUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientException;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoSocketWriteException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.MongoCursor;

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
     * @throws MongoWriteException, MongoClientException
     * @return
     */
    public void insertDocument(String dataBaseName, String collectionName, String input)
            throws MongoWriteException, MongoClientException {

    	try {
    	    long start = System.currentTimeMillis();
            MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
            if (collection != null) {
                final Document dbObjectInput = Document.parse(input);
                collection.insertOne(dbObjectInput);
                long stop = System.currentTimeMillis();
                LOGGER.debug("#### Response time to insert the document in ms: {} ", stop-start);
                LOGGER.debug(
                        "Object: {}\n was inserted successfully in collection: {} and database {}.",
                        input, collectionName, dataBaseName);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to insert Object: {} \n in collection: {} and database {}. \n {}", input,
                    collectionName, dataBaseName, e.getMessage());
        }
    }
    
    /**
     * This method is used to insert the Document object into collection
     * 
     * @param dataBaseName
     * @param collectionName
     * @param document - Document object to insert
     * @throws MongoWriteException
     */
    public void insertDocumentObject(String dataBaseName, String collectionName, Document document) throws MongoWriteException {
        try {
            MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);           

            if (collection != null) {
                long start = System.currentTimeMillis();
                collection.insertOne(document);
                long stop = System.currentTimeMillis();
                LOGGER.debug("#### Response time to insert the document in ms: {} ", stop-start);
                LOGGER.debug("Object: {}\n was inserted successfully in collection: {} and database {}.", document,
                        collectionName, dataBaseName);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to insert Object: {} \n in collection: {} and database {}. \n {}", document,
                    collectionName, dataBaseName, e.getMessage());
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
                	BasicDBObject basicDBObject=new BasicDBObject(document);
                	result.add(basicDBObject.toString());
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
            LOGGER.error("Failed to retrieve documents from the collection {} as {}", collectionName, e.getMessage());
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
            MongoQuery query) throws MongoClientException {
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
            String updateInput) throws MongoClientException {
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
     * @throws MongoDBConnectionException
     */
    public void createTTLIndex(String dataBaseName, String collectionName, String fieldName, int ttlValue)
            throws MongoDBConnectionException {
        try {
            MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
            IndexOptions indexOptions = new IndexOptions().expireAfter((long) ttlValue, TimeUnit.SECONDS);
            checkAndDropTTLIndex(collection, fieldName + "_1");
            LOGGER.debug("Creating the index for {} in collection: {}", fieldName, collection.getNamespace());
            collection.createIndex(Indexes.ascending(fieldName), indexOptions);
        } catch (Exception e) {
            throw new MongoDBConnectionException(e.getMessage());
        }
    }
    /**
     * This method is used to check and drop the TTL index for specific field.
     * 
     * @param collection - MongoCollection.
     * @param fieldName  - Field name for dropping the index.
     * @throws Exception
     */
    private void checkAndDropTTLIndex(final MongoCollection<Document> collection, String fieldName) throws Exception {
        // Verify if the index is present for the field in the collection.
        for (Document index : collection.listIndexes()) {
            for (Map.Entry<String, Object> entry : index.entrySet()) {
                Object value = entry.getValue();
                if (value.equals(fieldName)) {
                    LOGGER.debug("Dropping the index for {} in collection: {}", fieldName, collection.getNamespace());
                    collection.dropIndex(fieldName);
                    break;
                }
            }
        }
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
     * @param databaseName to know which database to remove
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
            ListDatabasesIterable<Document> list = mongoClient.listDatabases();
            MongoCursor<Document> iter = list.iterator(); 
            while (iter.hasNext()) {
                iter.getServerAddress();
                break;
            }
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
        mongoClient = MongoClients.create(mongoProperties.getUri());
    }

    private ArrayList<String> doFind(String dataBaseName, String collectionName,
            MongoQuery query) throws MongoClientException {
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
        	BasicDBObject basicDBObject=new BasicDBObject(document);
        	result.add(basicDBObject.toString());
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
            String updateInput) throws MongoClientException {
        long start = System.currentTimeMillis();
        MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
        if (collection == null) {
            return null;
        }
        final Document dbObjectInput = Document.parse(queryFilter.getQueryString());
        final Document dbObjectUpdateInput = Document.parse(updateInput);
        Document result = collection.findOneAndUpdate(dbObjectInput, dbObjectUpdateInput);
        if (result != null) {
            long stop = System.currentTimeMillis();
            LOGGER.debug("#### Response time to findAndModify the document in ms: {} ", stop-start);
            LOGGER.debug(
                    "updateDocument() :: database: {} and collection: {} updated successfully",
                    dataBaseName, collectionName);
        }
        return result;
    }

    private boolean doUpdate(String dataBaseName, String collectionName, MongoQuery queryFilter,
            String updateInput) throws MongoClientException {
        long start = System.currentTimeMillis();
        MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
        if (collection == null) {
            return false;
        }

        final Document dbObjectInput = Document.parse(queryFilter.getQueryString());
        final Document dbObjectUpdateInput = Document.parse(updateInput);
        UpdateResult updateOne = collection.replaceOne(dbObjectInput, dbObjectUpdateInput);
        long stop = System.currentTimeMillis();
        LOGGER.debug("#### Response time to update the document in ms: {} ", stop-start);
        boolean updateWasPerformed = updateOne.wasAcknowledged()
                && updateOne.getModifiedCount() > 0;
        LOGGER.debug(
                "updateDocument() :: database: {} and collection: {} is document Updated : {}",
                dataBaseName, collectionName, updateWasPerformed);
        return updateWasPerformed;
    }

    private boolean doDrop(String dataBaseName, String collectionName, MongoQuery query) throws MongoClientException {
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

    private MongoCollection<Document> getMongoCollection(String databaseName, String collectionName)
            throws MongoClientException {
        if (mongoClient == null) {
            throw new MongoClientException("Failed to connect MongoDB");
        }

        return getCollection(databaseName, collectionName);
    }

    private MongoCollection<Document> getCollection(String databaseName, String collectionName) throws MongoClientException {
        MongoCollection<Document> collection;
        MongoDatabase db;
        try {
            db = mongoClient.getDatabase(databaseName);
            collection = db.getCollection(collectionName);
        } catch (MongoInterruptedException | MongoSocketReadException | MongoSocketWriteException
                | MongoCommandException | IllegalStateException e) {
            String message = String.format("Failed to get Mongo collection, Reason: %s",
                    e.getMessage()); 
            throw new MongoClientException(message, e);
        }
        if(collection == null) {
            createCollection(databaseName, collectionName);
            collection = db.getCollection(collectionName);
        }
        return collection;
    }

    private void createCollection(String databaseName, String collectionName) throws MongoClientException {
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
            MongoCommandException e) throws MongoClientException {
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
    
    /**
     * Check if the document exists
     * @param databaseName
     * @param collectionName
     * @param condition      a condition to find a requested object in the database
     * @return
     */
    public boolean checkDocumentExists(String databaseName, String collectionName, MongoCondition condition) {

        try {
            long start = System.currentTimeMillis();
            MongoDatabase db = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> mongoCollection = db.getCollection(collectionName);
            Document doc = null;
            if (mongoCollection != null) {
                doc = mongoCollection.find(BasicDBObject.parse(condition.toString())).first();
            }
            if (doc == null || doc.isEmpty()) {
                return false;
            }
            long stop = System.currentTimeMillis();
            LOGGER.debug("#### Response time to checkDocumentExists in ms: {} ", stop-start);
        } catch (Exception e) {
            LOGGER.error("something wrong with MongoDB " + e);
            return false;
        }
        return true;
    }


    /**
     * Update the existing documents with unique objects list
     * Used only in EventToObjectMapHandler.java
     * @param dataBaseName
     * @param collectionName
     * @param condition      a condition to find a requested object in the database
     * @param eventId eventId to update in the mapper collection
     * @return 
     */
    public boolean updateDocumentAddToSet(String dataBaseName, String collectionName, MongoCondition condition, String eventId) {
        try {
            long start = System.currentTimeMillis();
            MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
            if (collection != null) {
                final Document dbObjectInput = Document.parse(condition.toString());
                UpdateResult updateMany = collection.updateOne(dbObjectInput, Updates.addToSet("objects", eventId));
                updateMany = collection.updateOne(dbObjectInput, Updates.set(MongoConstants.TIME, DateUtils.getDate()));
                long stop = System.currentTimeMillis();
                LOGGER.debug("#### Response time to updateDocumentAddToSet in ms: {} ", stop-start);
                LOGGER.debug("updateDocument() :: database: {} and collection: {} is document Updated : {}", dataBaseName, collectionName, updateMany.wasAcknowledged());
                return updateMany.wasAcknowledged();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to update document.", e);
        }

        return false;
    }
    
    public boolean checkMongoDbStatus(String dataBaseName) {
        MongoDatabase db;
        List<String> collectionList;
        try {
                if (mongoClient == null) {
                        createMongoClient();
                }
                db = mongoClient.getDatabase(dataBaseName);
                collectionList = db.listCollectionNames().into(new ArrayList<String>());
        } catch (Exception e) {
                LOGGER.error("MongoCommandException, Something went wrong with MongoDb connection. Error: " + e);
                return false;
        }
        if (collectionList == null || collectionList.isEmpty()) {
                return false;
        }
        return true;
}


}
