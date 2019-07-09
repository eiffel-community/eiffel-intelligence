
package com.ericsson.ei.controller;

import java.util.List;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Provides REST API for handling subscriptions.
 * (Generated with springmvc-raml-parser v.2.0.4)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/subscriptions", produces = "application/json")
public interface SubscriptionController {


    /**
     * This method retrieves all subscriptions from the database.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getSubscriptions();

    /**
     * This method creates new subscription(s) and saves in the database. The name of a subscription must be unique.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> createSubscription(
        @Valid
        @RequestBody
        List<com.ericsson.ei.controller.model.Subscription> subscription);

    /**
     * This method modifies existing subscriptions.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseEntity<?> updateSubscriptions(
        @Valid
        @RequestBody
        List<com.ericsson.ei.controller.model.Subscription> subscription);

    /**
     * This method returns the subscriptions for the given subscription names.
     * 
     */
    @RequestMapping(value = "/{subscriptionNames}", method = RequestMethod.GET)
    public ResponseEntity<?> getSubscriptionByNames(
        @PathVariable
        String subscriptionNames);

    /**
     * This method removes the subscriptions from the database for the given subscription names.
     * 
     */
    @RequestMapping(value = "/{subscriptionNames}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteSubscriptionByNames(
        @PathVariable
        String subscriptionNames);

}
