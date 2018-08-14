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
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents the mechanism to extract the aggregated data on the
 * basis of the ID from the aggregatedObject.
 */
@Component
public class ProcessAggregatedObject {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ProcessAggregatedObject.class);

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Value("${spring.data.mongodb.database}")
    private String aggregationDataBaseName;

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
        String query = "{\"aggregatedObject.id\": \"" + id + "\"}";
        
        LOGGER.debug("The condition is : " + query);
        JsonNode jsonCondition = null;
        try {
            jsonCondition = mapper.readTree(query);
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
     * @param templateName
     * @return ArrayList
     */
    public ArrayList<String> getAggregatedObjectByTemplateName(String templateName) {
        String condition = "{\"aggregatedObject.id\": /.*" + templateName + "/}";
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
        String condition = "{\"aggregatedObject.id\": /.*" + templateName + "/}";
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
    public JSONArray processQueryAggregatedObject(String request, String AggregationDataBaseName, String AggregationCollectionName) {
        List<String> allDocuments = handler.find(AggregationDataBaseName, AggregationCollectionName, request);
        LOGGER.debug("Number of document returned from AggregatedObject collection is : " + allDocuments.size());
        Iterator<String> allDocumentsItr = allDocuments.iterator();
        JSONArray jsonArray = new JSONArray();
        JSONObject doc = null;
        while (allDocumentsItr.hasNext()) {
            String temp = allDocumentsItr.next();
            try {
                doc = new JSONObject(temp);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            jsonArray.put(doc);
        }
        return jsonArray;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("The Aggregated Database is : " + aggregationDataBaseName
            + "\nThe Aggregated Collection is : " + aggregationCollectionName);
    }
}
