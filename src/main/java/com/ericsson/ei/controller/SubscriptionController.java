
package com.ericsson.ei.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Provides interaction with Subscription resource
 * (Generated with springmvc-raml-parser v.0.10.11)
 * 
 */
@RestController
@RequestMapping(value = "/subscriptions", produces = "application/json")
public interface SubscriptionController {


    /**
     * List the names of all subscriptions
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<com.ericsson.ei.controller.model.Subscription>> getSubscriptions();

    /**
     * Takes the subscription rules, the name for subscription and the user name of the person registering this subscription and saves the subscription in subscription database. The name needs to be unique.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> createSubscription(
        @javax.validation.Valid
        @org.springframework.web.bind.annotation.RequestBody
        com.ericsson.ei.controller.model.Subscription subscription);

    /**
     * Modify an existing Subscription.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseEntity<com.ericsson.ei.controller.model.SubscriptionResponse> updateSubscriptions(
        @javax.validation.Valid
        @org.springframework.web.bind.annotation.RequestBody
        com.ericsson.ei.controller.model.Subscription subscription);

    /**
     * Returns the subscription rules for given subscription name.
     * 
     */
    @RequestMapping(value = "/{subscriptionName}", method = RequestMethod.GET)
    public ResponseEntity<List<com.ericsson.ei.controller.model.Subscription>> getSubscriptionById(
        @PathVariable(required = false)
        String subscriptionName);

    /**
     * Removes the subscription from the database.
     * 
     */
    @RequestMapping(value = "/{subscriptionName}", method = RequestMethod.DELETE)
    public ResponseEntity<com.ericsson.ei.controller.model.SubscriptionResponse> deleteSubscriptionById(
        @PathVariable(required = false)
        String subscriptionName);

}
