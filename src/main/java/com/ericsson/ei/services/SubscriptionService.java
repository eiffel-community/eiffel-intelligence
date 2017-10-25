package com.ericsson.ei.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.repository.ISubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

@Component
public class SubscriptionService implements ISubscriptionService {

    @Value("${spring.application.name}") private String SpringApplicationName;

    private static final String SUBSCRIPTION_NAME = "{'subscriptionName':'%s'}";
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
        try {
            String StringSubscription = mapper.writeValueAsString(subscription);
            String subscriptionNameQuery = String.format(SUBSCRIPTION_NAME, subscriptionName);
            return subscriptionRepository.modifySubscription(subscriptionNameQuery, StringSubscription);
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    @Override
    public boolean deleteSubscription(String name) {
        String query = String.format(SUBSCRIPTION_NAME, name);
        return subscriptionRepository.deleteSubscription(query);
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
    
}
