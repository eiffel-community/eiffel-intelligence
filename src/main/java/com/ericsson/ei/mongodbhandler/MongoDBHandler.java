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
package com.ericsson.ei.mongodbhandler;

import java.util.ArrayList;
import java.util.Collections;
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoCredential;
import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
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
    static Logger log = LoggerFactory.getLogger(MongoDBHandler.class);

    @Autowired
    private MongoProperties mongoProperties;

    @Getter
    @Setter
    @JsonIgnore
    private MongoClient mongoClient;

    // TODO establish connection automatically when Spring instantiate this
    // based on connection data in properties file
    @PostConstruct
    public void init() {
        createConnection();
    }

    @PreDestroy
    public void close() {
        mongoClient.close();
    }

    // Establishing the connection to mongodb and creating a collection
    private void createConnection() {
        if (!StringUtils.isBlank(mongoProperties.getUsername())
                && !StringUtils.isBlank(new String(mongoProperties.getPassword()))) {
            ServerAddress address = new ServerAddress(mongoProperties.getHost(), mongoProperties.getPort());
            MongoCredential credential = MongoCredential.createCredential(mongoProperties.getUsername(),
                    mongoProperties.getDatabase(), mongoProperties.getPassword());
            mongoClient = new MongoClient(address, Collections.singletonList(credential));
        } else {
            mongoClient = new MongoClient(mongoProperties.getHost(), mongoProperties.getPort());
        }
    }

    /**
     * This method used for the insert the document into collection
     *
     * @param dataBaseName
     * @param collectionName
     * @param input          json String
     * @return
     */
    public void insertDocument(String dataBaseName, String collectionName, String input) throws MongoWriteException {
        MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
        if (collection != null) {
            final Document dbObjectInput = Document.parse(input);
            collection.insertOne(dbObjectInput);
            log.debug("Object : " + input);
            log.debug("inserted successfully in ");
            log.debug("collection : " + collectionName + "and db : " + dataBaseName);
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
                collection.find(new BasicDBObject()).forEach((Block<Document>) document -> {
                    result.add(JSON.serialize(document));
                });
                if (result.size() != 0) {
                    // This will pass about 10 times/second and most of the times DB will be empty,
                    // this is normal, no need to log
                    log.debug("getAllDocuments() :: database: " + dataBaseName + " and collection: " + collectionName
                            + " fetched No of :" + result.size());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * This method is used for the retrieve the documents based on the condition
     *
     * @param dataBaseName
     * @param collectionName
     * @param condition      string json
     * @return
     */
    public ArrayList<String> find(String dataBaseName, String collectionName, String condition) {
        ArrayList<String> result = new ArrayList<>();

        log.debug("Find and retrieve data from database." + "\nDatabase: " + dataBaseName + "\nCollection: "
                + collectionName + "\nCondition/Query: " + condition);

        try {
            MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
            if (collection != null) {
                collection.find(BasicDBObject.parse(condition)).forEach((Block<Document>) document -> {
                    result.add(JSON.serialize(document));
                });
                if (result.size() != 0) {
                    log.debug("find() :: database: " + dataBaseName + " and collection: " + collectionName
                            + " fetched No of :" + result.size());
                } else {
                    log.debug("find() :: database: " + dataBaseName + " and collection: " + collectionName
                            + " documents are not found");
                }
            } else {
                log.debug("Collection " + collectionName + " is empty in database " + dataBaseName);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return result;
    }

    /**
     * This method is used for update the document in collection and remove the lock in one query. Lock
     * is needed for multi process execution
     *
     * @param dataBaseName
     * @param collectionName
     * @param input          is a json string
     * @param updateInput    is updated document without lock
     * @return
     */
    public boolean updateDocument(String dataBaseName, String collectionName, String input, String updateInput) {
        try {
            MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
            if (collection != null) {
                final Document dbObjectInput = Document.parse(input);
                final Document dbObjectUpdateInput = Document.parse(updateInput);
                UpdateResult updateMany = collection.replaceOne(dbObjectInput, dbObjectUpdateInput);
                log.debug("updateDocument() :: database: " + dataBaseName + " and collection: " + collectionName
                        + " is document Updated :" + updateMany.wasAcknowledged());
                return updateMany.wasAcknowledged();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    /**
     * This method is used for lock and return the document that matches the input condition in one
     * query. Lock is needed for multi process execution. This method is executed in a loop.
     *
     * @param dataBaseName
     * @param collectionName
     * @param input          is a condition for update documents
     * @param updateInput    is updated document without lock
     * @return
     */
    public Document findAndModify(String dataBaseName, String collectionName, String input, String updateInput) {
        try {
            MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
            if (collection != null) {
                final Document dbObjectInput = Document.parse(input);
                final Document dbObjectUpdateInput = Document.parse(updateInput);
                Document result = collection.findOneAndUpdate(dbObjectInput, dbObjectUpdateInput);
                if (result != null) {
                    log.debug("updateDocument() :: database: " + dataBaseName + " and collection: " + collectionName
                            + " updated successfully");
                    return result;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * This method is used for the delete documents from collection using a condition
     *
     * @param dataBaseName
     * @param collectionName
     * @param condition      string json
     * @return
     */
    public boolean dropDocument(String dataBaseName, String collectionName, String condition) {
        try {
            MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
            if (collection != null) {
                final Document dbObjectCondition = Document.parse(condition);
                DeleteResult deleteMany = collection.deleteMany(dbObjectCondition);
                if (deleteMany.getDeletedCount() > 0) {
                    log.debug("database" + dataBaseName + " and collection: " + collectionName
                            + " deleted No.of records " + deleteMany.getDeletedCount());
                    return true;
                } else {
                    log.debug("database " + dataBaseName + " and collection: " + collectionName
                            + " No documents found to delete");
                    return false;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
    public void createTTLIndex(String dataBaseName, String collectionName, String fieldName, int ttlValue) {
        MongoCollection<Document> collection = getMongoCollection(dataBaseName, collectionName);
        IndexOptions indexOptions = new IndexOptions().expireAfter((long) ttlValue, TimeUnit.SECONDS);
        collection.createIndex(Indexes.ascending(fieldName), indexOptions);
    }

    private MongoCollection<Document> getMongoCollection(String dataBaseName, String collectionName) {
        if (mongoClient == null)
            return null;
        MongoDatabase db = mongoClient.getDatabase(dataBaseName);
        List<String> collectionList = db.listCollectionNames().into(new ArrayList<String>());
        if (!collectionList.contains(collectionName)) {
            log.debug("The requested database(" + dataBaseName + ") / collection(" + collectionName
                    + ") not available in mongodb, Creating ........");
            try {
                db.createCollection(collectionName);
            } catch (MongoCommandException e) {
                String message = "collection '" + dataBaseName + "." + collectionName + "' already exists";
                if (e.getMessage().contains(message)) {
                    log.warn("A " + message + ".");
                } else {
                    throw e;
                }
            }
            log.debug("done....");
        }
        MongoCollection<Document> collection = db.getCollection(collectionName);
        return collection;
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
}
