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
package com.ericsson.ei.waitlist;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;

import lombok.Getter;
import lombok.Setter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class WaitListStorageHandler {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(WaitListStorageHandler.class);

    @Getter
    @Value("${waitlist.collection.name}")
    private String collectionName;

    @Getter
    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Getter
    @Value("${waitlist.collection.ttlValue}")
    private int ttlValue;

    @Setter
    @Autowired
    private MongoDBHandler mongoDbHandler;

    @Setter
    @Autowired
    private JmesPathInterface jmesPathInterface;

    public void addEventToWaitList(String event, RulesObject rulesObject) {
        try {
            String condition = "{\"_id\" : \"" + new JSONObject(event).getJSONObject("meta").getString("id") + "\"}";
            List<String> foundEventsInWaitList = mongoDbHandler.find(databaseName, collectionName, condition);
            if (foundEventsInWaitList.isEmpty()) {
                String input = addPropertiesToEvent(event, rulesObject);
                mongoDbHandler.insertDocument(databaseName, collectionName, input);
            }
        } catch (MongoWriteException e) {
            LOGGER.debug("Failed to insert event into waitlist");
            e.printStackTrace();
        }
    }

    public boolean dropDocumentFromWaitList(String document) {
        return mongoDbHandler.dropDocument(databaseName, collectionName, document);
    }

    public List<String> getWaitList() {
        return mongoDbHandler.getAllDocuments(databaseName, collectionName);
    }

    private String addPropertiesToEvent(String event, RulesObject rulesObject) {
        String idRule = rulesObject.getIdRule();
        JsonNode id = jmesPathInterface.runRuleOnEvent(idRule, event);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String time = dateFormat.format(date);
        try {
            date = dateFormat.parse(time);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage(), e);
        }
        BasicDBObject document = new BasicDBObject();
        document.put("_id", id.textValue());
        document.put("Time", date);
        document.put("Event", event);
        mongoDbHandler.createTTLIndex(databaseName, collectionName, "Time", ttlValue);
        return document.toString();
    }

}