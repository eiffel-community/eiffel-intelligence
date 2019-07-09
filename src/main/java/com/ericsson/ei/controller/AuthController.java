
package com.ericsson.ei.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class handles requests related to authentication.
 * (Generated with springmvc-raml-parser v.2.0.4)
 *
 */
@RestController
@Validated
@RequestMapping(value = "/auth", produces = "application/json")
public interface AuthController {


    /**
     * This method checks if security is enabled.
     *
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getAuth();

    /**
     * This method returns a logged in user.
     *
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<?> getAuthLogin();

    /**
     * This method checks Eiffel Intelligene backend status.
     *
     */
    @RequestMapping(value = "/checkStatus", method = RequestMethod.GET)
    public ResponseEntity<?> getAuthCheckStatus();

}
