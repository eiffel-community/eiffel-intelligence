
package com.ericsson.ei.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class returns aggregated data given a specific ID of an aggregated object.
 * (Generated with springmvc-raml-parser v.2.0.4)
 *
 */
@RestController
@Validated
@RequestMapping(value = "/queryAggregatedObject", produces = "application/json")
public interface QueryAggregatedObjectController {


    /**
     * This method retrieves aggregated data on a specific aggregated object, given an ID.
     *
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getQueryAggregatedObject(
        @RequestParam
        String id);

}
