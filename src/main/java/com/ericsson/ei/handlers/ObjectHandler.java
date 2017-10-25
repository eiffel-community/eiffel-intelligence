package com.ericsson.ei.handlers;

import java.util.ArrayList;

import com.ericsson.ei.subscriptionhandler.SubscriptionHandler;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class ObjectHandler {

       static Logger log = (Logger) LoggerFactory.getLogger(ObjectHandler.class);

    @Value("${aggregated.collection.name}") private String collectionName;
    @Value("${database.name}") private String databaseName;

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Autowired
    private MongoDBHandler mongoDbHandler;

    public void setMongoDbHandler(MongoDBHandler mongoDbHandler) {
        this.mongoDbHandler = mongoDbHandler;
    }

    @Autowired
    private JmesPathInterface jmespathInterface;

    public void setJmespathInterface(JmesPathInterface jmespathInterface) {
        this.jmespathInterface = jmespathInterface;
    }

    @Autowired
    private EventToObjectMapHandler eventToObjectMap;


    public void setEventToObjectMap(EventToObjectMapHandler eventToObjectMap) {
        this.eventToObjectMap = eventToObjectMap;
    }

    @Autowired
    private SubscriptionHandler subscriptionHandler;

    public void setSubscriptionHandler(SubscriptionHandler subscriptionHandler) {
        this.subscriptionHandler = subscriptionHandler;
    }


    public boolean insertObject(String aggregatedObject, RulesObject rulesObject, String event, String id) {
        if (id == null) {
            String idRules = rulesObject.getIdRule();
            JsonNode idNode = jmespathInterface.runRuleOnEvent(idRules, event);
            id = idNode.textValue();
        }
        JsonNode document = prepareDocumentForInsertion(id, aggregatedObject);
        String documentStr = document.toString();
        boolean result = mongoDbHandler.insertDocument(databaseName, collectionName, documentStr);
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

    public ArrayList<String> findObjectsByCondition(String condition) {
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

    public ArrayList<String> findObjectsByIds(ArrayList<String> ids) {
        ArrayList<String> objects = new ArrayList<String>();
        for (String id : ids) {
            objects.add(findObjectById(id));
        }
        return objects;
    }

    private JsonNode prepareDocumentForInsertion(String id, String object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String docStr = "{\"_id\":\"" + id +"\"}";
            JsonNode document = mapper.readValue(docStr, JsonNode.class);
            ((ObjectNode) document).put("aggregatedObject", object);
            return document;
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
        return aggregatedDbObject.get("_id").asText();
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
                DBObject result = mongoDbHandler.findAndModify(databaseName, collectionName, queryCondition.toString(), documentJson.toString());
                if(result != null){
                    log.info("DB locked by " + Thread.currentThread().getId() + " thread");
                    documentLocked = false;
                    return result.toString();}
//              To Remove
                log.info("Waiting by " + Thread.currentThread().getId() + " thread");
            } catch (Exception e) {
                log.info(e.getMessage(),e); }
        }
        return null;
    }
}
