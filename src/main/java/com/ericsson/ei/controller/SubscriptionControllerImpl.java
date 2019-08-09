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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.AccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.ericsson.ei.config.HttpSessionConfig;
import com.ericsson.ei.controller.model.GetSubscriptionResponse;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.services.ISubscriptionService;
import com.ericsson.ei.subscription.SubscriptionValidator;
import com.ericsson.ei.utils.ResponseMessage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Component
@CrossOrigin
@Api(value = "subscriptions", tags = {"Subscriptions"})
public class SubscriptionControllerImpl implements SubscriptionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionControllerImpl.class);

    private static final String SUBSCRIPTION_NOT_FOUND = "Subscription is not found";
    private static final String SUBSCRIPTION_ALREADY_EXISTS = "Subscription already exists";
    private static final String INVALID_USER = "Unauthorized! You must be logged in as the creator of a subscription to modify it.";

    @Value("${ldap.enabled}")
    private boolean ldapEnabled;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Override
    @CrossOrigin
    @ApiOperation(value = "Creates subscription(s)")
    public ResponseEntity<?> createSubscription(@RequestBody List<Subscription> subscriptions) {
        Map<String, String> errorMap = new HashMap<>();
        String user = (ldapEnabled) ? HttpSessionConfig.getCurrentUser() : "";

        subscriptions.forEach(subscription -> {
            String subscriptionName = subscription.getSubscriptionName();
            try {
                LOGGER.debug("Subscription creation has been started: {}", subscriptionName);
                SubscriptionValidator.validateSubscription(subscription);

                if (!subscriptionService.doSubscriptionExist(subscriptionName)) {
                    subscription.setLdapUserName(user);
                    subscription.setCreated(Instant.now().toEpochMilli());
                    subscriptionService.addSubscription(subscription);
                    LOGGER.debug("Subscription is created successfully: {}", subscriptionName);
                } else {
                    LOGGER.error("Subscription to create already exists: {}", subscriptionName);
                    errorMap.put(subscriptionName, SUBSCRIPTION_ALREADY_EXISTS);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to create subscription {}", subscriptionName, e);
                errorMap.put(subscriptionName, e.getMessage());
            }
        });
        return getPostResponse(errorMap);
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Returns the subscriptions for given subscription names separated by comma")
    public ResponseEntity<?> getSubscriptionByNames(@PathVariable String subscriptionNames) {
        // set is used to prevent subscription names repeating
        Set<String> subscriptionNamesList = new HashSet<>(Arrays.asList(subscriptionNames.split(",")));
        List<Subscription> foundSubscriptionList = new ArrayList<>();
        List<String> notFoundSubscriptionList = new ArrayList<>();

        subscriptionNamesList.forEach(subscriptionName -> {
            try {
                LOGGER.debug("Subscription fetching has been started: {}", subscriptionName);

                // Make sure the password is not sent outside this service.
                Subscription subscription = subscriptionService.getSubscription(subscriptionName);
                subscription.setPassword("");
                foundSubscriptionList.add(subscription);
                LOGGER.debug("Subscription [{}] fetched successfully.", subscriptionName);
            } catch (SubscriptionNotFoundException e) {
                LOGGER.error("Subscription not found: {}", subscriptionName);
                LOGGER.debug("Subscription not found traceback", e);
                notFoundSubscriptionList.add(subscriptionName);
            } catch (Exception e) {
                LOGGER.error("Failed to fetch subscription {}", subscriptionName, e);
                LOGGER.debug("Failed to fetch subscription {}", e);

                notFoundSubscriptionList.add(subscriptionName);
            }
        });
        GetSubscriptionResponse response = new GetSubscriptionResponse();
        response.setFoundSubscriptions(foundSubscriptionList);
        response.setNotFoundSubscriptions(notFoundSubscriptionList);
        HttpStatus httpStatus = (!foundSubscriptionList.isEmpty()) ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        if (httpStatus == HttpStatus.NOT_FOUND) {
            String errorMessage = "Failed to fetch subscriptions:\n" + notFoundSubscriptionList.toString();
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, httpStatus);
        }
        return new ResponseEntity<>(response, httpStatus);
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Updates existing subscription(s)")
    public ResponseEntity<?> updateSubscriptions(@RequestBody List<Subscription> subscriptions) {
        Map<String, String> errorMap = new HashMap<>();
        String user = (ldapEnabled) ? HttpSessionConfig.getCurrentUser() : "";

        subscriptions.forEach(subscription -> {
            String subscriptionName = subscription.getSubscriptionName();
            LOGGER.debug("Subscription updating has been started: " + subscriptionName);
            try {
                SubscriptionValidator.validateSubscription(subscription);
                if (subscriptionService.doSubscriptionExist(subscriptionName)) {
                    subscription.setLdapUserName(user);
                    subscription.setCreated((float) Instant.now().toEpochMilli());
                    subscriptionService.modifySubscription(subscription, subscriptionName);
                    LOGGER.debug("Updating subscription completed: {}", subscriptionName);
                } else {
                    LOGGER.error("Subscription to update was not found: {}", subscriptionName);
                    errorMap.put(subscriptionName, SUBSCRIPTION_NOT_FOUND);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to update subscription {}",subscriptionName, e);
                errorMap.put(subscriptionName, e.getMessage());
            }
        });
        return getPutResponse(errorMap);
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Removes subscription(s)")
    public ResponseEntity<?> deleteSubscriptionByNames(@PathVariable String subscriptionNames) {
        Map<String, String> errorMap = new HashMap<>();
        // set is used to prevent subscription names repeating
        Set<String> subscriptionNamesList = new HashSet<>(Arrays.asList(subscriptionNames.split(",")));

        subscriptionNamesList.forEach(subscriptionName -> {
            LOGGER.debug("Subscription deleting has been started: {}", subscriptionName);

            try {
                if (subscriptionService.deleteSubscription(subscriptionName)) {
                    LOGGER.debug("Subscription was deleted successfully: {}", subscriptionName);
                } else {
                    LOGGER.error("Subscription to delete was not found: {}", subscriptionName);
                    errorMap.put(subscriptionName, SUBSCRIPTION_NOT_FOUND);
                }
            } catch (AccessException e) {
                LOGGER.error("Failed to delete subscription: {}", subscriptionName, e);
                errorMap.put(subscriptionName, INVALID_USER);
            } catch (Exception e) {
                LOGGER.error("Failed to delete subscriptions.", e);
                errorMap.put(subscriptionName, e.getClass().toString() + " : " + e.getMessage());
            }
        });
        return getDeleteResponse(errorMap);
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Retrieves all subscriptions")
    public ResponseEntity<?> getSubscriptions() {
        LOGGER.debug("Fetching subscriptions has been initiated");
        try {
            // Make sure the password is not sent outside this service.
            List<Subscription> subscriptions = subscriptionService.getSubscriptions();
            for (Subscription subscription : subscriptions) {
                subscription.setPassword("");
            }

            return new ResponseEntity<>(subscriptions, HttpStatus.OK);
        } catch (SubscriptionNotFoundException e) {
            LOGGER.info(e.getMessage(),e);
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Internal Server Error: Failed to fetch subscriptions.";
            LOGGER.error(errorMessage, e);
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<?> getPostResponse(Map<String, String> errorMap) {
        String errorMessage = "Failed to create Subscriptions:\n";
        return getResponse(errorMap, errorMessage);
    }

    private ResponseEntity<?> getPutResponse(Map<String, String> errorMap) {
        String errorMessage = "Failed to update subscription:\n";
        return getResponse(errorMap, errorMessage);
    }

    private ResponseEntity<?> getDeleteResponse(Map<String, String> errorMap) {
        String errorMessage = "Failed to delete subscriptions:\n";
        return getResponse(errorMap, errorMessage);
    }

    private ResponseEntity<?> getResponse(Map<String, String> errorMap, String errorMessage) {
        if (!errorMap.isEmpty()) {
            for (Map.Entry<String, String> entry : errorMap.entrySet()) {
                errorMessage += "" + entry.getKey() + " :: " + entry.getValue() + "\n";
            }
            String errorJsonAsString = ResponseMessage.createJsonMessage(errorMessage);
            return new ResponseEntity<>(errorJsonAsString, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
