
package com.ericsson.ei.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class searches for failed notifications in the database.
 * (Generated with springmvc-raml-parser v.2.0.5)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/failed-notifications", produces = "application/json")
public interface FailedNotificationController {


    /**
     * This method returns failed notification(s) given the subscription name(s).
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getFailedNotifications(
        @RequestParam
        String subscriptionNames, HttpServletRequest httpRequest);

}
