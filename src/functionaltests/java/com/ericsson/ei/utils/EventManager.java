package com.ericsson.ei.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.rmqhandler.RmqHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

@Component
public class EventManager {

    @Autowired
    private RmqHandler rmqHandler;

    protected List<String> getEventNamesToSend() {
        return new ArrayList<>();
    }

    /**
     * Send Eiffel Events to the waitlist queue. Takes a path to a JSON file
     * containing events and uses getEventNamesToSend to get specific events from
     * that file. getEventNamesToSend needs to be overridden.
     *
     * @param eiffelEventsJsonPath
     *            JSON file containing Eiffel Events
     * @return list of eiffel event IDs
     * @throws InterruptedException
     * @throws IOException
     */
    public void sendEiffelEvents(String eiffelEventsJsonPath, List<String> eventNamesToSend) throws IOException {
        List<String> eventNames = eventNamesToSend;
        JsonNode parsedJSON = getJSONFromFile(eiffelEventsJsonPath);
        for (String eventName : eventNames) {
            JsonNode eventJson = parsedJSON.get(eventName);
            rmqHandler.publishObjectToWaitlistQueue(eventJson.toString());
        }
    }
    
    /**
     * Send Eiffel Events to the waitlist queue. Takes a Json String containing
     * a single event.
     * 
     * @param eiffelEventJson
     */
    public void sendEiffelEvent(String eiffelEventJson) {
        rmqHandler.publishObjectToWaitlistQueue(eiffelEventJson);
    }
    
    public List<String> getEventsIdList(String eiffelEventsJsonPath, List<String> eventNamesToSend) throws IOException {
        List<String> eventsIdList = new ArrayList<>();;
        List<String> eventNames = eventNamesToSend;
        JsonNode parsedJSON = getJSONFromFile(eiffelEventsJsonPath);
        for (String eventName : eventNames) {
            JsonNode eventJson = parsedJSON.get(eventName);
            eventsIdList.add(eventJson.get("meta").get("id").toString().replaceAll("\"", ""));
        }
        return eventsIdList;
    }

    /**
     * Converts a JSON string into a tree model.
     *
     * @param filePath
     *            path to JSON file
     * @return JsonNode tree model
     * @throws IOException
     */
    public JsonNode getJSONFromFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String expectedDocument = FileUtils.readFileToString(new File(filePath), "UTF-8");
        return objectMapper.readTree(expectedDocument);
    }

}
