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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.After;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.handlers.RmqHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.waitlist.WaitListStorageHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Channel;

/**
 * @author evasiba
 *
 */
public abstract class FlowTestBase extends AbstractTestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowTestBase.class);

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    public ObjectHandler objectHandler;

    @Autowired
    private RulesHandler rulesHandler;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private WaitListStorageHandler waitlist;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String event_map;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static HashMap<String, FlowTestConfigs> configsMap = new HashMap<String, FlowTestConfigs>();

    @Value("${systemTest:false}")
    protected boolean systemTest;

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        String systemTestProperty = System.getProperty("systemTest");
        Boolean systemTestValue = Boolean.parseBoolean(systemTestProperty);
        if (!systemTestValue) {
            System.setProperty("flow.test", "true");
            createFlowTestConfigs();
            getFlowTestConfigs().init();
        }
    }

    @PostConstruct
    public void init() {
        LOGGER.info("System Test is: " + systemTest);
        if (!systemTest) {
            mongoDBHandler.setMongoClient(getFlowTestConfigs().getMongoClient());
            waitlist.setMongoDbHandler(mongoDBHandler);
        }
    }

    @After
    public void teardown() {
        if (!systemTest) {
            mongoDBHandler.setMongoClient(null);
            getFlowTestConfigs().tearDown();
            cleanFlowTestConfigs();
        }
    }

    protected FlowTestConfigs getFlowTestConfigs() {
        return configsMap.get(getClasName());
    }

    private void createFlowTestConfigs() {
        FlowTestConfigs newConfigs = new FlowTestConfigs();
        String className = getClasName();
        configsMap.put(className, newConfigs);
    }

    private String getClasName() {
        return this.getClass().getName();
    }

    private void cleanFlowTestConfigs() {
        configsMap.remove(getClasName());
    }

    // setFirstEventWaitTime: variable to set the wait time after publishing the
    // first event. So any thread looking for the events don't do it before
    // actually
    // populating events in the database
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

    @Test
    public void flowTest() throws InterruptedException {
        try {
            String queueName = rmqHandler.getQueueName();
            Channel channel = null;
            if (!systemTest) {
                channel = getFlowTestConfigs().getConn().createChannel();
            } else {
                channel = connectionFactory.createConnection().createChannel(true);
            }
            String exchangeName = "ei-poc-4";
            if (!systemTest) {
                getFlowTestConfigs().createExchange(exchangeName, queueName);
            }

            rulesHandler.setRulePath(getRulesFilePath());

            List<String> eventNames = getEventNamesToSend();
            JsonNode parsedJSON = getJSONFromFile(getEventsFilePath());
            int eventsCount = eventNames.size() + extraEventsCount();

            boolean alreadyExecuted = false;
            for (String eventName : eventNames) {
                JsonNode eventJson = parsedJSON.get(eventName);
                String event = eventJson.toString();
                if (!systemTest) {
                    channel.basicPublish(exchangeName, queueName, null, event.getBytes());
                } else {
                    rmqHandler.publishObjectToWaitlistQueue(event);
                }
                if (!alreadyExecuted) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(firstEventWaitTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        LOGGER.error(e.getMessage(), e);
                    }
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
     * @return map, where key - _id of expected aggregated object value - expected
     *         aggregated object
     *
     */
    abstract Map<String, JsonNode> getCheckData() throws IOException;

    JsonNode getJSONFromFile(String filePath) throws IOException {
        String expectedDocument = FileUtils.readFileToString(new File(filePath), "UTF-8");
        return objectMapper.readTree(expectedDocument);
    }

    // count documents that were processed
    private long countProcessedEvents(String database, String collection) {
        MongoClient mongoClient = null;
        if (!systemTest) {
            mongoClient = getFlowTestConfigs().getMongoClient();
        } else {
            mongoClient = mongoDBHandler.getMongoClient();
        }
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection table = db.getCollection(collection);
        return table.count();
    }

    protected void waitForEventsToBeProcessed(int eventsCount) throws InterruptedException {
        // wait for all events to be processed
        long stopTime = System.currentTimeMillis() + 30000;
        long processedEvents = 0;
        while (processedEvents < eventsCount && stopTime > System.currentTimeMillis()) {
            processedEvents = countProcessedEvents(database, event_map);
            LOGGER.info("Have gotten: " + processedEvents + " out of: " + eventsCount);
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        TimeUnit.MILLISECONDS.sleep(5000);
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