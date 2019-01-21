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
package util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.rules.RulesHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public abstract class IntegrationTestBase extends AbstractTestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestBase.class);
    protected static final String MAILHOG_DATABASE_NAME = "mailhog";
    private static final String EIFFEL_DATABASE_NAME = "eiffel";
    private static final String EIFFEL_INTELLIGENCE_DATABASE_NAME = "eiffel_intelligence";

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    public ObjectHandler objectHandler;

    @Autowired
    private RulesHandler rulesHandler;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String event_map;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        cleanDatabases();
    }

    private void cleanDatabases() {
        mongoDBHandler.dropDatabase(EIFFEL_DATABASE_NAME);
        mongoDBHandler.dropDatabase(EIFFEL_INTELLIGENCE_DATABASE_NAME);
        mongoDBHandler.dropDatabase(MAILHOG_DATABASE_NAME);
    }

    /*
     * setFirstEventWaitTime: variable to set the wait time after publishing the
     * first event. So any thread looking for the events don't do it before actually
     * populating events in the database
     */
    private int firstEventWaitTime = 0;

    public void setFirstEventWaitTime(int value) {
        firstEventWaitTime = value;
    }

    /**
     * Override this if you have more events that will be registered to event to
     * object map but it is not visible in the test. For example from upstream or
     * downstream from event repository
     *
     * @return
     */
    protected int extraEventsCount() {
        return 0;
    }

    protected void sendEventsAndConfirm() throws InterruptedException {
        try {
            rulesHandler.setRulePath(getRulesFilePath());

            List<String> eventNames = getEventNamesToSend();
            JsonNode parsedJSON = getJSONFromFile(getEventsFilePath());
            int eventsCount = eventNames.size() + extraEventsCount();

            boolean alreadyExecuted = false;
            for (String eventName : eventNames) {
                JsonNode eventJson = parsedJSON.get(eventName);
                String event = eventJson.toString();

                rmqHandler.publishObjectToWaitlistQueue(event);
                if (!alreadyExecuted) {
                    TimeUnit.MILLISECONDS.sleep(firstEventWaitTime);
                    alreadyExecuted = true;
                }
            }

            // wait for all events to be processed
            waitForEventsToBeProcessed(eventsCount);
            checkResult(getCheckData());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * @return path to file with rules, that is used in flow test
     */
    protected abstract String getRulesFilePath();

    /**
     * @return path to file, with events, that is used in flow test
     */
    protected abstract String getEventsFilePath();

    /**
     * @return list of event names, that will be used in flow test
     */
    protected abstract List<String> getEventNamesToSend();

    /**
     * @return map, where key - _id of expected aggregated object value - expected
     *         aggregated object
     *
     */
    protected abstract Map<String, JsonNode> getCheckData() throws IOException;

    protected JsonNode getJSONFromFile(String filePath) throws IOException {
        String expectedDocument = FileUtils.readFileToString(new File(filePath), "UTF-8");
        return objectMapper.readTree(expectedDocument);
    }

    // count documents that were processed
    private long countProcessedEvents(String database, String collection) {
        MongoClient mongoClient = null;
        mongoClient = mongoDBHandler.getMongoClient();
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection table = db.getCollection(collection);
        return table.count();
    }

    protected void waitForEventsToBeProcessed(int eventsCount) throws InterruptedException {
        // wait for all events to be processed
        long processedEvents = 0;
        while (processedEvents < eventsCount) {
            processedEvents = countProcessedEvents(database, event_map);
            LOGGER.info("Have gotten: " + processedEvents + " out of: " + eventsCount);
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }

    private void checkResult(final Map<String, JsonNode> checkData) throws IOException {
        Iterator iterator = checkData.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            String id = (String) pair.getKey();
            JsonNode expectedJSON = (JsonNode) pair.getValue();

            String document = objectHandler.findObjectById(id);
            JsonNode actualJSON = objectMapper.readTree(document);
            LOGGER.info("Complete aggregated object: " + actualJSON);
            JSONAssert.assertEquals(expectedJSON.toString(), actualJSON.toString(), false);
        }
    }
}