
package com.ericsson.ei.controller;

import java.util.List;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<List<String>> getSubscriptions();

    /**
     * Takes the subscription rules, the name for subscription and the user name of the person registering this subscription and saves the subscription in subscription database. The name needs to be unique.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> createSubscription(
        @Valid
        @RequestBody
        List<String> string);

    /**
     * Modify an existing Subscription.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseEntity<SubscriptionResponse> updateSubscriptions(
        @Valid
        @RequestBody
        List<String> string);

    /**
     * Returns the subscription rules for given subscription name.
     * 
     */
    @RequestMapping(value = "/{subscriptionName}", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getSubscriptionById(
        @PathVariable(required = false)
        java.lang.String subscriptionName);

    /**
     * Removes the subscription from the database.
     * 
     */
    @RequestMapping(value = "/{subscriptionName}", method = RequestMethod.DELETE)
    public ResponseEntity<SubscriptionResponse> deleteSubscriptionById(
        @PathVariable(required = false)
        java.lang.String subscriptionName);

}
