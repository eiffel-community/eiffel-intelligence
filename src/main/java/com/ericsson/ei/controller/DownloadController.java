
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
@RequestMapping(value = "/download", produces = "application/json")
public interface DownloadController {


    /**
     * This method returns a list of endpoints for downloading templates.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getDownload(HttpServletRequest httpRequest);

    /**
     * This method returns a subscription template.
     * 
     */
    @RequestMapping(value = "/subscriptionsTemplate", method = RequestMethod.GET)
    public ResponseEntity<?> getDownloadSubscriptionsTemplate(HttpServletRequest httpRequest);

    /**
     * This method returns a template for rules.
     * 
     */
    @RequestMapping(value = "/rulesTemplate", method = RequestMethod.GET)
    public ResponseEntity<?> getDownloadRulesTemplate(HttpServletRequest httpRequest);

    /**
     * This method returns a template for Eiffel events.
     * 
     */
    @RequestMapping(value = "/eventsTemplate", method = RequestMethod.GET)
    public ResponseEntity<?> getDownloadEventsTemplate(HttpServletRequest httpRequest);

}
