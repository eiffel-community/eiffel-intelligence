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
import java.util.ArrayList;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

@Component
public class WaitListStorageHandler {
    static Logger log = (Logger) LoggerFactory.getLogger(WaitListStorageHandler.class);

    @Getter
    @Value("${waitlist.collection.name}")
    private String collectionName;

    @Getter
    @Value("${database.name}")
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
        String input = addProprtiesToEvent(event, rulesObject);
        boolean result=mongoDbHandler.insertDocument(databaseName,collectionName, input);
        if (result == false) {
            throw new Exception("failed to insert the document into database");
        }
        updateTestEventCount(true);
    }

    private String addProprtiesToEvent(String event, RulesObject rulesObject) {
        String time = null;
        Date date=null;
        String idRule = rulesObject.getIdRule();
        JsonNode id = jmesPathInterface.runRuleOnEvent(idRule, event);
        String condition = "{Event:" + JSON.parse(event).toString()+"}";
        ArrayList<String> documents = mongoDbHandler.find(databaseName, collectionName, condition);
        if (documents.size() == 0){
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            date = new Date();
            time = dateFormat.format(date);
            try {
                date=dateFormat.parse(time);
            } catch (ParseException e) {
                log.info(e.getMessage(),e);
            }

        }
        BasicDBObject document = new BasicDBObject();
        document.put("_id", id.textValue());
        document.put("Time",  date);
        document.put("Event", JSON.parse(event));
        mongoDbHandler.createTTLIndex(databaseName, collectionName, "Time",ttlValue);
        return document.toString();
    }

    public ArrayList<String> getWaitList() {
        ArrayList<String> documents = mongoDbHandler.getAllDocuments(databaseName,collectionName);
        return documents;
    }

    public boolean dropDocumentFromWaitList(String document) {
        boolean result = mongoDbHandler.dropDocument(databaseName, collectionName, document);

        if (result) {
            updateTestEventCount(false);
        }

        return result;
    }

    private void updateTestEventCount(boolean increase) {
        if (System.getProperty("flow.test") == "true") {
            String countStr = System.getProperty("eiffel.intelligence.waitListEventsCount");
            int count = Integer.parseInt(countStr);
            if (increase) {
                count++;
            } else {
                count--;
            }
            System.setProperty("eiffel.intelligence.waitListEventsCount", "" + count);
        }
    }
}