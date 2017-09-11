/***********************************************************************
 *                                                                     *
 * Copyright Ericsson AB 2017                                          *
 *                                                                     *
 * No part of this software may be reproduced in any form without the  *
 * written permission of the copyright owner.                          *
 *                                                                     *
 ***********************************************************************/
package com.ericsson.ei.queryservice;

import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This class represents the REST end-points for the query service. It can take
 * both query parameters and form parameters as criterias.
 *
 * @author xjibbal
 *
 */

@RestController
@RequestMapping("/ei/query")
public class QueryServiceRestController {

    @Autowired
    private ProcessQueryParams processQueryParams;

    static Logger log = (Logger) LoggerFactory.getLogger(QueryServiceRestController.class);

    /**
     * The REST POST method is used to query Aggregated Objects with the
     * criterias requested, which are present in the request body of the request
     * URL.
     *
     * @param requestBody
     * @return ResponseEntity
     */
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity extractQuery(@RequestBody JsonNode requestBody) {
        JSONArray result = null;
        log.info("requestBody : " + requestBody.toString());
        try {
            result = processQueryParams.filterFormParam(requestBody);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("Final Output : " + result.toString());
        return new ResponseEntity<String>(result.toString(), HttpStatus.OK);
    }

    /**
     * The REST GET method is used to query Aggregated Objects with the
     * criterias requested, which are present as query parameters in the request
     * URL.
     *
     * @param request
     * @return ResponseEntity
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity extractQueryParams(HttpServletRequest request) {
        String url = request.getRequestURI();
        log.info("The url is : " + url);
        JSONArray result = null;
        try {
            result = processQueryParams.filterQueryParam(request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new ResponseEntity<String>(result.toString(), HttpStatus.OK);
    }

}
