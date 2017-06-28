package com.ericsson.ei.handlers;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ObjectHandler {
    @Value("${aggregated.collection.name}") private String collectionName;
    @Value("${database.name}") private String databaseName;

    @Autowired
    private MongoDBHandler mongoDbHandler;

    public boolean insertObject(String aggregatedObject) {
        return mongoDbHandler.insertDocument(databaseName, collectionName, aggregatedObject);
    }

    public void insertObject(JsonNode aggregatedObject) {
        insertObject(aggregatedObject.asText());
    }

    public ArrayList<String> findObjectsById(String condition) {
        return mongoDbHandler.getDocumentsOnCondition(databaseName, collectionName, condition);
    }

    public String findObjectById(String condition) {
        return mongoDbHandler.getDocumentsOnCondition(databaseName, collectionName, condition).get(0);
    }
}
