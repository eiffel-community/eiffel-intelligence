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
package com.ericsson.ei.queryservice;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents the mechanism to extract the aggregated data on the
 * basis of the SubscriptionName from the Missed Notification Object.
 */
@Component
public class ProcessMissedNotification {

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDataBaseName;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ProcessMissedNotification.class);

    @Autowired
    private MongoDBHandler handler;

    /**
     * The method is responsible to extract the data on the basis of the
     * subscriptionName from the Missed Notification Object.
     *
     * @param subscriptionName
     * @return ArrayList
     */
    public List<String> processQueryMissedNotification(String subscriptionName) {
        ObjectMapper mapper = new ObjectMapper();
        String condition = "{\"subscriptionName\" : \"" + subscriptionName + "\"}";
        LOGGER.debug("The condition is : " + condition);
        JsonNode jsonCondition = null;
        try {
            jsonCondition = mapper.readTree(condition);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("The Json condition is : " + jsonCondition);
        ArrayList<String> output = handler.find(missedNotificationDataBaseName, missedNotificationCollectionName,
                jsonCondition.toString());
        return output.stream().map(a -> {
            try {
                return mapper.readTree(a).path("AggregatedObject").toString();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            return null;
        }).collect(Collectors.toList());

    }

    /**
     * This method is responsible for fetching all the missed notifications from
     * the missed Notification database and return it as JSONArray.
     *
     * @param request
     * @param MissedNotificationDataBaseName
     * @param MissedNotificationCollectionName
     * @return JSONArray
     */
    public JSONArray processQueryMissedNotification(JsonNode request, String MissedNotificationDataBaseName, String MissedNotificationCollectionName) {
        DB db = new MongoClient().getDB(MissedNotificationDataBaseName);
        Jongo jongo = new Jongo(db);
        MongoCollection aggObjects = jongo.getCollection(MissedNotificationCollectionName);
        LOGGER.debug("Successfully connected to MissedNotification database");
        MongoCursor<Document> allDocuments = aggObjects.find(request.toString()).as(Document.class);
        LOGGER.debug("Number of document returned from Notification collection is : " + allDocuments.count());
        JSONArray jsonArray = new JSONArray();
        JSONObject doc = null;
        while (allDocuments.hasNext()) {
            Document temp = allDocuments.next();
            try {
                doc = new JSONObject(temp.toJson());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            jsonArray.put(doc);
        }
        return jsonArray;
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("The Aggregated Database is : " + missedNotificationDataBaseName);
        LOGGER.debug("The Aggregated Collection is : " + missedNotificationCollectionName);
    }

}
