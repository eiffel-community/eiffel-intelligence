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

import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This class represents the REST end-points for the query service. It can take
 * both query parameters and form parameters as criterias.
 */
@Component
@CrossOrigin
@Api(value = "query", description = "REST endpoints for the freestyle query service")
public class QueryControllerImpl implements QueryController {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(QueryControllerImpl.class);

    @Autowired
    private ProcessQueryParams processQueryParams;

    @Override
    @CrossOrigin
    @ApiOperation(value = "")
    public ResponseEntity<?> updateQuery(@RequestParam(value = "request") String request) {
        try {
            JSONArray result = processQueryParams.filterFormParam(new ObjectMapper().readTree(request));
            return new ResponseEntity<>(result.toString(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Failed to extract data from the Aggregated Object using freestyle query. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, e);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "")
    public ResponseEntity<?> getQuery(@RequestParam(value = "request") final String request) {
        try {
            JSONArray result = processQueryParams.filterQueryParam(request);
            LOGGER.debug("Final Output : " + result.toString());
            return new ResponseEntity<>(result.toString(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Failed to extract data from the Aggregated Object using freestyle query. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, e);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}