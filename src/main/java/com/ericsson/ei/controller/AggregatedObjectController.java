
package com.ericsson.ei.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class returns aggregated data given a specific ID of an aggregated object and can perform free style query as well..
 * (Generated with springmvc-raml-parser v.2.0.5)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/aggregated-objects", produces = "application/json")
public interface AggregatedObjectController {


    /**
     * This method retrieves aggregated data on a specific aggregated object, given an ID.
     * 
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getAggregatedObjectById(
        @PathVariable
        String id, HttpServletRequest httpRequest);

    /**
     * The REST POST method is used to query aggregated objects with the requested criteria, which are present in the request body.
     * 
     */
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public ResponseEntity<?> createAggregatedObjectsQuery(
        @Valid
        @RequestBody
        QueryBody queryBody, HttpServletRequest httpRequest);

}
