package com.ericsson.ei.handlers;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

    public boolean insertObject(String aggregatedObject, RulesObject rulesObject, String event, String id) {
        if (id == null) {
            String idRules = rulesObject.getIdRule();
            JsonNode idNode = jmespathInterface.runRuleOnEvent(idRules, event);
            id = idNode.textValue();
        }
        JsonNode document = prepareDocumentForInsertion(id, aggregatedObject);
        String documentStr = document.toString();
        return mongoDbHandler.insertDocument(databaseName, collectionName, documentStr);
    }

    public boolean insertObject(JsonNode aggregatedObject, RulesObject rulesObject, String event, String id) {
        return insertObject(aggregatedObject.toString(), rulesObject, event, id);
    }

    public boolean updateObject(String aggregatedObject, RulesObject rulesObject, String event, String id) {
        if (id == null) {
            String idRules = rulesObject.getIdRule();
            JsonNode idNode = jmespathInterface.runRuleOnEvent(idRules, event);
            id = idNode.textValue();
        }
        JsonNode document = prepareDocumentForInsertion(id, aggregatedObject);
        String documentStr = document.toString();
        return mongoDbHandler.updateDocument(databaseName, collectionName, documentStr, documentStr);
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
        JsonNode result = getAggregatedObject(document);
        if (result != null)
            return result.asText();
        return document;
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
}
