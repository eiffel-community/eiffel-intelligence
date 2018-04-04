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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.exception.SubscriptionValidationException;
import com.ericsson.ei.services.ISubscriptionService;
import com.ericsson.ei.subscriptionhandler.SubscriptionValidator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Component
@CrossOrigin
@Api(value = "subscription", description = "The Subscription API for the store and retrieve the subscriptions from the database")
public class SubscriptionControllerImpl implements SubscriptionController {

    @Autowired
    private ISubscriptionService subscriptionService;

    private SubscriptionValidator subscriptionValidator = new SubscriptionValidator();

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionControllerImpl.class);

    @Override
    @CrossOrigin
    @ApiOperation(value = "Creates the subscription")
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody List<Subscription> subscriptions) {
        ResponseEntity<SubscriptionResponse> subResponse = null;
        SubscriptionResponse subscriptionResponse = new SubscriptionResponse();
        for (Subscription subscription :  subscriptions){
            subResponse = null;
            try {
                subscription.setCreated(Instant.now().toEpochMilli());
                subscriptionValidator.validateSubscription(subscription);
            } catch (SubscriptionValidationException e) {
                String msg = "Validation of Subscription parameters on:" + subscription.getSubscriptionName()
                        + " failed! Error: " + e.getMessage();
                LOG.error(msg);
                subscriptionResponse.setMsg(msg);
                subscriptionResponse.setStatusCode(HttpStatus.PRECONDITION_FAILED.value());
                subResponse = new ResponseEntity<SubscriptionResponse>(subscriptionResponse, HttpStatus.PRECONDITION_FAILED);
            }

            if (!subscriptionService.doSubscriptionExist(subscription.getSubscriptionName())) {
                subscriptionService.addSubscription(subscription);
                LOG.info("Subscription :" + subscription.getSubscriptionName() + " Inserted Successfully");
                subscriptionResponse.setMsg("Inserted Successfully");
                subscriptionResponse.setStatusCode(HttpStatus.OK.value());
                subResponse = new ResponseEntity<SubscriptionResponse>(subscriptionResponse, HttpStatus.OK);

            } else {
                LOG.error("Subscription :" + subscription.getSubscriptionName() + " already exists");
                subscriptionResponse.setMsg("Subscription already exists");
                subscriptionResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                subResponse = new ResponseEntity<SubscriptionResponse>(subscriptionResponse, HttpStatus.BAD_REQUEST);
            }
        }
        return subResponse;
    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Returns the subscription rules for given subscription name")
    public ResponseEntity<List<Subscription>> getSubscriptionById(@PathVariable String subscriptionName) {
        List<Subscription> subscriptionList = new ArrayList<Subscription>();
        try {
            LOG.info("Subscription :" + subscriptionName + " fetch started");
            subscriptionList.add(subscriptionService.getSubscription(subscriptionName));
            LOG.info("Subscription :" + subscriptionName + " fetched");
            return new ResponseEntity<List<Subscription>>(subscriptionList, HttpStatus.OK);
        } catch (SubscriptionNotFoundException e) {
            LOG.error("Subscription :" + subscriptionName + " not found in records");
            return new ResponseEntity<List<Subscription>>(subscriptionList, HttpStatus.OK);

        }

    }

    @Override

    // @CrossOrigin
    @ApiOperation(value = "Update the existing subscription by the subscription name")
    public ResponseEntity<SubscriptionResponse> updateSubscriptions(@RequestBody List<Subscription> subscriptions) {
        Subscription subscription = subscriptions.get(0);
        String subscriptionName = subscription.getSubscriptionName();
        LOG.info("Subscription :" + subscriptionName + " update started");
        SubscriptionResponse subscriptionResponse = new SubscriptionResponse();

        try {
            subscription.setCreated(Instant.now().toEpochMilli());
            subscriptionValidator.validateSubscription(subscription);
        } catch (SubscriptionValidationException e) {
            String msg = "Validation of Subscription parameters on:" + subscription.getSubscriptionName()
                    + " failed! Error: " + e.getMessage();
            LOG.error(msg);
            subscriptionResponse.setMsg(msg);
            subscriptionResponse.setStatusCode(HttpStatus.PRECONDITION_FAILED.value());
            return new ResponseEntity<SubscriptionResponse>(subscriptionResponse, HttpStatus.PRECONDITION_FAILED);
        }

        if (subscriptionService.doSubscriptionExist(subscriptionName)) {
            subscriptionService.modifySubscription(subscription, subscriptionName);
            LOG.info("Subscription :" + subscriptionName + " update completed");
            subscriptionResponse.setMsg("Updated Successfully");
            subscriptionResponse.setStatusCode(HttpStatus.OK.value());
            return new ResponseEntity<SubscriptionResponse>(subscriptionResponse, HttpStatus.OK);

        } else {
            LOG.error("Subscription :" + subscription.getSubscriptionName() + " can't be found.");
            subscriptionResponse.setMsg("Subscription can't be found");
            subscriptionResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<SubscriptionResponse>(subscriptionResponse, HttpStatus.BAD_REQUEST);
        }

    }
    
    @Override
    @CrossOrigin
    @ApiOperation(value = "Removes the subscription from the database")
    public ResponseEntity<SubscriptionResponse> deleteSubscriptionById(@PathVariable String subscriptionName) {
        SubscriptionResponse subscriptionResponse = new SubscriptionResponse();
        LOG.info("Subscription :" + subscriptionName + " delete started");
        if (subscriptionService.deleteSubscription(subscriptionName)) {
            LOG.info("Subscription :" + subscriptionName + " deleted Successfully");
            subscriptionResponse.setMsg("Deleted Successfully");
            subscriptionResponse.setStatusCode(HttpStatus.OK.value());
            return new ResponseEntity<SubscriptionResponse>(subscriptionResponse, HttpStatus.OK);
        } else {
            LOG.info("Subscription :" + subscriptionName + " delete completed :: Record not found for delete");
            subscriptionResponse.setMsg("Record not found for delete");
            subscriptionResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<SubscriptionResponse>(subscriptionResponse, HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    @CrossOrigin
    @ApiOperation(value = "Retrieve all the subscriptions")
    public ResponseEntity<List<Subscription>> getSubscriptions() {
        LOG.info("Subscription : get all records started");
        List<Subscription> subscriptionList = new ArrayList<Subscription>();
        try {
            subscriptionList = subscriptionService.getSubscription();
            return new ResponseEntity<List<Subscription>>(subscriptionList, HttpStatus.OK);
        } catch (SubscriptionNotFoundException e) {
            LOG.error(e.getLocalizedMessage());
            return new ResponseEntity<List<Subscription>>(subscriptionList, HttpStatus.OK);
        }
    }
}
