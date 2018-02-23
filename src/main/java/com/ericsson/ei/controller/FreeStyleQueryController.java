
package com.ericsson.ei.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * No description
 * (Generated with springmvc-raml-parser v.0.10.11)
 * 
 */
@RestController
@RequestMapping(value = "/freeStyleQuery", produces = "application/json")
public interface FreeStyleQueryController {


    /**
     * The REST GET method is used to query Aggregated Objects with the criterias requested, which are present as query parameters in the request URL.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<com.ericsson.ei.controller.model.QueryResponse> getFreeStyleQuery(
        @RequestParam
        String request);

    /**
     * The REST POST method is used to query Aggregated Objects with the criterias requested, which are present in the request body of the request URL.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<com.ericsson.ei.controller.model.QueryResponse> updateFreeStyleQuery(
        @RequestParam
        String query);

}
