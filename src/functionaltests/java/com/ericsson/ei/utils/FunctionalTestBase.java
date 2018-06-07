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
package com.ericsson.ei.utils;

import com.ericsson.ei.App;
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
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author evasiba
 */
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, FunctionalTestBase.class})
public class FunctionalTestBase extends AbstractTestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionalTestBase.class);

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

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${event_object_map.collection.name}")
    private String event_map;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static Map<String, TestConfigs> configsMap = new HashMap<>();

    @Setter
    private int firstEventWaitTime = 0;

    public int getMongoDbPort() {
        return mongoDBHandler.getPort();
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        createTestConfigs();
    }

    @PostConstruct
    public void init() throws Exception {
        mongoDBHandler.setMongoClient(getTestConfigs().getMongoClient());
        waitlist.setMongoDbHandler(mongoDBHandler);

    }
    private void createTestConfigs() {
        TestConfigs newConfigs = new TestConfigs();
        String className = getClassName();
        configsMap.put(className, newConfigs);
    }

    private void cleanTestConfigs() {
        configsMap.remove(getClassName());
    }

    private TestConfigs getTestConfigs() {
        return configsMap.get(getClassName());
    }

    private String getClassName() {
        return this.getClass().getName();
    }

    @Test
    public void flowTest() {
        try {
            String queueName = rmqHandler.getQueueName();
            Channel channel = null;
            try {
                channel = getTestConfigs().getConn().createChannel();
            } catch (IOException e) {
                LOGGER.error(configsMap.toString());
            }
            String exchangeName = "ei-poc-4";
            getTestConfigs().createExchange(exchangeName, queueName);
            rulesHandler.setRulePath(getRulesFilePath());
            List<String> eventNames = getEventNamesToSend();
            JsonNode parsedJSON = getJSONFromFile(getEventsFilePath());
            int eventsCount = eventNames.size() + extraEventsCount();
            boolean alreadyExecuted = false;
            for (String eventName : eventNames) {
                JsonNode eventJson = parsedJSON.get(eventName);
                String event = eventJson.toString();
                channel.basicPublish(exchangeName, queueName, null, event.getBytes());
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
        cleanTestConfigs();
    }

    /**
     * Override this if you have more events that will be registered to event to
     * object map but it is not visible in the test. For example form upstream
     * or downstream from event repository
     *
     * @return
     */
    protected int extraEventsCount() {
        return 0;
    }

    /**
     * @return path to file with rules, that is used in test
     */
    protected String getRulesFilePath() {
        return null;
    }

    /**
     * @return path to file, with events, that is used in test
     */
    protected String getEventsFilePath() {
        return null;
    }

    /**
     * @return list of event names, that will be used in test
     */
    protected List<String> getEventNamesToSend() {
        return null;
    }

    /**
     * @return map, where key - _id of expected aggregated object value -
     * expected aggregated object
     */
    protected Map<String, JsonNode> getCheckData() {
        return null;
    }

    JsonNode getJSONFromFile(String filePath) throws IOException {
        String expectedDocument = FileUtils.readFileToString(new File(filePath), "UTF-8");
        return objectMapper.readTree(expectedDocument);
    }

    private long countProcessedEvents(String database, String collection) {
        MongoDatabase db = getTestConfigs().getMongoClient().getDatabase(database);
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

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
       // getTestConfigs().tearDown();
    }
}