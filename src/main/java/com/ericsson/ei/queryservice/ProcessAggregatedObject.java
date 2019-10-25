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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.MongoCondition;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.handlers.MongoQuery;
import com.ericsson.ei.handlers.MongoStringQuery;

/**
 * This class represents the mechanism to extract the aggregated object, which
 * matches the given search criteria, from the database.
 */
@Component
public class ProcessAggregatedObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessAggregatedObject.class);

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Value("${spring.data.mongodb.database}")
    private String aggregationDataBaseName;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    /**
     * The method is responsible to extract the aggregated data given the
     * ID from the aggregated object.
     *
     * @param id
     * @return ArrayList
     */
    public ArrayList<String> processQueryAggregatedObject(String id) {
        MongoCondition condition = MongoCondition.idCondition(id);
        LOGGER.debug("The JSON condition is: {}", condition);
        ArrayList<String> response = mongoDBHandler.find(aggregationDataBaseName,
                aggregationCollectionName,
                condition);
        return response;
    }

    /**
     * The method is responsible to extract the aggregated data given the
     * templateName from the aggregated object.
     *
     * @param templateName
     * @return ArrayList
     */
    public ArrayList<String> getAggregatedObjectByTemplateName(String templateName) {
        String queryString = "{\"id\": /.*" + templateName + "/}";
        MongoQuery query = new MongoStringQuery(queryString);
        LOGGER.debug("The JSON query is: {}", query);
        return mongoDBHandler.find(aggregationDataBaseName, aggregationCollectionName, query);
    }

    /**
     * This method is responsible for deleting the aggregated object with given
     * template name suffix
     *
     * @param templateName
     * @return boolean
     */
    public boolean deleteAggregatedObject(String templateName) {
        String queryString = "{\"id\": /.*" + templateName + "/}";
        MongoQuery query = new MongoStringQuery(queryString);
        LOGGER.debug("The JSON query for deleting aggregated object is: {}", query);
        return mongoDBHandler.dropDocument(aggregationDataBaseName, aggregationCollectionName, query);
    }

    /**
     * This method is responsible for fetching all the aggregated objects from the
     * aggregation database based on the given query and returns the result as a
     * JSONArray.
     *
     * @param query
     *     A String containing search criteria to use
     * @param AggregationDataBaseName
     * @param AggregationCollectionName
     * @return JSONArray
     */
    public JSONArray processQueryAggregatedObject(MongoQuery query, String AggregationDataBaseName,
            String AggregationCollectionName) {
        List<String> allDocuments = mongoDBHandler.find(AggregationDataBaseName, AggregationCollectionName, query);
        LOGGER.debug("Number of documents returned from {} collection is : {}", AggregationCollectionName, allDocuments.size());
        Iterator<String> allDocumentsItr = allDocuments.iterator();
        JSONArray jsonArray = new JSONArray();
        JSONObject doc = null;
        while (allDocumentsItr.hasNext()) {
            String temp = allDocumentsItr.next();
            try {
                doc = new JSONObject(temp);
            } catch (Exception e) {
                LOGGER.error("Failed to parse JSON.", e);
            }
            jsonArray.put(doc);
        }
        return jsonArray;
    }

}
