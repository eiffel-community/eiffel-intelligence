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
package com.ericsson.ei.queryservice;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

/**
 * This class represents the mechanism to extract the aggregated data on the
 * basis of the ID from the aggregatedObject.
 */
@Component
public class ProcessAggregatedObject {

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Value("${database.name}")
    private String aggregationDataBaseName;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ProcessAggregatedObject.class);

    @Autowired
    private MongoDBHandler handler;

    /**
     * The method is responsible to extract the aggregated data on the basis of
     * the ID from the aggregatedObject.
     *
     * @param id
     * @return ArrayList
     */
    public ArrayList<String> processQueryAggregatedObject(String id) {
        ObjectMapper mapper = new ObjectMapper();
        String condition = "{\"_id\" : \"" + id + "\"}";
        LOGGER.debug("The condition is : " + condition);
        JsonNode jsonCondition = null;
        try {
            jsonCondition = mapper.readTree(condition);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("The Json condition is : " + jsonCondition);
        ArrayList<String> response = handler.find(aggregationDataBaseName, aggregationCollectionName,
                jsonCondition.toString());
        return response;
    }
    
    /**
     * The method is responsible to extract the aggregated data on the basis of
     * the ID from the aggregatedObject.
     * 
     * @param id
     * @return ArrayList
     */
    public ArrayList<String> getAggregatedObjectByTemplateName(String templateName) {
        String condition = "{\"_id\": /.*" + templateName + "/}";
        LOGGER.debug("The Json condition is : " + condition);
        return handler.find(aggregationDataBaseName, aggregationCollectionName, condition);
    }
    
    /**
     * The method is responsible for the delete the aggregated object using template name suffix
     * 
     * @param templateName
     * @return boolean
     */
    public boolean deleteAggregatedObject(String templateName) {
        String condition = "{\"_id\": /.*" + templateName + "/}";
        LOGGER.debug("The Json condition for delete aggregated object is : " + condition);
        return handler.dropDocument(aggregationDataBaseName, aggregationCollectionName, condition);
    }

    /**
     * This method is responsible for fetching all the aggregatedObjects from
     * the Aggregation database and return it as JSONArray.
     *
     * @param request
     * @param AggregationDataBaseName
     * @param AggregationCollectionName
     * @return JSONArray
     */
    public JSONArray processQueryAggregatedObject(JsonNode request, String AggregationDataBaseName, String AggregationCollectionName) {
        DB db = new MongoClient().getDB(AggregationDataBaseName);
        Jongo jongo = new Jongo(db);
        MongoCollection aggObjects = jongo.getCollection(AggregationCollectionName);
        LOGGER.debug("Successfully connected to AggregatedObject database");
        MongoCursor<Document> allDocuments = aggObjects.find(request.toString()).as(Document.class);
        LOGGER.debug("Number of document returned from AggregatedObject collection is : " + allDocuments.count());
        JSONArray jsonArray = new JSONArray();
        JSONObject doc = null;
        while (allDocuments.hasNext()) {
            Document temp = allDocuments.next();
            try {
                doc = new JSONObject(temp.toJson());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            jsonArray.put(doc);
        }
        return jsonArray;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("The Aggregated Database is : " + aggregationDataBaseName);
        LOGGER.debug("The Aggregated Collection is : " + aggregationCollectionName);
    }
}
