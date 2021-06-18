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
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(EventToObjectMapHandler.class);

    @Value("${event_object_map.collection.name}") private String collectionName;
        
    @Value("${spring.data.mongodb.database}") private String databaseName;

    private final String listPropertyName = "objects";
    
    private boolean isTTLCreated;

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

    /**
     * To check and save the eventIds to the objectId in the mapped database.
     * @param rulesObject
     * @param event
     * @param objectId aggregated event object Id
     */
    public void updateEventToObjectMapInMemoryDB(RulesObject rulesObject, String event, String objectId, int ttlValue) {
        String eventId = getEventId(rulesObject, event);
        String condition = "{\"_id\" : \"" + objectId + "\"}";

        LOGGER.debug("Checking document exists in the collection with condition : {}\n EventId : {}", condition, eventId);
        boolean docExists = mongodbhandler.checkDocumentExists(databaseName, collectionName, condition);
        try {
        	if (!docExists) {
        		ArrayList<String> list =  new ArrayList<String>();
        		list.add(eventId);
        		final ObjectMapper mapper = new ObjectMapper();
    			JsonNode entry = new ObjectMapper().readValue(condition, JsonNode.class);
        		ArrayNode jsonNode = mapper.convertValue(list, ArrayNode.class);
        		((ObjectNode) entry).set(listPropertyName, mapper.readTree(jsonNode.toString()));        		
                final String mapStr = entry.toString(); 
            	LOGGER.debug("MongoDbHandler Insert/Update Event: {}\nto database: {} and to Collection: {}", mapStr, databaseName, collectionName);	
                
                Document document = Document.parse(mapStr);
                document.append("Time", DateUtils.getDate());
            	
            	if(ttlValue > 0 && !isTTLCreated) {                 
            		mongodbhandler.createTTLIndex(databaseName, collectionName, "Time", ttlValue);
            		isTTLCreated = true;
            	}
            	mongodbhandler.insertDocumentObject(databaseName, collectionName, document);
            } else {
                mongodbhandler.updateDocumentAddToSet(databaseName, collectionName, condition, eventId);
            }
        } catch (Exception e) {
            isTTLCreated = false;
            LOGGER.error("Failed to update event object list.", e);
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
                LOGGER.info("Failed to deserialize event object list.", e);
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
        LOGGER.info("The JSON condition for deleting aggregated object is : {}", condition);
        return mongodbhandler.dropDocument(databaseName, collectionName, condition);
    }

    public boolean isEventInEventObjectMap(String eventId) {
        String condition = "{\"objects\": { \"$in\" : [\"" + eventId + "\"]} }";
        List<String> documents = mongodbhandler.find(databaseName, collectionName, condition);
        return !documents.isEmpty();
    }
}
