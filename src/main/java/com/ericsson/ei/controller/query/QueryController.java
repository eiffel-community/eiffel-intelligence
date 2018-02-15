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

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * This class represents the REST end-points for the query service. It can take
 * both query parameters and form parameters as criterias.
 */
@RestController
@RequestMapping("/ei/query")
public interface QueryController {

    /**
     * The REST POST method is used to query Aggregated Objects with the
     * criterias requested, which are present in the request body of the request
     * URL.
     *
     * @param requestBody
     * @return ResponseEntity
     */
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    ResponseEntity extractQuery(@RequestBody JsonNode requestBody);

    /**
     * The REST GET method is used to query Aggregated Objects with the
     * criterias requested, which are present as query parameters in the request
     * URL.
     *
     * @param request
     * @return ResponseEntity
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    ResponseEntity extractQueryParams(HttpServletRequest request);
}
