
package com.ericsson.ei.controller;

import java.util.List;
import javax.validation.Valid;
import com.ericsson.ei.controller.model.GetSubscriptionResponse;
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
     * Fetches all subscriptions as list.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getSubscriptions();

    /**
     * Takes the subscription rules, the name for subscription and the user name of the person registering this subscription and saves the subscription in subscription database. The name needs to be unique.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<List<com.ericsson.ei.controller.model.SubscriptionResponse>> createSubscription(
        @Valid
        @RequestBody
        List<com.ericsson.ei.controller.model.Subscription> subscription);

    /**
     * Modify existing Subscriptions.
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseEntity<List<com.ericsson.ei.controller.model.SubscriptionResponse>> updateSubscriptions(
        @Valid
        @RequestBody
        List<com.ericsson.ei.controller.model.Subscription> subscription);

    /**
     * Returns the subscriptions for the given subscription names.
     * 
     */
    @RequestMapping(value = "/{subscriptionNames}", method = RequestMethod.GET)
    public ResponseEntity<GetSubscriptionResponse> getSubscriptionById(
        @PathVariable
        String subscriptionNames);

    /**
     * Removes the subscriptions from the database for the given subscription names.
     * 
     */
    @RequestMapping(value = "/{subscriptionNames}", method = RequestMethod.DELETE)
    public ResponseEntity<List<com.ericsson.ei.controller.model.SubscriptionResponse>> deleteSubscriptionById(
        @PathVariable
        String subscriptionNames);

}
