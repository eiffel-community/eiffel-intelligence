package com.ericsson.ei.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;

/**
 * @author evasiba
 * Class for handling event to object map.
 * The map has the event id as key and the value is a list
 * with all the ids of objects that an event has contributed to.
 *
 */
@Component
public class EventToObjectMapHandler {
    @Value("${event_object_map.collection.name}") private String collectionName;
    @Value("${database.name}") private String databaseName;

    @Autowired
    MongoDBHandler mongodbhandler;

    public ArrayList<String> getEventsForObject(String object) {
        return null;
    }

    public void updateEventToObjectMapInMemoryDB(String event, String object) {

    }

    public Map<String, List<String>> getEventToObjectMap() {
        return null;
    }
}
