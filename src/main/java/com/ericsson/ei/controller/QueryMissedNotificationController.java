
package com.ericsson.ei.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class searches for missed notifications in the database.
 * (Generated with springmvc-raml-parser v.2.0.4)
 *
 */
@RestController
@Validated
@RequestMapping(value = "/queryMissedNotifications", produces = "application/json")
public interface QueryMissedNotificationController {


    /**
     * This method returns missed notification(s) given a subscription name.
     *
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getQueryMissedNotifications(
        @RequestParam
        String subscriptionName);

}
