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
package com.ericsson.ei.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.ericsson.ei.controller.model.QueryBody;
import com.ericsson.ei.controller.model.QueryResponse;
import com.ericsson.ei.controller.model.QueryResponseEntity;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.ericsson.ei.utils.ResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;

/**
 * This class represents the REST GET mechanism to extract the aggregated data on the basis of the
 * Id from the aggregatedObject.
 */
@Component
@CrossOrigin
@Api(tags = {"Aggregated objects"}, description = "Fetch aggregated data based on Id")
public class AggregatedObjectControllerImpl implements AggregatedObjectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatedObjectControllerImpl.class);

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    @Autowired
    private ProcessQueryParams processQueryParams;

    /**
     * This method is responsible for the REST Get mechanism to extract the aggregated data on the
     * basis of the Id from the aggregatedObject.
     *
     * @param id
     * @return ResponseEntity
     */
    @Override
    @ApiOperation(value = "Get a specific aggregated object", tags = { "Aggregated objects" })
    public ResponseEntity<?> getAggregatedObjectById(@PathVariable final String id,
            final HttpServletRequest httpRequest) {
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

    @Override
    @CrossOrigin
    @ApiOperation(value = "Perform a freestyle query to retrieve aggregated objects", tags = { "Aggregated objects" })
    public ResponseEntity<?> createAggregatedObjectsQuery(@RequestBody final QueryBody body,
            final HttpServletRequest httpRequest) {
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
            if (!result.toString().equalsIgnoreCase(emptyResponseContent)) {
                httpStatus = HttpStatus.OK;
            } else {
                httpStatus = HttpStatus.NO_CONTENT;
            }
            return new ResponseEntity<>(result.toString(), httpStatus);
        } catch (Exception e) {
            String errorMessage = "Internal Server Error: Failed to extract data from the Aggregated Object using freestyle query.";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}