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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    @Value("${aggregated.object.name}")
    private String objectName;
    
    @Value("${search.query.prefix}")
    private String searchQueryPrefix;


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
    public JSONArray filterFormParam(JSONObject criteriaObj, JSONObject optionsObj, String filter) {
        JSONArray resultAggregatedObject;
        String criteria = editObjectNameInQueryParam(criteriaObj);
        
        
        if (optionsObj == null || optionsObj.toString().equals("{}")) {
            resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject(criteria, databaseName, aggregationCollectionName);
        } else {
            String options = editObjectNameInQueryParam(optionsObj);
            LOGGER.debug("The options is : " + options);
            String request = "{ \"$and\" : [ " + criteria + "," + options + " ] }";
            resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject(request, databaseName, aggregationCollectionName);
        }
        resultAggregatedObject = checkFilterCondition(filter, resultAggregatedObject);

        return resultAggregatedObject;
    }

    /**
     * This method checks if filter condition exists.
     *
     * @param filterKey, resultAggregatedObjectArray
     * @return JSONArray
     * @throws IOException
     */
    private JSONArray checkFilterCondition(String filter, JSONArray resultAggregatedObject) {
        if (filter == null || filter.equals("")) {
            LOGGER.debug("No filter conditions provided. ResultAggregatedObject : " + resultAggregatedObject.toString());
        } else {
            JSONArray filteredResults = filterResult(filter, resultAggregatedObject);
            LOGGER.debug("Filtered values from resultAggregatedObject : " + filteredResults.toString());
            return filteredResults;
        }
        return resultAggregatedObject;
    }

    /**
     * This method takes array of aggregated objects and a filterKey. It returns a JSONArray where each element has a key (object Id)
     * and a list of filtered values.
     *
     * @param filterKey, resultAggregatedObjectArray
     * @return JSONArray
     * @throws IOException
     */
    private JSONArray filterResult(String filter, JSONArray resultAggregatedObjectArray) {
        JSONArray resultArray = new JSONArray();
        JmesPathInterface jmesPathInterface = new JmesPathInterface();
        try {
            for (int i = 0; i < resultAggregatedObjectArray.length(); i++) {
                String objectId = ((JSONObject) resultAggregatedObjectArray.get(i)).get("_id").toString();
                JsonNode filteredData = jmesPathInterface.runRuleOnEvent(filter, resultAggregatedObjectArray.get(i).toString());
                JSONObject tempJson = new JSONObject();
                tempJson.put(objectId, filteredData);
                resultArray.put(tempJson);
            }
        } catch (JSONException e) {
            LOGGER.error("Failed to filter an object\n: " + e.getMessage());
            e.printStackTrace();
        }
        return resultArray;
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
    
    /**
     * This method takes takes the tesxt as input and replaces all the instances of "object" with the object name in the properties file and return the edited text.
     * @param  txtObject JSONObject
     * @return String text after object name replaced with the name configured in the properties file
     */
    public String editObjectNameInQueryParam(JSONObject txtObject) {
        return Pattern.compile("("+ searchQueryPrefix + ".)").matcher(txtObject.toString()).replaceAll(objectName +".");    	
    }    
}
