
package com.ericsson.ei.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class returns status information about Eiffel Intelligence and it's dependencies.
 * (Generated with springmvc-raml-parser v.2.0.4)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/status", produces = "application/json")
public interface StatusController {


    /**
     * Returns status information about Eiffel Intelligence and services Eiffel Intelligence is dependent on.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getStatus();

}
