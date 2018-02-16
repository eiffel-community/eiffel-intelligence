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
package com.ericsson.ei.controller.query;

import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

/**
 * This class represents the REST end-points for the query service. It can take
 * both query parameters and form parameters as criterias.
 */
@Component
@CrossOrigin
@Api(value = "query", description = "REST end-points for the freestyle query service")
public class QueryControllerImpl implements QueryController {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(QueryControllerImpl.class);

    @Autowired
    private ProcessQueryParams processQueryParams;

    @Override
    @CrossOrigin
    @ApiOperation(value = "")
    public ResponseEntity extractQuery(@RequestBody JsonNode requestBody) {
        JSONArray result = null;
        LOGGER.info("requestBody : " + requestBody.toString());
        try {
            result = processQueryParams.filterFormParam(requestBody);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("Final Output : " + result.toString());
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "")
    public ResponseEntity extractQueryParams(HttpServletRequest request) {
        String url = request.getRequestURI();
        LOGGER.info("The url is : " + url);
        JSONArray result = null;
        try {
            result = processQueryParams.filterQueryParam(request);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }
}
