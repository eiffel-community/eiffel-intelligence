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

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.subscriptionhandler.SubscriptionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.util.JSON;

import lombok.Getter;
import lombok.Setter;

@Component
public class ObjectHandler {

    static Logger log = (Logger) LoggerFactory.getLogger(ObjectHandler.class);

    @Getter @Setter
    @Value("${aggregated.collection.name}")
    private String collectionName;

    @Getter @Setter
    @Value("${database.name}")
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
    private int ttlValue;

    public boolean insertObject(String aggregatedObject, RulesObject rulesObject, String event, String id) {
        if (id == null) {
            String idRules = rulesObject.getIdRule();
            JsonNode idNode = jmespathInterface.runRuleOnEvent(idRules, event);
            id = idNode.textValue();
        }
        JsonNode document = prepareDocumentForInsertion(id, aggregatedObject);
        log.debug("ObjectHandler: Aggregated Object document to be inserted: " + document.toString());
        mongoDbHandler.createTTLIndex(databaseName, collectionName, "Time", ttlValue);
 
        boolean result = mongoDbHandler.insertDocument(databaseName, collectionName, document.toString());
        if (result)
            eventToObjectMap.updateEventToObjectMapInMemoryDB(rulesObject, event, id);
            subscriptionHandler.checkSubscriptionForObject(aggregatedObject);
        return result;
    }

    public boolean insertObject(JsonNode aggregatedObject, RulesObject rulesObject, String event, String id) {
        return insertObject(aggregatedObject.toString(), rulesObject, event, id);
    }

    /**
     * This method uses previously locked in database aggregatedObject (lock was set in lockDocument method)
     * and modifies this document with the new values and removes the lock in one query
     * @param aggregatedObject String to insert in database
     * @param rulesObject used for fetching id
     * @param event String to fetch id if it was not specified
     * @param id String
     * @return true if operation succeed
     */
    public boolean updateObject(String aggregatedObject, RulesObject rulesObject, String event, String id) {
        if (id == null) {
            String idRules = rulesObject.getIdRule();
            JsonNode idNode = jmespathInterface.runRuleOnEvent(idRules, event);
            id = idNode.textValue();
        }
        log.debug("ObjectHandler: Updating Aggregated Object:\n" + aggregatedObject +
        		"\nEvent:\n" + event);
        JsonNode document = prepareDocumentForInsertion(id, aggregatedObject);
        String condition = "{\"_id\" : \"" + id + "\"}";
        String documentStr = document.toString();
        boolean result = mongoDbHandler.updateDocument(databaseName, collectionName, condition, documentStr);
        if (result)
            eventToObjectMap.updateEventToObjectMapInMemoryDB(rulesObject, event, id);
            subscriptionHandler.checkSubscriptionForObject(aggregatedObject);
        return result;
    }

    public boolean updateObject(JsonNode aggregatedObject, RulesObject rulesObject, String event, String id) {
        return updateObject(aggregatedObject.toString(), rulesObject, event, id);
    }

    public List<String> findObjectsByCondition(String condition) {
        return mongoDbHandler.find(databaseName, collectionName, condition);
    }

    public String findObjectById(String id) {
        String condition = "{\"_id\" : \"" + id + "\"}";
        String document = findObjectsByCondition(condition).get(0);
//        JsonNode result = getAggregatedObject(document);
//        if (result != null)
//            return result.asText();
        return document;
    }

    public List<String> findObjectsByIds(List<String> ids) {
        List<String> objects = new ArrayList<>();
        for (String id : ids) {
            objects.add(findObjectById(id));
        }
        return objects;
    }

    public JsonNode prepareDocumentForInsertion(String id, String object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String docStr = "{\"_id\": \"" + id + "\"}";
            JsonNode jsonNodeNew = mapper.readValue(docStr, JsonNode.class);

            JsonNode jsonNode = mapper.readValue(jsonNodeNew.toString(), JsonNode.class);
            ObjectNode objNode = (ObjectNode) jsonNode;
            objNode.set("aggregatedObject", mapper.readTree(object));

            return jsonNode;
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
        return null;
    }

    public JsonNode getAggregatedObject(String dbDocument) {
         ObjectMapper mapper = new ObjectMapper();
         try {
             JsonNode documentJson = mapper.readValue(dbDocument, JsonNode.class);
             JsonNode objectDoc = documentJson.get("aggregatedObject");
             return objectDoc;
         } catch (Exception e) {
             log.info(e.getMessage(),e);
         }
         return null;
    }

    public String extractObjectId(JsonNode aggregatedDbObject) {
        return aggregatedDbObject.get("_id").textValue();
    }

    /**
     * Locks the document in database to achieve pessimistic locking. Method findAndModify is used to optimize
     * the quantity of requests towards database.
     * @param id String to search
     * @return String aggregated document
     */
    public String lockDocument(String id){
        boolean documentLocked = true;
        String conditionId = "{\"_id\" : \"" + id + "\"}";
        String conditionLock = "[ { \"lock\" :  null } , { \"lock\" : \"0\"}]";
        String setLock = "{ \"$set\" : { \"lock\" : \"1\"}}";
        ObjectMapper mapper = new ObjectMapper();
        while (documentLocked==true){
            try {
                JsonNode documentJson = mapper.readValue(setLock, JsonNode.class);
                JsonNode queryCondition = mapper.readValue(conditionId, JsonNode.class);
                ((ObjectNode) queryCondition).set("$or", mapper.readValue(conditionLock, JsonNode.class));
                Document result = mongoDbHandler.findAndModify(databaseName, collectionName, queryCondition.toString(), documentJson.toString());
                if (result != null) {
                    log.info("DB locked by " + Thread.currentThread().getId() + " thread");
                    documentLocked = false;
                    return JSON.serialize(result);
                }
                // To Remove
                log.info("Waiting by " + Thread.currentThread().getId() + " thread");
            } catch (Exception e) {
                log.info(e.getMessage(),e); }
        }
        return null;
    }
}
