
package com.ericsson.ei.controller;

import com.ericsson.ei.controller.model.QueryResponse;
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
@RequestMapping(value = "/queryAggregatedObject", produces = "application/json")
public interface QueryAggregatedObjectController {


    /**
     * No description
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<QueryResponse> getQueryAggregatedObject(
        @RequestParam
        String id);

}
