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
package com.ericsson.ei.handlers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author evasiba
 * Class for handling event to object map.
 * The map has the event id as key and the value is a list
 * with all the ids of objects that an event has contributed to.
 *
 */
@Component
public class EventToObjectMapHandler {

    static Logger log = (Logger) LoggerFactory.getLogger(ExtractionHandler.class);

    @Value("${event_object_map.collection.name}") private String collectionName;
    @Value("${database.name}") private String databaseName;

    private final String listPropertyName = "objects";

    @Autowired
    MongoDBHandler mongodbhandler;

    @Autowired
    JmesPathInterface jmesPathInterface;

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setMongodbhandler(MongoDBHandler mongodbhandler) {
        this.mongodbhandler = mongodbhandler;
    }

    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmesPathInterface = jmesPathInterface;
    }

    public ArrayList<String> getObjectsForEvent(RulesObject rulesObject, String event) {
        String eventId = getEventId(rulesObject, event);
        return getEventToObjectList(eventId);
    }

    public ArrayList<String> getObjectsForEventId(String eventId) {
        return getEventToObjectList(eventId);
    }

    public void updateEventToObjectMapInMemoryDB(RulesObject rulesObject, String event, String objectId) {
        String eventId = getEventId(rulesObject, event);
        String condition = "{\"_id\" : \"" + eventId + "\"}";
        ArrayList<String> list =  getEventToObjectList(eventId);
        boolean firstTime = list.isEmpty();
        list = updateList(list, eventId, objectId);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode entry = null;

        try {
            entry = new ObjectMapper().readValue(condition, JsonNode.class);
            ArrayNode jsonNode = mapper.convertValue(list, ArrayNode.class);
            ((ObjectNode) entry).set(listPropertyName, mapper.readTree(jsonNode.toString()));
            String mapStr = entry.toString();
            log.debug("MongoDbHandler Insert/Update Event: " + mapStr +
            		"\nto database: " + databaseName + " and to Collection: " + collectionName);
            if (firstTime) {
                mongodbhandler.insertDocument(databaseName, collectionName, mapStr);
            } else {
                mongodbhandler.updateDocument(databaseName, collectionName, condition, mapStr);
            }
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    public String getEventId(RulesObject rulesObject, String event) {
        String idRule = rulesObject.getIdRule();
        JsonNode eventIdJson = jmesPathInterface.runRuleOnEvent(idRule, event);
        return eventIdJson.textValue();
    }

    public ArrayList<String> updateList(ArrayList<String> list, String eventId, String objectId) {
        list.add(objectId);
        return list;
    }

    public ArrayList<String> getEventToObjectList(String eventId) {
        ArrayList<String> list = new ArrayList<String>();
        String condition = "{\"_id\" : \"" + eventId + "\"}";
        ArrayList<String> documents = mongodbhandler.find(databaseName, collectionName, condition);
        if (!documents.isEmpty()) {
            String mapStr = documents.get(0);
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode document = mapper.readValue(mapStr, JsonNode.class);
                JsonNode value = document.get(listPropertyName);
                list = new ObjectMapper().readValue(value.traverse(), new TypeReference<ArrayList<String>>(){});
            } catch (Exception e) {
                log.info(e.getMessage(),e);
            }
        }
        return list;
    }
    
    /**
     * The method is responsible for the delete the EventObjectMap by using the suffix template Name
     * 
     * @param templateName
     * @return boolean
     */
    public boolean deleteEventObjectMap(String templateName) {
        String condition = "{\"objects\": { \"$in\" : [/.*" + templateName + "/]} }";
        log.info("The Json condition for delete aggregated object is : " + condition);
        return mongodbhandler.dropDocument(databaseName, collectionName, condition);
    }


}
