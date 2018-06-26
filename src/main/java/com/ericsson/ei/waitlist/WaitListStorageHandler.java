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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;

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

    public void addEventToWaitList(String event, RulesObject rulesObject) throws Exception {
        String condition = "{\"_id\" : \"" + new JSONObject(event).getJSONObject("meta").getString("id") + "\"}";
        List<String> foundEventsInWaitList = mongoDbHandler.find(databaseName, collectionName, condition);
        foundEventsInWaitList.forEach(e -> System.out.println("EVENT :: " + e));
        System.out.println("NUMBER OF FOUND EVENTS :: " + foundEventsInWaitList.size());
        if (foundEventsInWaitList.isEmpty()) {
            System.out.println("TRUE");
            String input = addPropertiesToEvent(event, rulesObject);
            boolean result = mongoDbHandler.insertDocument(databaseName, collectionName, input);
            List<String> waitList = mongoDbHandler.getAllDocuments(databaseName, collectionName);
            waitList.forEach(w -> System.out.println("WAIT :: " + w));
            if (!result) {
                throw new Exception("Failed to insert the document into database");
            }
        } else {
            System.out.println("FALSE");
        }
    }

    public boolean dropDocumentFromWaitList(String document) {
        return mongoDbHandler.dropDocument(databaseName, collectionName, document);
    }

    public List<String> getWaitList() {
        return mongoDbHandler.getAllDocuments(databaseName, collectionName);
    }

    private String addPropertiesToEvent(String event, RulesObject rulesObject) {
        String time;
        Date date = new Date();
        String idRule = rulesObject.getIdRule();
        JsonNode id = jmesPathInterface.runRuleOnEvent(idRule, event);
        String condition = "{Event:" + event + "}";
        List<String> documents = mongoDbHandler.find(databaseName, collectionName, condition);
        if (documents.isEmpty()) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            time = dateFormat.format(date);
            try {
                date = dateFormat.parse(time);
            } catch (ParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        JSONObject document = new JSONObject()
            .put("_id", id.textValue())
            .put("Time", date)
            .put("Event", event);
        mongoDbHandler.createTTLIndex(databaseName, collectionName, "Time", ttlValue);
        return document.toString();
    }

}