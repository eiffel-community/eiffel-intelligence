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

import com.ericsson.ei.exception.AbortExecutionException;
import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongo.*;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;

import lombok.Getter;
import lombok.Setter;

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

import javax.annotation.PostConstruct;

@Component
public class WaitListStorageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitListStorageHandler.class);

    @Getter
    @Value("${waitlist.collection.name}")
    private String waitlistCollectionName;

    @Getter
    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Getter
    @Value("${waitlist.collection.ttl}")
    private int waitlistTtl;

    @Setter
    @Autowired
    private MongoDBHandler mongoDbHandler;

    @Setter
    @Autowired
    private JmesPathInterface jmesPathInterface;

    @PostConstruct
    public void init() throws AbortExecutionException {
        try {
            if (waitlistTtl > 0) {
                mongoDbHandler.createTTLIndex(databaseName, waitlistCollectionName, MongoConstants.TIME, waitlistTtl);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create an index for {} due to: {}", waitlistCollectionName, e);
        }
    }
    
    /**
     * Adds event to the waitlist database if it does not already exists.
     *
     * @param event The event that will be added to database
     * @param rulesObject Rules for extracting a unique identifier from an event object to be used as document id
     * @throws MongoDBConnectionException
     */
    public void addEventToWaitListIfNotExisting(String event, RulesObject rulesObject)
            throws MongoDBConnectionException {
        try {
            JsonNode id = extractIdFromEventUsingRules(event, rulesObject);
            String foundEvent = findEventInWaitList(id.textValue());
            if (foundEvent.isEmpty()) {
                Date date = createCurrentTimeStamp();
                BasicDBObject document = createWaitListDocument(event, id, date);
                mongoDbHandler.insertDocument(databaseName, waitlistCollectionName, document.toString());
            }
        } catch (MongoWriteException e) {
            LOGGER.debug("Failed to insert event into waitlist.", e);
        }
    }

    private String findEventInWaitList(String id) {
        final MongoCondition condition = MongoCondition.idCondition(id);
        List<String> foundEventsInWaitList = mongoDbHandler.find(databaseName,
                waitlistCollectionName, condition);
        if (foundEventsInWaitList.isEmpty()) {
            return "";
        }
        String foundEvent = foundEventsInWaitList.get(0);
        return foundEvent;
    }

    public boolean dropDocumentFromWaitList(String document) {
       MongoQuery query = new MongoStringQuery(document);
        return mongoDbHandler.dropDocument(databaseName, waitlistCollectionName, query);
    }

    public List<String> getWaitList() {
        return mongoDbHandler.getAllDocuments(databaseName, waitlistCollectionName);
    }

    private BasicDBObject createWaitListDocument(String event, JsonNode id, Date date)
            throws MongoDBConnectionException {
        BasicDBObject document = new BasicDBObject();
        document.put(MongoConstants.ID, id.textValue());
        document.put(MongoConstants.TIME, date);
        document.put(MongoConstants.EVENT, event);
        return document;
    }

    private Date createCurrentTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String time = dateFormat.format(date);
        try {
            date = dateFormat.parse(time);
        } catch (ParseException e) {
            LOGGER.error("Failed to parse time from date object.", e);
        }
        return date;
    }

    private JsonNode extractIdFromEventUsingRules(String event, RulesObject rulesObject) {
        String idRule = rulesObject.getIdRule();
        JsonNode id = jmesPathInterface.runRuleOnEvent(idRule, event);
        return id;
    }

}