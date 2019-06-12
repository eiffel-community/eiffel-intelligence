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

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;


@Component
public class SubscriptionRepository implements ISubscriptionRepository {

    @Value("${subscription.collection.name}")
    private String collectionName;

    @Value("${spring.data.mongodb.database}")
    private String dataBaseName;

    @Autowired
    MongoDBHandler mongoDBHandler;

    @Override
    public void addSubscription(String StringSubscription) {
        mongoDBHandler.insertDocument(dataBaseName, collectionName, StringSubscription);
    }

    @Override
    public ArrayList<String> getSubscription(String getQuery) {
        return mongoDBHandler.find(dataBaseName, collectionName, getQuery);
    }

    @Override
    public Document modifySubscription(String query, String stringSubscription) throws JSONException {
        JSONObject subscriptionJson = new JSONObject(stringSubscription);
        if(subscriptionJson.has("password") && subscriptionJson.getString("password").equals("")) {
            subscriptionJson.remove("password");
        }

        JSONObject updateObject = new JSONObject();
        updateObject.put("$set", subscriptionJson);
        return mongoDBHandler.findAndModify(dataBaseName, collectionName, query, updateObject.toString());
    }

    @Override
    public boolean deleteSubscription(String query) {
        return mongoDBHandler.dropDocument(dataBaseName, collectionName, query);
    }

    @Override
    public MongoDBHandler getMongoDbHandler() {
        return mongoDBHandler;
    }
}
