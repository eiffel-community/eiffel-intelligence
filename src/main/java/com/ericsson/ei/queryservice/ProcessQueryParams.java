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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.stream.IntStream;

/**
 * This class is responsible to fetch the criterias from both the query
 * parameters or the form parameters. Then find the aggregatedObject from the
 * database and concatenate the result.
 */
@Component
public class ProcessQueryParams {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(QueryControllerImpl.class);

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Value("${database.name}")
    private String dataBaseName;

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDataBaseName;

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    @Autowired
    private ProcessMissedNotification processMissedNotification;

    /**
     * This method takes the parameters from the REST POST request body.
     * If the Aggregated Object matches the condition, then it is returned.
     *
     * @param request
     * @return JSONArray
     * @throws IOException
     */
    public JSONArray filterFormParam(JsonNode request) throws IOException {
        JsonNode criteria = request.get("criteria");
        JsonNode options = request.get("options");
        LOGGER.debug("The criteria is : " + criteria.toString());
        LOGGER.debug("The options is : " + options.toString());
        if (options.toString().equals("{}") || options.isNull()) {
            return getProcessQuery(criteria);
        } else {
            String result = "{ \"$and\" : [ " + criteria.toString() + "," + options.toString() + " ] }";
            return getProcessQuery(new ObjectMapper().readTree(result));
        }
    }

    /**
     * This method is responsible for concatenating two JSONArrays.
     *
     * @param firstArray
     * @param secondArray
     * @return JSONArray
     * @throws JSONException
     */
    private static JSONArray concatArray(JSONArray firstArray, JSONArray secondArray) throws JSONException {
        JSONArray result = new JSONArray();
        IntStream.range(0, firstArray.length()).mapToObj(firstArray::get).forEach(result::put);
        IntStream.range(0, secondArray.length()).mapToObj(secondArray::get).forEach(result::put);
        return result;
    }

    /**
     * This method takes the parameters from the REST GET request query.
     * If the Aggregated Object matches the condition, then
     * it is returned.
     *
     * @param request
     * @return JSONArray
     */
    public JSONArray filterQueryParam(String request) {
        LOGGER.debug("The query string is : " + request);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode criteria = mapper.createObjectNode();
        String[] criterias = request.split(",");
        LOGGER.debug("The query parameters are :");
        for (String s : criterias) {
            String[] node = s.split(":");
            String key = node[0];
            String value = node[1];
            LOGGER.debug("The key is : " + key);
            LOGGER.debug("The value is : " + value);
            criteria.put(key, value);
        }
        LOGGER.debug(criteria.toString());
        return getProcessQuery(criteria);
    }

    /**
     * Process parameters to create a JsonNode request to query the
     * Aggregated Objects.
     *
     * @param criteria
     * @return
     */
    private JSONArray getProcessQuery(JsonNode criteria) {
        JSONArray resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject(criteria, dataBaseName, aggregationCollectionName);
        JSONArray resultMissedNotification = processMissedNotification.processQueryMissedNotification(criteria, missedNotificationDataBaseName, missedNotificationCollectionName);
        LOGGER.debug("resultAggregatedObject : " + resultAggregatedObject.toString());
        LOGGER.debug("resultMissedNotification : " + resultMissedNotification.toString());
        JSONArray result = null;
        try {
            result = ProcessQueryParams.concatArray(resultAggregatedObject, resultMissedNotification);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.debug("Final Result is : " + result.toString());
        return result;
    }

    @PostConstruct
    public void print() {
        LOGGER.debug("Values from application.properties file");
        LOGGER.debug("AggregationCollectionName : " + aggregationCollectionName);
        LOGGER.debug("AggregationDataBaseName : " + dataBaseName);
        LOGGER.debug("MissedNotificationCollectionName : " + missedNotificationCollectionName);
        LOGGER.debug("MissedNotificationDataBaseName : " + missedNotificationDataBaseName);
    }
}