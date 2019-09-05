
package com.ericsson.ei.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Provides REST API for handling subscriptions.
 * (Generated with springmvc-raml-parser v.2.0.5)
 * 
 */
@RestController
@Validated
@RequestMapping(value = "/subscriptions", produces = "application/json")
public interface SubscriptionController {


    /**
     * This method retrieves all subscriptions from the database. Specific subscriptions can be retrieved by using the 'subscriptionNames' parameter.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getSubscriptions(
        @RequestParam(required = false)
        String subscriptionNames, HttpServletRequest httpRequest);

    /**
     * This method creates new subscription(s) and saves in the database. The name of a subscription must be unique.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> createSubscription(
        @Valid
        @RequestBody
        List<com.ericsson.ei.controller.model.Subscription> subscription, HttpServletRequest httpRequest);

    /**
     * This method modifies existing subscriptions.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseEntity<?> updateSubscriptions(
        @Valid
        @RequestBody
        List<com.ericsson.ei.controller.model.Subscription> subscription, HttpServletRequest httpRequest);

    /**
     * This method removes subscriptions from the database. It requires a list of names by using the 'subscriptionNames' parameter
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteSubscriptions(
        @RequestParam
        String subscriptionNames, HttpServletRequest httpRequest);

    /**
     * This method returns the subscription for the given subscription name.
     * 
     */
    @RequestMapping(value = "/{subscriptionName}", method = RequestMethod.GET)
    public ResponseEntity<?> getSubscriptionByName(
        @PathVariable
        String subscriptionName, HttpServletRequest httpRequest);

    /**
     * This method removes the subscription from the database for the given subscription name.
     * 
     */
    @RequestMapping(value = "/{subscriptionName}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteSubscriptionByName(
        @PathVariable
        String subscriptionName, HttpServletRequest httpRequest);

}
