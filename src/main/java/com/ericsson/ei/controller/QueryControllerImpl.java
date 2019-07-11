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
package com.ericsson.ei.controller;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.ericsson.ei.controller.model.QueryBody;
import com.ericsson.ei.controller.model.QueryResponse;
import com.ericsson.ei.controller.model.QueryResponseEntity;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessMissedNotification;
import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.ericsson.ei.utils.ResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * This class represents the REST end-points for the query services.
 */
@Component
@CrossOrigin
@Api(value = "query", tags = {"Query"})
public class QueryControllerImpl implements QueryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryControllerImpl.class);

    @Autowired
    private ProcessQueryParams processQueryParams;

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    @Autowired
    private ProcessMissedNotification processMissedNotification;

    /**
     * This method is used to query aggregated objects with the requested criteria, which must be present in the request body. Options and filter are optional.
     * 
     * @param body
     * @return ResponseEntity
     */
    @Override
    @CrossOrigin
    @ApiOperation(value = "Perform a freestyle query to retrieve aggregated objects", response = String.class)
    public ResponseEntity<?> createQuery(@RequestBody final QueryBody body) {
        String emptyResponseContent = "[]";
        HttpStatus httpStatus;
        try {
            JSONObject criteria = new JSONObject(body.getCriteria().getAdditionalProperties());
            JSONObject options = null;
            String filter = "";
            if (body.getOptions() != null) {
                options = new JSONObject(body.getOptions().getAdditionalProperties());
            }
            if (body.getFilter() != null) {
                filter = body.getFilter();
            }

            JSONArray result = processQueryParams.runQuery(criteria, options, filter);
            if(!result.toString().equalsIgnoreCase(emptyResponseContent)) {
                httpStatus = HttpStatus.OK;
            } else {
                httpStatus = HttpStatus.NO_CONTENT;
            }
            return new ResponseEntity<String>(result.toString(), httpStatus);
        } catch (Exception e) {
            String errorMessage = "Internal Server Error: Failed to extract data from the Aggregated Object using freestyle query.";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method retrieves aggregated data on a specific aggregated object, given an ID.
     *
     * @param id
     * @return ResponseEntity
     */
    @Override
    @ApiOperation(value = "Retrieve an aggregated object by id", response = QueryResponse.class)
    public ResponseEntity<?> getQueryAggregatedObject(@RequestParam("ID") final String id) {
        ObjectMapper mapper = new ObjectMapper();
        QueryResponseEntity queryResponseEntity = new QueryResponseEntity();
        QueryResponse queryResponse = new QueryResponse();
        HttpStatus httpStatus = HttpStatus.NO_CONTENT;
        try {
            List<String> response = processAggregatedObject.processQueryAggregatedObject(id);
            if (!response.isEmpty()) {
                queryResponseEntity = mapper.readValue(response.get(0), QueryResponseEntity.class);
                httpStatus = HttpStatus.OK;
            }

            queryResponse.setQueryResponseEntity(queryResponseEntity);
            LOGGER.debug("The response is: {}", response.toString());
            return new ResponseEntity<QueryResponse>(queryResponse, httpStatus);
        } catch (Exception e) {
            String errorMessage = "Internal Server Error: Failed to extract the aggregated data from the Aggregated Object based on ID " + id + ".";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method returns missed notification(s) given a subscription name.
     *
     * @param subscriptionName
     */
    @Override
    @ApiOperation(value = "Retrieve missed notifications", response = QueryResponse.class)
    public ResponseEntity<?> getQueryMissedNotifications(@RequestParam("SubscriptionName") final String subscriptionName) {
        ObjectMapper mapper = new ObjectMapper();
        QueryResponse queryResponse = new QueryResponse();
        QueryResponseEntity queryResponseEntity = new QueryResponseEntity();
        try {
            List<String> response = processMissedNotification.processQueryMissedNotification(subscriptionName);
            if (!response.isEmpty()) {
                queryResponseEntity = mapper.readValue(response.get(0), QueryResponseEntity.class);
            }
            queryResponse.setQueryResponseEntity(queryResponseEntity);
            LOGGER.debug("The response is : {}", response.toString());
            if (processMissedNotification.deleteMissedNotification(subscriptionName)) {
                LOGGER.debug("Missed notification with subscription name {} was successfully removed from database", subscriptionName);
            }
            return new ResponseEntity<QueryResponse>(queryResponse, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Internal Server Error: Failed to extract the data from the Missed Notification Object based on subscription name "
                    + subscriptionName + ".";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
