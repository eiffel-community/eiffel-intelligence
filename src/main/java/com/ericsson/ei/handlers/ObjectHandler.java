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
package com.ericsson.ei.handlers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.ericsson.ei.mongo.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.subscription.SubscriptionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientException;
import com.mongodb.util.JSON;

import lombok.Getter;
import lombok.Setter;

@Component
public class ObjectHandler {

    private static final int MAX_RETRY_COUNT = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectHandler.class);

    @Getter
    @Setter
    @Value("${aggregations.collection.name}")
    private String aggregationsCollectionName;

    @Getter
    @Setter
    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Getter
    @Value("${aggregations.collection.ttl}")
    private String aggregationsTtl;

    @Setter
    @Autowired
    private MongoDBHandler mongoDbHandler;

    @Setter
    @Autowired
    private JmesPathInterface jmespathInterface;

    @Setter
    @Autowired
    private EventToObjectMapHandler eventToObjectMap;

    @Setter
    @Autowired
    private SubscriptionHandler subscriptionHandler;

    /**
     * This method is responsible for inserting an aggregated object in to the database.
     *
     * @param aggregatedObject String format of an aggregated object to be inserted
     * @param rulesObject      RulesObject
     * @param event            String representation of event, used to fetch id if not specified
     * @param givenId          String id is stored together with aggregated object in database
     * @throws                 MongoDBConnectionException
     */
    public void insertObject(String aggregatedObject, RulesObject rulesObject, String event,
            String givenId) throws MongoDBConnectionException {
        String id = givenId;
        if (id == null) {
            String idRules = rulesObject.getIdRule();
            JsonNode idNode = jmespathInterface.runRuleOnEvent(idRules, event);
            id = idNode.textValue();
        }
        BasicDBObject document = prepareDocumentForInsertion(id, aggregatedObject);
        LOGGER.debug("ObjectHandler: Aggregated Object document to be inserted: {}",
                document.toString());

        if (getTtl() > 0) {
            mongoDbHandler.createTTLIndex(databaseName, aggregationsCollectionName,
                    MongoConstants.TIME, getTtl());
        }

        mongoDbHandler.insertDocument(databaseName, aggregationsCollectionName, document.toString());
        postInsertActions(aggregatedObject, rulesObject, event, id);
    }

    public void insertObject(JsonNode aggregatedObject, RulesObject rulesObject, String event,
            String id) throws MongoDBConnectionException {
        insertObject(aggregatedObject.toString(), rulesObject, event, id);
    }

    /**
     * This method uses previously locked in database aggregatedObject (lock was set in lockDocument
     * method) and modifies this document with the new values and removes the lock in one query
     *
     * @param aggregatedObject String to insert in database
     * @param rulesObject      used for fetching id
     * @param event            String to fetch id if it was not specified
     * @param givenId          String
     * @return true if operation succeed
     */
    public void updateObject(String aggregatedObject, RulesObject rulesObject, String event,
            final String givenId) {
        String id = givenId;
        if (id == null) {
            String idRules = rulesObject.getIdRule();
            JsonNode idNode = jmespathInterface.runRuleOnEvent(idRules, event);
            id = idNode.textValue();
        }
        LOGGER.debug("ObjectHandler: Updating Aggregated Object:\n{} \nEvent:\n{}",
                aggregatedObject, event);
        BasicDBObject document = prepareDocumentForInsertion(id, aggregatedObject);
        final MongoCondition condition = MongoCondition.idCondition(id);
        String documentStr = document.toString();
        mongoDbHandler.updateDocument(databaseName, aggregationsCollectionName, condition, documentStr);
        postInsertActions(aggregatedObject, rulesObject, event, id);
    }

    public void updateObject(JsonNode aggregatedObject, RulesObject rulesObject, String event,
            String id) {
        updateObject(aggregatedObject.toString(), rulesObject, event, id);
    }

    /**
     * This methods searches the database for documents matching a given condition.
     *
     * @param query query to base search on
     * @return List of documents
     */
    public List<String> findObjectsByCondition(MongoQuery query) throws MongoClientException {
        return mongoDbHandler.find(databaseName, aggregationsCollectionName, query);
    }

    /**
     * This method searches the database for a document matching a specific id.
     *
     * @param id An id to search for in the database
     * @return document
     */
    public String findObjectById(String id) throws MongoClientException {
        final MongoCondition condition = MongoCondition.idCondition(id);
        String document = "";
        List<String> documents = findObjectsByCondition(condition);
        if (!documents.isEmpty())
            document = documents.get(0);
        return document;
    }

    /**
     * This method searches the database for documents matching a list of ids.
     *
     * @param ids List of string ids to search for
     * @return objects
     */
    public List<String> findObjectsByIds(List<String> ids) {
        List<String> objects = new ArrayList<>();
        for (String id : ids) {
            String object = findObjectById(id);
            if (object != null && !object.isEmpty())
                objects.add(object);
        }
        return objects;
    }

    /**
     * This method creates a new document containing id, aggregated object and sets the time to live
     * value.
     *
     * @param id
     * @param object
     * @return document
     */
    public BasicDBObject prepareDocumentForInsertion(String id, String object) {
        BasicDBObject document = BasicDBObject.parse(object);
        document.put(MongoConstants.ID, id);
        try {
            if (getTtl() > 0) {
                document.put(MongoConstants.TIME, DateUtils.getDate());
            }
        } catch (ParseException e) {
            LOGGER.error("Failed to attach date to document.", e);
        }
        return document;
    }

    /**
     * This method gets the id from an aggregated object.
     *
     * @param aggregatedDbObject The JsonNode to search
     * @return id
     */
    public String extractObjectId(JsonNode aggregatedDbObject) {
        return aggregatedDbObject.get(MongoConstants.ID).textValue();
    }

    /**
     * Locks the document in database to achieve pessimistic locking. Method findAndModify is used
     * to optimize the quantity of requests towards database.
     *
     * @param id String to search
     * @return String aggregated document
     */
    public String lockDocument(String id) {
        int retryCounter = 0;
        boolean documentLocked = true;

        String setLock = "{ \"$set\" : { \"lock\" : \"1\"}}";
        ObjectMapper mapper = new ObjectMapper();

        final MongoCondition lockNotSet = MongoCondition.lockCondition(MongoConstants.NOT_LOCKED);
        final MongoCondition noLock = MongoCondition.lockNullCondition();
        MongoQuery idAndNoLockCondition = MongoQueryBuilder.buildOr(lockNotSet, noLock)
                                                           .append(MongoCondition.idCondition(id));

        /*
         * As findAndModify below does not distinguish between a invalid id and locked document we
         * need to check that the document id can be found
         */
        if (isInvalidId(id)) {
            LOGGER.error("Could not find document with id: {}", id);
            return null;
        }

        // Checking a retryCounter to prevent a infinite loop while waiting for document to unlock
        while (documentLocked && retryCounter < MAX_RETRY_COUNT) {
            try {
                JsonNode documentJson = mapper.readValue(setLock, JsonNode.class);
                Document result = mongoDbHandler.findAndModify(databaseName,
                        aggregationsCollectionName,
                        idAndNoLockCondition,
                        documentJson.toString());
                if (result != null) {
                    LOGGER.debug("DB locked by {} thread", Thread.currentThread().getId());
                    documentLocked = false;
                    return JSON.serialize(result);
                }
                // To Remove
                LOGGER.debug("Waiting by {} thread", Thread.currentThread().getId());
            } catch (Exception e) {
                LOGGER.error("Could not lock document", e);
            } finally {
                retryCounter++;
            }
        }
        return null;
    }

    /**
     * This method gives the TTL (time to live) value for documents stored in the database. This
     * value is set in application.properties when starting Eiffel Intelligence.
     *
     * @return ttl Integer value representing time to live for documents
     */
    @JsonIgnore
    public int getTtl() {
        int ttl = 0;
        if (StringUtils.isNotEmpty(aggregationsTtl)) {
            try {
                ttl = Integer.parseInt(aggregationsTtl);
            } catch (NumberFormatException e) {
                LOGGER.error("Failed to parse TTL value.", e);
            }
        }
        return ttl;
    }

    private boolean isInvalidId(String id) {
        final MongoCondition idCondition = MongoCondition.idCondition(id);
        ArrayList<String> documentExistsCheck = mongoDbHandler.find(databaseName,
                aggregationsCollectionName,
                idCondition);

        return documentExistsCheck.isEmpty();
    }

    private void postInsertActions(String aggregatedObject, RulesObject rulesObject, String event,
            String id) {
        eventToObjectMap.updateEventToObjectMapInMemoryDB(rulesObject, event, id);
        subscriptionHandler.checkSubscriptionForObject(aggregatedObject, id);
    }
}
