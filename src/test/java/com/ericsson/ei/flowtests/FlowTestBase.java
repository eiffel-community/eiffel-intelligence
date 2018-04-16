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
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.waitlist.WaitListStorageHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

public abstract class FlowTestBase extends AbstractTestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowTestBase.class);

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    private ObjectHandler objectHandler;

    @Autowired
    private RulesHandler rulesHandler;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private WaitListStorageHandler waitlist;

    @Value("${database.name}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String event_map;

    private static ObjectMapper objectMapper = new ObjectMapper();

    // final FlowTestConfigs configs = new FlowTestConfigs();

    private static HashMap<String, FlowTestConfigs> configsMap = new HashMap<String, FlowTestConfigs>();

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        System.setProperty("flow.test", "true");
        // System.setProperty("eiffel.intelligence.processedEventsCount", "0");
        // System.setProperty("eiffel.intelligence.waitListEventsCount", "0");
        createFlowTestConfigs();
        getFlowTestConfigs().init();
    }

    // @BeforeClass
    // public static void setUp() throws Exception {
    //
    // }

    @PostConstruct
    public void init() throws Exception {
        mongoDBHandler.setMongoClient(getFlowTestConfigs().getMongoClient());
        waitlist.setMongoDbHandler(mongoDBHandler);

    }

    protected FlowTestConfigs getFlowTestConfigs() {
        return configsMap.get(getClasName());
        // return configs;
    }

    private void createFlowTestConfigs() {
        FlowTestConfigs newConfigs = new FlowTestConfigs();
        String className = getClasName();
        configsMap.put(className, newConfigs);
    }

    private String getClasName() {
        // Class c = MethodHandles.lookup().lookupClass();
        // return c.getName();
        return this.getClass().getName();
    }

    private void cleanFlowTestConfigs() {
        configsMap.remove(getClasName());
    }

    @Test
    public void flowTest() {
        try {
            String queueName = rmqHandler.getQueueName();
            Channel channel = getFlowTestConfigs().getConn().createChannel();
            String exchangeName = "ei-poc-4";
            getFlowTestConfigs().createExchange(exchangeName, queueName);

            rulesHandler.setRulePath(getRulesFilePath());

            List<String> eventNames = getEventNamesToSend();
            JsonNode parsedJSON = getJSONFromFile(getEventsFilePath());
            int eventsCount = eventNames.size();
            for (String eventName : eventNames) {
                JsonNode eventJson = parsedJSON.get(eventName);
                String event = eventJson.toString();
                channel.basicPublish(exchangeName, queueName, null, event.getBytes());
            }

            // wait for all events to be processed
            waitForEventsToBeProcessed(eventsCount);
            checkResult(getCheckData());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        getFlowTestConfigs().tearDown();
        cleanFlowTestConfigs();
    }

    /**
     * @return path to file with rules, that is used in flow test
     */
    abstract String getRulesFilePath();

    /**
     * @return path to file, with events, that is used in flow test
     */
    abstract String getEventsFilePath();

    /**
     * @return list of event names, that will be used in flow test
     */
    abstract List<String> getEventNamesToSend();

    /**
     * @return map, where key - _id of expected aggregated object value -
     *         expected aggregated object
     */
    abstract Map<String, JsonNode> getCheckData() throws IOException;

    JsonNode getJSONFromFile(String filePath) throws IOException {
        String expectedDocument = FileUtils.readFileToString(new File(filePath), "UTF-8");
        return objectMapper.readTree(expectedDocument);
    }

    // count documents that were processed
    private long countProcessedEvents(String database, String collection) {
        MongoDatabase db = getFlowTestConfigs().getMongoClient().getDatabase(database);
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

    private void checkResult(final Map<String, JsonNode> checkData) {
        checkData.forEach((id, expectedJSON) -> {
            try {
                String document = objectHandler.findObjectById(id);
                JsonNode actualJSON = objectMapper.readTree(document);
                LOGGER.info("Complete aggregated object: " + actualJSON);
                JSONAssert.assertEquals(expectedJSON.toString(), actualJSON.toString(), false);
            } catch (IOException | JSONException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }
}
