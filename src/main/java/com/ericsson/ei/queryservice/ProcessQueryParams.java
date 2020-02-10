/*
    Copyright 2018 Ericsson AB.
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

import com.ericsson.ei.mongo.MongoConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongo.MongoQuery;
import com.ericsson.ei.mongo.MongoQueryBuilder;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This class is responsible to search for an aggregatedObject in the database,
 * which matches the criteria sent in. It filters the result if
 * any filter was given, and returns the result from the search.
 */
@Component
public class ProcessQueryParams {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessQueryParams.class);

    @Value("${aggregations.collection.name}")
    private String aggregationCollectionName;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;


    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    /**
     * This method takes the parameters from the REST POST request body. If the
     * Aggregated Object matches the condition, then it is returned.
     *
     * @param criteriaObj
     * @param optionsObj
     * @param filter
     * @return JSONArray
     */
    public JSONArray runQuery(JSONObject criteriaObj, JSONObject optionsObj, String filter) {
        JSONArray resultAggregatedObject;
        MongoQuery query = MongoQueryBuilder.buildAnd(criteriaObj, optionsObj);
        resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject(query,
                databaseName, aggregationCollectionName);

        if(hasFilterCondition(filter)) {
            JSONArray filteredResults = filterResult(filter, resultAggregatedObject);
            LOGGER.debug("Filtered values from resultAggregatedObject: {}", filteredResults.toString());
            return filteredResults;
        }

        return resultAggregatedObject;
    }

    /**
     * This method checks if filter condition exists.
     *
     * @param filter
     *     An array of aggregated objects
     * @return JSONArray
     */
    private boolean hasFilterCondition(String filter) {
        if (filter != null && !filter.equals("")) {
            return true;
        }
        LOGGER.debug("No filter conditions were provided.");
        return false;
    }

    /**
     * This method takes an array of aggregated objects and a filter. It
     * returns a JSONArray where each element has a key (object Id)
     * and a list of filtered values.
     *
     * @param filter
     *     The filter to apply to an array of aggregated objects
     * @param resultAggregatedObjectArray
     *     An array of aggregated objects
     * @return JSONArray
     */
    private JSONArray filterResult(String filter, JSONArray resultAggregatedObjectArray) {
        JSONArray resultArray = new JSONArray();
        JmesPathInterface jmesPathInterface = new JmesPathInterface();
        try {
            for (int i = 0; i < resultAggregatedObjectArray.length(); i++) {
                String objectId =
                        ((JSONObject) resultAggregatedObjectArray.get(i)).get(MongoConstants.ID).toString();
                JsonNode filteredData = jmesPathInterface.runRuleOnEvent(filter, resultAggregatedObjectArray.get(i).toString());
                JSONObject tempJson = new JSONObject();
                tempJson.put(objectId, filteredData);
                resultArray.put(tempJson);
            }
        } catch (JSONException e) {
            LOGGER.error("Failed to filter an object.", e);
        }
        return resultArray;
    }

}
