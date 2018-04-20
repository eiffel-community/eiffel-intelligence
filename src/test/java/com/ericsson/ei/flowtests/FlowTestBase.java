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
package com.ericsson.ei.flowtests;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.rules.RulesHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class FlowTestBase extends FlowTestConfigs {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowTest.class);

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    private ObjectHandler objectHandler;

    @Autowired
    private RulesHandler rulesHandler;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String event_map;

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static JsonNode parsedJson;

    @Test
    public void flowTest() {
        try {
            String queueName = rmqHandler.getQueueName();
            Channel channel = conn.createChannel();
            String exchangeName = "ei-poc-4";
            createExchange(exchangeName, queueName);

            rulesHandler.setRulePath(setRulesFilePath());

            List<String> eventNames = setEventNamesToSend();
            testParametersChange();
            int eventsCount = eventNames.size();
            for (String eventName : eventNames) {
                JsonNode eventJson = parsedJson.get(eventName);
                String event = eventJson.toString();
                channel.basicPublish(exchangeName, queueName, null, event.getBytes());
            }

            // wait for all events to be processed
            waitForEventsToBeProcessed(eventsCount);
            checkResult(setCheckInfo());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * @return path to file with rules, that is used in flow test
     */
    abstract String setRulesFilePath();

    /**
     * @return path to file, with events, that is used in flow test
     */
    abstract String setEventsFilePath();

    /**
     * @return list of event names, that will be used in flow test
     */
    abstract List<String> setEventNamesToSend();

    /**
     * @return map, where
     *          key - _id of expected aggregated object
     *          value - path to file with expected aggregated object
     */
    abstract Map<String, String> setCheckInfo();

    private void testParametersChange() throws IOException {
        String jsonFileContent = FileUtils.readFileToString(new File(setEventsFilePath()), "UTF-8");
        parsedJson = objectMapper.readTree(jsonFileContent);
    }

    // count documents that were processed
    private long countProcessedEvents(String database, String collection) {
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection table = db.getCollection(collection);
        return table.count();
    }

    private void waitForEventsToBeProcessed(int eventsCount) {
        // wait for all events to be processed
        long processedEvents = 0;
        while (processedEvents < eventsCount) {
            processedEvents = countProcessedEvents(database, event_map);
            LOGGER.info("Have gotten: " + processedEvents + " out of: " + eventsCount);
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void checkResult(final Map<String, String> inputFiles) {
        inputFiles.forEach((id, filePath) -> {
            try {
                String document = objectHandler.findObjectById(id);
                String expectedDocument = FileUtils.readFileToString(new File(filePath), "UTF-8");
                JsonNode expectedJson = objectMapper.readTree(expectedDocument);
                JsonNode actualJson = objectMapper.readTree(document);
                LOGGER.info("Complete aggregated object: " + actualJson);
                JSONAssert.assertEquals(expectedJson.toString(), actualJson.toString(), false);
            } catch (IOException | JSONException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }
}
