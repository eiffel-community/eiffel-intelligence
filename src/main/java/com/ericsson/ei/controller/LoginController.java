
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
@RequestMapping(value = "/auth/login", produces = "application/json")
public interface LoginController {


    /**
     * This call for getting logged in user
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getAuthLogin();

}
