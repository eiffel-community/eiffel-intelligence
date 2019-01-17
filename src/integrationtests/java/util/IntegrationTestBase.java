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
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public abstract class IntegrationTestBase extends AbstractTestExecutionListener {

    protected RabbitTemplate rabbitTemplate;
    protected static final String MAILHOG_DATABASE_NAME = "mailhog";
    @Autowired
    protected MongoDBHandler mongoDBHandler;

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestBase.class);
    private static final String EIFFEL_INTELLIGENCE_DATABASE_NAME = "eiffel_intelligence";

    @Autowired
    public ObjectHandler objectHandler;

    @Value("${spring.data.mongodb.database}")
    private String database;
    @Value("${event_object_map.collection.name}")
    private String event_map;
    @Value("${rabbitmq.host}")
    private String rabbitMqHost;
    @Value("${rabbitmq.port}")
    private int rabbitMqPort;
    @Value("${rabbitmq.user}")
    private String rabbitMqUsername;
    @Value("${rabbitmq.password}")
    private String rabbitMqPassword;
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${rabbitmq.binding.key}")
    private String bindingKey;
    @Value("${rabbitmq.consumerName}")
    private String consumerName;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        cleanDatabases();
        rabbitTemplate = createRabbitMqTemplate();
    }

    private void cleanDatabases() {
        String aggregatedCollectionName = System.getProperty("aggregated.collection.name");
        String waitlistCollectionName = System.getProperty("waitlist.collection.name");
        String subscriptionCollectionName = System.getProperty("subscription.collection.name");
        String eventObjectMapCollectionName = System.getProperty("event_object_map.collection.name");
        String subscriptionCollectionRepatFlagHandlerName = System.getProperty("subscription.collection.repeatFlagHandlerName");
        String missedNotificationCollectionName = System.getProperty("missedNotificationCollectionName");
        String sessionsCollectionName = System.getProperty("sessions.collection.name");

        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, aggregatedCollectionName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, waitlistCollectionName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, subscriptionCollectionName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, eventObjectMapCollectionName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, subscriptionCollectionRepatFlagHandlerName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, missedNotificationCollectionName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, sessionsCollectionName);
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

    /**
     * Send events and confirms that all was processed
     *
     * @return
     * @throws InterruptedException
     */
    protected void sendEventsAndConfirm() throws InterruptedException {
        try {
            List<String> eventNames = getEventNamesToSend();
            JsonNode parsedJSON = getJSONFromFile(getEventsFilePath());
            int eventsCount = eventNames.size() + extraEventsCount();

            boolean alreadyExecuted = false;
            for (String eventName : eventNames) {
                JsonNode eventJson = parsedJSON.get(eventName);
                String event = eventJson.toString();

                rabbitTemplate.convertAndSend(event);
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
     * @throws IOException
     */
    protected List<String> getEventNamesToSend() throws IOException {
        ArrayList<String> eventNames = new ArrayList<>();

        URL eventsInput = new File(getEventsFilePath()).toURI().toURL();
        Iterator eventsIterator = objectMapper.readTree(eventsInput).fields();

        while(eventsIterator.hasNext()) {
            Map.Entry pair = (Map.Entry)eventsIterator.next();
            eventNames.add(pair.getKey().toString());
            }

        return eventNames;
    }

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

    /**
     * Wait for certain amount of events to be processed.
     * @param eventsCount - An int which indicated how many events that should be processed.
     * @return
     * @throws InterruptedException
     */
    protected void waitForEventsToBeProcessed(int eventsCount) throws InterruptedException {
        // wait for all events to be processed
        long processedEvents = 0;
        while (processedEvents < eventsCount) {
            processedEvents = countProcessedEvents(database, event_map);
            LOGGER.info("Have gotten: " + processedEvents + " out of: " + eventsCount);
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }

    // count documents that were processed
    private long countProcessedEvents(String database, String collection) {
        MongoClient mongoClient = null;
        mongoClient = mongoDBHandler.getMongoClient();
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection table = db.getCollection(collection);
        return table.count();
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

    private RabbitTemplate createRabbitMqTemplate() {
        CachingConnectionFactory cf = new CachingConnectionFactory();
        cf.setHost(rabbitMqHost);
        cf.setPort(rabbitMqPort);
        cf.setUsername(rabbitMqUsername);
        cf.setPassword(rabbitMqPassword);

        RabbitTemplate rabbitTemplate = new RabbitTemplate(cf);
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setRoutingKey(bindingKey);
        rabbitTemplate.setQueue(consumerName);
        return rabbitTemplate;
    }
}