
package com.ericsson.ei.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class is responsible for providing templates of rules, Eiffel events and subscriptions.
 * (Generated with springmvc-raml-parser v.2.0.5)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/templates", produces = "application/json")
public interface TemplateController {


    /**
     * This method returns a list of endpoints for downloading templates.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getTemplates(HttpServletRequest httpRequest);

    /**
     * This method returns subscription templates.
     * 
     */
    @RequestMapping(value = "/subscriptions", method = RequestMethod.GET)
    public ResponseEntity<?> getTemplatesSubscriptions(HttpServletRequest httpRequest);

    /**
     * This method returns a template for rules.
     * 
     */
    @RequestMapping(value = "/rules", method = RequestMethod.GET)
    public ResponseEntity<?> getTemplatesRules(HttpServletRequest httpRequest);

    /**
     * This method returns a template for Eiffel events.
     * 
     */
    @RequestMapping(value = "/events", method = RequestMethod.GET)
    public ResponseEntity<?> getTemplatesEvents(HttpServletRequest httpRequest);

}
