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

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.subscription.SubscriptionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class ObjectHandler {


    static Logger log = LoggerFactory.getLogger(ObjectHandler.class);

    @Getter
    @Setter
    @Value("${aggregated.collection.name}")
    private String collectionName;

    @Getter
    @Setter
    @Value("${spring.data.mongodb.database}")
    private String databaseName;

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

    @Getter
    @Value("${aggregated.collection.ttlValue}")
    private String ttlValue;

    public void insertObject(String aggregatedObject, RulesObject rulesObject, String event, String id) {
        if (id == null) {
            String idRules = rulesObject.getIdRule();
            JsonNode idNode = jmespathInterface.runRuleOnEvent(idRules, event);
            id = idNode.textValue();
        }
        BasicDBObject document = prepareDocumentForInsertion(id, aggregatedObject);
        log.debug("ObjectHandler: Aggregated Object document to be inserted: " + document.toString());

        if (getTtl() > 0) {
            mongoDbHandler.createTTLIndex(databaseName, collectionName, "Time", getTtl());
        }

        mongoDbHandler.insertDocument(databaseName, collectionName, document.toString());
        postInsertActions(aggregatedObject, rulesObject, event, id);
    }

    public void insertObject(JsonNode aggregatedObject, RulesObject rulesObject, String event, String id) {
        insertObject(aggregatedObject.toString(), rulesObject, event, id);
    }

    /**
     * This method uses previously locked in database aggregatedObject (lock was set
     * in lockDocument method) and modifies this document with the new values and
     * removes the lock in one query
     *
     * @param aggregatedObject
     *            String to insert in database
     * @param rulesObject
     *            used for fetching id
     * @param event
     *            String to fetch id if it was not specified
     * @param id
     *            String
     * @return true if operation succeed
     */
    public void updateObject(String aggregatedObject, RulesObject rulesObject, String event, String id) {
        if (id == null) {
            String idRules = rulesObject.getIdRule();
            JsonNode idNode = jmespathInterface.runRuleOnEvent(idRules, event);
            id = idNode.textValue();
        }
        log.debug("ObjectHandler: Updating Aggregated Object:\n" + aggregatedObject + "\nEvent:\n" + event);
        BasicDBObject document = prepareDocumentForInsertion(id, aggregatedObject);
        String condition = "{\"_id\" : \"" + id + "\"}";
        String documentStr = document.toString();
        mongoDbHandler.updateDocument(databaseName, collectionName, condition, documentStr);
        postInsertActions(aggregatedObject, rulesObject, event, id);
    }

    private void postInsertActions(String aggregatedObject, RulesObject rulesObject, String event, String id) {
        eventToObjectMap.updateEventToObjectMapInMemoryDB(rulesObject, event, id);
        subscriptionHandler.checkSubscriptionForObject(aggregatedObject, id);
    }

    public void updateObject(JsonNode aggregatedObject, RulesObject rulesObject, String event, String id) {
        updateObject(aggregatedObject.toString(), rulesObject, event, id);
    }

    public List<String> findObjectsByCondition(String condition) {
        return mongoDbHandler.find(databaseName, collectionName, condition);
    }

    public String findObjectById(String id) {
        String condition = "{\"_id\" : \"" + id + "\"}";
        String document = "";
        List<String> documents = findObjectsByCondition(condition);
        if (!documents.isEmpty())
            document = documents.get(0);
        return document;
    }

    public List<String> findObjectsByIds(List<String> ids) {
        List<String> objects = new ArrayList<>();
        for (String id : ids) {
            String object = findObjectById(id);
            if (object != null && !object.isEmpty())
                objects.add(object);
        }
        return objects;
    }

    public BasicDBObject prepareDocumentForInsertion(String id, String object) {
        BasicDBObject document = new BasicDBObject();
        document.put("_id", id);
        document.put("aggregatedObject", BasicDBObject.parse(object));

        if (getTtl() > 0) {
            try {
                document.put("Time", DateUtils.getDate());
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }
        }
        return document;
    }

    public JsonNode getAggregatedObject(String dbDocument) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode documentJson = mapper.readValue(dbDocument, JsonNode.class);
            JsonNode objectDoc = documentJson.get("aggregatedObject");
            return objectDoc;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public String extractObjectId(JsonNode aggregatedDbObject) {
        return aggregatedDbObject.get("_id").textValue();
    }

    /**
     * Locks the document in database to achieve pessimistic locking. Method
     * findAndModify is used to optimize the quantity of requests towards database.
     *
     * @param id
     *            String to search
     * @return String aggregated document
     */
    public String lockDocument(String id) {
        boolean documentLocked = true;
        String conditionId = "{\"_id\" : \"" + id + "\"}";
        String conditionLock = "[ { \"lock\" :  null } , { \"lock\" : \"0\"}]";
        String setLock = "{ \"$set\" : { \"lock\" : \"1\"}}";
        ObjectMapper mapper = new ObjectMapper();
        while (documentLocked == true) {
            try {
                JsonNode documentJson = mapper.readValue(setLock, JsonNode.class);
                JsonNode queryCondition = mapper.readValue(conditionId, JsonNode.class);
                ((ObjectNode) queryCondition).set("$or", mapper.readValue(conditionLock, JsonNode.class));
                Document result = mongoDbHandler.findAndModify(databaseName, collectionName, queryCondition.toString(),
                        documentJson.toString());
                if (result != null) {
                    log.debug("DB locked by " + Thread.currentThread().getId() + " thread");
                    documentLocked = false;
                    return JSON.serialize(result);
                }
                // To Remove
                log.debug("Waiting by " + Thread.currentThread().getId() + " thread");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public int getTtl() {
        int ttl = 0;
        if (ttlValue != null && !ttlValue.isEmpty()) {
            try {
                ttl = Integer.parseInt(ttlValue);
            } catch (NumberFormatException e) {
                log.error(e.getMessage(), e);
            }
        }
        return ttl;
    }
}
