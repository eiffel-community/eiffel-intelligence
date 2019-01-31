
package com.ericsson.ei.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * No description
 * (Generated with springmvc-raml-parser v.2.0.4)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/download", produces = "application/json")
public interface DownloadController {


    /**
     * This call for getting list of available templates
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getDownload();

    /**
     * This call for getting subscriptions template
     * 
     */
    @RequestMapping(value = "/subscriptionsTemplate", method = RequestMethod.GET)
    public ResponseEntity<?> getDownloadSubscriptionsTemplate();

    /**
     * This call for getting rules template
     * 
     */
    @RequestMapping(value = "/rulesTemplate", method = RequestMethod.GET)
    public ResponseEntity<?> getDownloadRulesTemplate();

    /**
     * This call for getting events template
     * 
     */
    @RequestMapping(value = "/eventsTemplate", method = RequestMethod.GET)
    public ResponseEntity<?> getDownloadEventsTemplate();

}
