
package com.ericsson.ei.controller;

import com.ericsson.ei.controller.model.QueryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * No description
 * (Generated with springmvc-raml-parser v.0.10.11)
 * 
 */
@RestController
@RequestMapping(value = "/queryMissedNotifications", produces = "application/json")
public interface QueryMissedNotificationController {


    /**
     * List the missed notification based on the subscriptionName in the query parameter.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<QueryResponse> getQueryMissedNotifications(
        @RequestParam
        String subscriptionName);

}
