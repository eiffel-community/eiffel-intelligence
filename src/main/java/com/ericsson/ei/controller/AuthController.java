
package com.ericsson.ei.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * No description
 * (Generated with springmvc-raml-parser v.0.10.11)
 * 
 */
@RestController
@RequestMapping(value = "/auth", produces = "application/json")
public interface AuthController {


    /**
     * This call for checking if security is enabled
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getAuth();

    /**
     * This call for getting logged in user
     * 
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<?> getLogin();

    /**
     * This call for checking backend status
     * 
     */
    @RequestMapping(value = "/checkStatus", method = RequestMethod.GET)
    public ResponseEntity<?> getCheckStatus();

}
