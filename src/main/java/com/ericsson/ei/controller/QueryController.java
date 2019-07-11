
package com.ericsson.ei.controller;

import javax.validation.Valid;
import com.ericsson.ei.controller.model.QueryBody;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class retrieves aggregated objects given a free style query. Criteria is required to have in the request body, while options and filter are optional.
 * (Generated with springmvc-raml-parser v.2.0.4)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/query", produces = "application/json")
public interface QueryController {


    /**
     * This method is used to query aggregated objects with the requested criteria, which must be present in the request body. Options and filter are optional.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> createQuery(
        @Valid
        @RequestBody
        QueryBody queryBody);

    /**
     * This method retrieves aggregated data on a specific aggregated object, given an ID.
     * 
     */
    @RequestMapping(value = "/aggregated-object", method = RequestMethod.GET)
    public ResponseEntity<?> getQueryAggregatedObject(
        @RequestParam
        String id);

    /**
     * This method returns missed notification(s) given a subscription name.
     * 
     */
    @RequestMapping(value = "/missed-notifications", method = RequestMethod.GET)
    public ResponseEntity<?> getQueryMissedNotifications(
        @RequestParam
        String subscriptionName);

}
