package com.ericsson.ei.repository;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;

@Component
public class SubscriptionRepository implements ISubscriptionRepository {
    
    @Value("${subscriptionCollectionName}")
    private String collectionName;
    
    @Value("${subscriptionDataBaseName}")
    private String dataBaseName;
    
    @Autowired
    MongoDBHandler mongoDBHandler;
    
    @Override
    public boolean addSubscription(String StringSubscription) {
        return mongoDBHandler.insertDocument(dataBaseName, collectionName, StringSubscription);
        
    }
    
    @Override
    public ArrayList<String> getSubscription(String getQuery) {
        return mongoDBHandler.find(dataBaseName, collectionName, getQuery);
    }
    
    @Override
    public boolean modifySubscription(String stringSubscription, String subscriptionName) {
        return mongoDBHandler.updateDocument(dataBaseName, collectionName, stringSubscription, subscriptionName);
    }
    
    @Override
    public boolean deleteSubscription(String query) {
        return mongoDBHandler.dropDocument(dataBaseName, collectionName, query);
    }
    
}
