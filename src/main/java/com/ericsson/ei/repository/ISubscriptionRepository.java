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

import com.ericsson.ei.mongodbhandler.MongoDBHandler;

public interface ISubscriptionRepository {

    ArrayList<String> getSubscription(String name);

    Document modifySubscription(String stringSubscription, String subscriptionName) throws JSONException;

    boolean deleteSubscription(String name);

    void addSubscription(String subscription);

    MongoDBHandler getMongoDbHandler();

}
