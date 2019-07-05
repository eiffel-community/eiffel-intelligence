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

import com.ericsson.ei.controller.model.QueryBody;
import com.ericsson.ei.queryservice.ProcessQueryParams;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * This class represents the REST end-points for the query service. Criteria is
 * required to have in the request body, while options and filter are optional.
 */
@Component
@CrossOrigin
@Api(value = "query", tags = {"Query"})
public class QueryControllerImpl implements QueryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryControllerImpl.class);

    @Autowired
    private ProcessQueryParams processQueryParams;

    @Override
    @CrossOrigin
    @ApiOperation(value = "Perform a freestyle query to retrieve aggregated objects")
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
            return new ResponseEntity<>(result.toString(), httpStatus);
        } catch (Exception e) {
            String errorMessage = "Failed to extract data from the Aggregated Object using freestyle query. Error message:\n" + e.getMessage();
            LOGGER.error(errorMessage, e);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
