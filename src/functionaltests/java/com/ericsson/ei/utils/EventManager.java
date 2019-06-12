package com.ericsson.ei.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.RmqHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EventManager {

    @Autowired
    private RmqHandler rmqHandler;

    protected List<String> getEventNamesToSend() {
        return new ArrayList<>();
    }

    /**
     * Send Eiffel Events to the waitlist queue.
     *
     * @param eiffelEventsJsonPath
     *            JSON file containing Eiffel Events
     * @param eventNames
     *            list of event names
     * @throws IOException
     */
    public void sendEiffelEvents(String eiffelEventsJsonPath, List<String> eventNames) throws IOException {

        JsonNode parsedJSON = getJSONFromFile(eiffelEventsJsonPath);
        for (String eventName : eventNames) {
            JsonNode eventJson = parsedJSON.get(eventName);
            rmqHandler.publishObjectToWaitlistQueue(eventJson.toString());
        }
    }

    /**
     * Send Eiffel Event to the waitlist queue. Takes a Json String containing a
     * single event.

     * 
     * @param eiffelEventJson
     */
    public void sendEiffelEvent(String eiffelEventJson) {
        rmqHandler.publishObjectToWaitlistQueue(eiffelEventJson);
    }

    /**
     * Get list of IDs.
     *
     * @param eiffelEventsJsonPath
     *            JSON file containing Eiffel Events
     * @param eventNames
     *            list of event names
     * @return list of event IDs
     * @throws IOException
     */
    public List<String> getEventsIdList(String eiffelEventsJsonPath, List<String> eventNames) throws IOException {
        List<String> eventsIdList = new ArrayList<>();
        JsonNode parsedJSON = getJSONFromFile(eiffelEventsJsonPath);
        for (String eventName : eventNames) {
            JsonNode eventJson = parsedJSON.get(eventName);
            eventsIdList.add(eventJson.get("meta").get("id").toString().replaceAll("\"", ""));
        }
        return eventsIdList;
    }

    /**
     * Converts a JSON file into a tree model.

     *
     * @param filePath
     *            path to JSON file
     * @return JsonNode tree model
     * @throws IOException
     */
    public JsonNode getJSONFromFile(String filePath) throws IOException {
        return getJSONFromString(FileUtils.readFileToString(new File(filePath), "UTF-8"));
    }

    /**
     * Converts a JSON string into a tree model.
     * 
     * @param document
     *            string of json
     * @return JsonNode tree model
     * @throws IOException
     */
    public JsonNode getJSONFromString(String document) throws IOException {
        return new ObjectMapper().readTree(document);

    }

}
