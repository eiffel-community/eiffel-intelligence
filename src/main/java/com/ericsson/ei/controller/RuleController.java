
package com.ericsson.ei.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * No description
 * (Generated with springmvc-raml-parser v.2.0.5)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/rules", produces = "application/json")
public interface RuleController {


    /**
     * This method returns the active rules content.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getRules(HttpServletRequest httpRequest);

}
