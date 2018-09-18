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

import com.ericsson.ei.controller.QueryControllerImpl;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * This class is responsible to fetch the criterias from both the query
 * parameters or the form parameters. Then find the aggregatedObject from the
 * database and concatenate the result.
 */
@Component
public class ProcessQueryParams {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryControllerImpl.class);

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    /**
     * This method takes the parameters from the REST POST request body. If the
     * Aggregated Object matches the condition, then it is returned.
     *
     * @param request
     * @return JSONArray
     * @throws IOException
     */
    public JSONArray filterFormParam(JsonNode request) {
        JsonNode criteria = request.get("criteria");
        JsonNode options = request.get("options");
        JsonNode filterKey = request.get("filterKey");
        JSONArray resultAggregatedObject;
        if (options == null || options.toString().equals("{}")) {
            resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject(criteria.toString(), databaseName, aggregationCollectionName);
        } else {
            LOGGER.debug("The options is : " + options.toString());
            String result = "{ \"$and\" : [ " + criteria.toString() + "," + options.toString() + " ] }";
            resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject(result, databaseName, aggregationCollectionName);
        }
        if (filterKey == null || filterKey.toString().equals("{}")) {
            LOGGER.debug("resultAggregatedObject : " + resultAggregatedObject.toString());
        } else {
            resultAggregatedObject = filterResult(resultAggregatedObject, filterKey);
            LOGGER.debug("Filtered values from resultAggregatedObject : " + resultAggregatedObject.toString());
        }
        return resultAggregatedObject;
    }

    /**
     * This method takes array of aggregated objects and a filterKey. It returns a JSONArray where each element has a key (object Id)
     * and a list of filtered values.
     *
     * @param request
     * @return JSONArray
     * @throws IOException
     */
    private JSONArray filterResult(JSONArray resultAggregatedObjectArray, JsonNode filterKey) {
        JSONArray tempArray = new JSONArray();
        JmesPathInterface unitUnderTest = new JmesPathInterface();
        String searchPath = filterKey.get("key").textValue();
        String processRule = "{values:" + searchPath + "}";
        for (int i = 0; i < resultAggregatedObjectArray.length(); i++) {
            try {
                String objectId = ((JSONObject) resultAggregatedObjectArray.get(i)).get("_id").toString();
                String str = resultAggregatedObjectArray.get(i).toString();
                JsonNode node = unitUnderTest.runRuleOnEvent(processRule, str);
                JSONObject json = new JSONObject();
                json.put(objectId, node.get("values").textValue());
                tempArray.put(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tempArray;
    }

    /**
     * This method takes the parameters from the REST GET request query. If the Aggregated Object matches the condition, then it is returned.
     *
     * @param request
     * @return JSONArray
     */
    public JSONArray filterQueryParam(String request) {
        LOGGER.debug("The query string is : " + request);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode criteriasJsonNode;
        try {
            criteriasJsonNode = mapper.readValue(request, JsonNode.class).get("criteria");
        } catch (IOException e) {
            LOGGER.error("Failed to parse FreeStyle query critera field from request:\n" + request);
            return new JSONArray();
        }
        LOGGER.debug("Freestyle criteria query:" + criteriasJsonNode.toString());
        return processAggregatedObject.processQueryAggregatedObject(criteriasJsonNode.toString(), databaseName, aggregationCollectionName);
    }

    @PostConstruct
    public void print() {
        LOGGER.debug("Aggregation Database : " + databaseName
                + "\nAggregation Collection is : " + aggregationCollectionName);
    }
}