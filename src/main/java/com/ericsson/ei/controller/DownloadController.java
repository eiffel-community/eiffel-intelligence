
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
    public ResponseEntity<?> getSubscriptionsTemplate();

    /**
     * This call for getting rules template
     * 
     */
    @RequestMapping(value = "/rulesTemplate", method = RequestMethod.GET)
    public ResponseEntity<?> getRulesTemplate();

    /**
     * This call for getting events template
     * 
     */
    @RequestMapping(value = "/eventsTemplate", method = RequestMethod.GET)
    public ResponseEntity<?> getEventsTemplate();

}
