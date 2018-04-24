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
package com.ericsson.ei.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.repository.ISubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SubscriptionService implements ISubscriptionService {

    @Value("${spring.application.name}") private String SpringApplicationName;
    
    @Value("${database.name}") private String dataBaseName;
    
    @Value("{subscription.collection.repeatFlagHandlerName}") private String RepeatFlagHandlerCollection;

    private static final String SUBSCRIPTION_NAME = "{'subscriptionName':'%s'}";
    private static final String SUBSCRIPTION_ID = "{'subscriptionId':'%s'}";
    @Autowired
    ISubscriptionRepository subscriptionRepository;
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionService.class);
    
    @Override
    public boolean addSubscription(Subscription subscription) {
        ObjectMapper mapper = new ObjectMapper();
        String stringSubscription;
        try {
            stringSubscription = mapper.writeValueAsString(subscription);
            return subscriptionRepository.addSubscription(stringSubscription);
        } catch (JsonProcessingException e) {
            return false;
        }
        
    }
    
    @Override
    public Subscription getSubscription(String name) throws SubscriptionNotFoundException {
        
        String query = String.format(SUBSCRIPTION_NAME, name);
        ArrayList<String> list = subscriptionRepository.getSubscription(query);
        ObjectMapper mapper = new ObjectMapper();
        if (list.isEmpty()) {
            throw new SubscriptionNotFoundException("No record found for the Subscription Name:" + name);
        }
        for (String input : list) {
            Subscription subscription;
            try {

                subscription = mapper.readValue(input, Subscription.class);
                // Inject aggregationtype
                subscription.setAggregationtype(SpringApplicationName);
                return subscription;
                //return mapper.readValue(input, Subscription.class);


            } catch (IOException e) {
                LOG.error("malformed json string");
            }
        }
        return null;
    }
    
    @Override
    public boolean doSubscriptionExist(String name) {
        String query = String.format(SUBSCRIPTION_NAME, name);
        ArrayList<String> list = subscriptionRepository.getSubscription(query);
        if (list.isEmpty()) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean modifySubscription(Subscription subscription, String subscriptionName) {
        ObjectMapper mapper = new ObjectMapper();
        boolean result = false;
        try {
            String StringSubscription = mapper.writeValueAsString(subscription);
            String subscriptionNameQuery = String.format(SUBSCRIPTION_NAME, subscriptionName);
            result = subscriptionRepository.modifySubscription(subscriptionNameQuery, StringSubscription);
            
            if (result) {
            	String subscriptionIdQuery = String.format(SUBSCRIPTION_ID, subscriptionName);
            	if (!cleanSubscriptionRepeatFlagHandlerDb(subscriptionIdQuery)) {
            		LOG.error("Failed to clean subscription \"" + subscriptionName + "\" matched AggregatedObjIds from RepeatFlagHandler database");
            	}
            }
            
        } catch (JsonProcessingException e) {
        	LOG.error(e.getMessage(), e);
            return false;
        }
        return result;
    }
    
    @Override
    public boolean deleteSubscription(String name) {
        String subscriptionNameQuery = String.format(SUBSCRIPTION_NAME, name);
        boolean result = subscriptionRepository.deleteSubscription(subscriptionNameQuery);
        if (result) {
        	String subscriptionIdQuery = String.format(SUBSCRIPTION_ID, name);
        	if(!cleanSubscriptionRepeatFlagHandlerDb(subscriptionIdQuery)) {
        		LOG.error("Failed to clean subscription \"" + name + "\" matched AggregatedObjIds from RepeatFlagHandler database");
        	}
        }
        return result;
    }
    
    @Override
    public List<Subscription> getSubscription() throws SubscriptionNotFoundException {
        String query = "{}";
        ArrayList<String> list = subscriptionRepository.getSubscription(query);
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        ObjectMapper mapper = new ObjectMapper();
        if (list.isEmpty()) {
            throw new SubscriptionNotFoundException("Empty Subscription in repository");
        }
        for (String input : list) {
            Subscription subscription;
            try {

                subscription = mapper.readValue(input, Subscription.class);
                // Inject aggregationtype
                subscription.setAggregationtype(SpringApplicationName);

                subscriptions.add(subscription);
            } catch (IOException e) {
                LOG.error("malformed json string");
            }
            
        }
        return subscriptions;
    }
    
    private boolean cleanSubscriptionRepeatFlagHandlerDb(String subscriptionNameQuery) {
    	LOG.debug("Cleaning and removing matched subscriptions AggrObjIds in ReapeatHandlerFlag database with query: " + subscriptionNameQuery);
    	MongoDBHandler mongoDbHandler = subscriptionRepository.getMongoDbHandler();
    	return mongoDbHandler.dropDocument(dataBaseName, RepeatFlagHandlerCollection, subscriptionNameQuery);
    }
    
}
