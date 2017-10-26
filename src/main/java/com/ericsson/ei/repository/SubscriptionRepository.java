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
package com.ericsson.ei.repository;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;

@Component
public class SubscriptionRepository implements ISubscriptionRepository {
    
    @Value("${subscription.collection.name}")
    private String collectionName;
    
    @Value("${database.name}")
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
