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
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Iterator;

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
    public JSONArray filterFormParam(JsonNode request) throws IOException {
        JsonNode criteria = request.get("criteria");
        JsonNode options = request.get("options");
        LOGGER.debug("The criteria is : " + criteria.toString());
        LOGGER.debug("The options is : " + options.toString());
        JSONArray resultAggregatedObject;
        if (options.toString().equals("{}") || options.isNull()) {
            resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject(criteria.toString(), databaseName, aggregationCollectionName);
        } else {
            String result = "{ \"$and\" : [ " + criteria.toString() + "," + options.toString() + " ] }";
            resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject(result, databaseName, aggregationCollectionName);
        }
        LOGGER.debug("resultAggregatedObject : " + resultAggregatedObject.toString());
        return resultAggregatedObject;
    }

    /**
     * This method takes the parameters from the REST GET request query. If the
     * Aggregated Object matches the condition, then it is returned.
     *
     * @param request
     * @return JSONArray
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    public JSONArray filterQueryParam(String request) throws JsonParseException, JsonMappingException, IOException {
        LOGGER.debug("The query string is : " + request);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode criteriasJsonNode = mapper.readValue(request, JsonNode.class).get("criteria");

        LOGGER.debug("Freestyle criteria query:" + criteriasJsonNode.toString());
        return processAggregatedObject.processQueryAggregatedObject(criteriasJsonNode.toString(), databaseName, aggregationCollectionName);
    }

    @PostConstruct
    public void print() {
        LOGGER.debug("Aggregation Database : " + databaseName
            + "\nAggregation Collection is : " + aggregationCollectionName);
    }
}