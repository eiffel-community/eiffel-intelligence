/*
   Copyright 2017 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.ei.controller;

import com.ericsson.ei.config.HttpSessionConfig;
import com.ericsson.ei.controller.model.GetSubscriptionResponse;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.services.ISubscriptionService;
import com.ericsson.ei.subscriptionhandler.SubscriptionValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.*;

@Component
@CrossOrigin
@Api(value = "subscription", description = "The Subscription API for the store and retrieve the subscriptions from the database")
public class SubscriptionControllerImpl implements SubscriptionController {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionControllerImpl.class);

    @Value("${ldap.enabled}")
    private boolean authenticate;

    @Autowired
    private ISubscriptionService subscriptionService;

    private SubscriptionValidator subscriptionValidator = new SubscriptionValidator();

    private Map<String, String> errorMap;

    @Override
    @CrossOrigin
    @ApiOperation(value = "Creates the subscription")
    public ResponseEntity<List<SubscriptionResponse>> createSubscription(@RequestBody List<Subscription> subscriptions) {
        errorMap = new HashMap<>();
        String user = (authenticate) ? HttpSessionConfig.getCurrentUser() : "";

        subscriptions.forEach(subscription -> {
            String subscriptionName = subscription.getSubscriptionName();
            try {
                LOG.debug("Subscription create started :: " + subscriptionName);
                subscriptionValidator.validateSubscription(subscription);

                if (!subscriptionService.doSubscriptionExist(subscriptionName)) {
                    subscription.setUserName(user);
                    subscription.setCreated(Instant.now().toEpochMilli());
                    subscriptionService.addSubscription(subscription);
                    LOG.debug("Subscription inserted successfully :: " + subscriptionName);
                } else {
                    LOG.error("Subscription already exists :: " + subscriptionName);
                    errorMap.put(subscriptionName, "Subscription already exists");
                }
            } catch (Exception e) {
                LOG.error("Error on subscription " + subscriptionName + ", " + e.getMessage());
                errorMap.put(subscriptionName, e.getMessage());
            }
        });
        return (errorMap.isEmpty()) ? new ResponseEntity<>(HttpStatus.OK) : new ResponseEntity<>(getSubscriptionResponseList(errorMap), HttpStatus.BAD_REQUEST);
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Returns the subscriptions for given subscription names separated by comma")
    public ResponseEntity<GetSubscriptionResponse> getSubscriptionById(@PathVariable String subscriptionName) {
        List<String> subscriptionNames = Arrays.asList(subscriptionName.split(","));
        List<Subscription> foundSubscriptionList = new ArrayList<>();
        List<String> notFoundSubscriptionList = new ArrayList<>();

        subscriptionNames.forEach(name -> {
            try {
                LOG.debug("Subscription fetch started :: " + name);
                foundSubscriptionList.add(subscriptionService.getSubscription(name));
                LOG.debug("Subscription was fetched :: " + name);
            } catch (SubscriptionNotFoundException e) {
                LOG.error("Subscription was not found :: " + name);
                notFoundSubscriptionList.add(name);
            }
        });
        GetSubscriptionResponse response = new GetSubscriptionResponse();
        response.setFoundSubscriptions(foundSubscriptionList);
        response.setNotFoundSubscriptions(notFoundSubscriptionList);
        HttpStatus httpStatus = (!foundSubscriptionList.isEmpty()) ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, httpStatus);
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Updates the existing subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> updateSubscriptions(@RequestBody List<Subscription> subscriptions) {
        errorMap = new HashMap<>();
        String user = (authenticate) ? HttpSessionConfig.getCurrentUser() : "";

        subscriptions.forEach(subscription -> {
            String subscriptionName = subscription.getSubscriptionName();
            try {
                subscriptionValidator.validateSubscription(subscription);

                if (subscriptionService.doSubscriptionExist(subscriptionName)) {
                    subscription.setUserName(user);
                    subscription.setCreated(Instant.now().toEpochMilli());
                    subscriptionService.modifySubscription(subscription, subscriptionName);
                    LOG.debug("Subscription update completed :: " + subscriptionName);
                } else {
                    LOG.error("Subscription cannot be found :: " + subscriptionName);
                    errorMap.put(subscriptionName, "Subscription cannot be found");
                }
            } catch (Exception e) {
                LOG.error("Error on subscription " + subscriptionName + ", " + e.getMessage());
                errorMap.put(subscriptionName, e.getMessage());
            }
        });
        return (errorMap.isEmpty()) ? new ResponseEntity<>(HttpStatus.OK) : new ResponseEntity<>(getSubscriptionResponseList(errorMap), HttpStatus.BAD_REQUEST);
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Removes the subscriptions from the database")
    public ResponseEntity<List<SubscriptionResponse>> deleteSubscriptionById(@PathVariable String subscriptionName) {
        errorMap = new HashMap<>();
        List<String> subscriptionNames = Arrays.asList(subscriptionName.split(","));

        subscriptionNames.forEach(name -> {
            LOG.debug("Subscription delete started :: " + name);
            if (subscriptionService.deleteSubscription(name)) {
                LOG.debug("Subscription was deleted successfully :: " + name);
            } else {
                LOG.error("Subscription was not found :: " + name);
                errorMap.put(name, "Subscription was not found");
            }
        });
        return (errorMap.isEmpty()) ? new ResponseEntity<>(HttpStatus.OK) : new ResponseEntity<>(getSubscriptionResponseList(errorMap), HttpStatus.BAD_REQUEST);
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Retrieves all the subscriptions")
    public ResponseEntity<List<Subscription>> getSubscriptions() {
        LOG.debug("Subscription get all records started");
        try {
            return new ResponseEntity<>(subscriptionService.getSubscription(), HttpStatus.OK);
        } catch (SubscriptionNotFoundException e) {
            LOG.error(e.getLocalizedMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    private List<SubscriptionResponse> getSubscriptionResponseList(Map<String, String> errorMap) {
        List<SubscriptionResponse> subscriptionResponseList = new ArrayList<>();
        errorMap.forEach((subscriptionName, reason) -> {
            SubscriptionResponse subscriptionResponse = new SubscriptionResponse();
            subscriptionResponse.setSubscription(subscriptionName);
            subscriptionResponse.setReason(reason);
            subscriptionResponseList.add(subscriptionResponse);
        });
        return subscriptionResponseList;
    }
}
