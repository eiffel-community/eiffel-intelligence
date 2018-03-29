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

import com.ericsson.ei.controller.model.QueryResponse;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

/**
 * This class represents the REST GET mechanism to extract the aggregated data
 * on the basis of the ID from the aggregatedObject.
 */
@Component
@CrossOrigin
public class QueryAggregatedObjectControllerImpl implements QueryAggregatedObjectController {

    private static Logger LOGGER = (Logger) LoggerFactory.getLogger(QueryAggregatedObjectControllerImpl.class);

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    /**
     * This method is responsible for the REST Get mechanism to extract the
     * aggregated data on the basis of the ID from the aggregatedObject.
     *
     * @param id
     * @return ResponseEntity
     */
    public ResponseEntity<QueryResponse> getQueryAggregatedObject(@RequestParam("ID") final String id) {
        QueryResponse queryResponse= new QueryResponse();
        ArrayList<String> response = processAggregatedObject.processQueryAggregatedObject(id);
        queryResponse.setResponseEntity(response.toString());
        LOGGER.debug("The response is : " + response.toString());
        return new ResponseEntity(queryResponse, HttpStatus.OK);
    }
}