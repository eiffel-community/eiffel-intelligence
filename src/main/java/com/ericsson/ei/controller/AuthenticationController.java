
package com.ericsson.ei.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class handles requests related to authentication.
 * (Generated with springmvc-raml-parser v.2.0.5)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/authentication", produces = "application/json")
public interface AuthenticationController {


    /**
     * This method checks if security is enabled.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getAuthentication(HttpServletRequest httpRequest);

    /**
     * This method returns a logged in user.
     * 
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<?> getAuthenticationLogin(HttpServletRequest httpRequest);

}
