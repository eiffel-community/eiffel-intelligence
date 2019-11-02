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
import java.net.URISyntaxException;
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
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public abstract class IntegrationTestBase extends AbstractTestExecutionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestBase.class);
    private static final String EIFFEL_INTELLIGENCE_DATABASE_NAME = "eiffel_intelligence";

    protected RabbitTemplate rabbitTemplate;
    protected static final String MAILHOG_DATABASE_NAME = "mailhog";
    @Autowired
    protected MongoDBHandler mongoDBHandler;
    @Value("${ei.host:localhost}")
    protected String eiHost;
    @LocalServerPort
    protected int port;

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

    @Value("${aggregated.collection.name}")
    private String aggregatedCollectionName;
    @Value("${waitlist.collection.name}")
    private String waitlistCollectionName;
    @Value("${subscription.collection.name}")
    private String subscriptionCollectionName;
    @Value("${event_object_map.collection.name}")
    private String eventObjectMapCollectionName;
    @Value("${subscription.collection.repeatFlagHandlerName}")
    private String subscriptionCollectionRepatFlagHandlerName;
    @Value("${failed.notification.collection-name}")
    private String failedNotificationCollectionName;
    @Value("${sessions.collection.name}")
    private String sessionsCollectionName;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        cleanDatabases();
        rabbitTemplate = createRabbitMqTemplate();
    }

    private void cleanDatabases() {
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, aggregatedCollectionName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, waitlistCollectionName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, subscriptionCollectionName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, eventObjectMapCollectionName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, subscriptionCollectionRepatFlagHandlerName);
        mongoDBHandler.dropCollection(EIFFEL_INTELLIGENCE_DATABASE_NAME, failedNotificationCollectionName);
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
     * @throws Exception
     */
    protected void sendEventsAndConfirm() throws Exception {
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
    protected abstract List<String> getEventNamesToSend() throws IOException;

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
     *
     * @param eventsCount - An int which indicated how many events that should be
     *                    processed.
     * @return
     * @throws InterruptedException
     */
    protected void waitForEventsToBeProcessed(int eventsCount) throws InterruptedException {
        // wait for all events to be processed
        long stopTime = System.currentTimeMillis() + 60000;
        long processedEvents = 0;
        while (processedEvents < eventsCount && stopTime > System.currentTimeMillis()) {
            processedEvents = countProcessedEvents(database, event_map);
            LOGGER.debug("Have gotten: " + processedEvents + " out of: " + eventsCount);
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }

    /**
     * Counts documents that were processed
     *
     * @param database   - A string with the database to use
     * @param collection - A string with the collection to use
     * @return amount of processed events
     */
    private long countProcessedEvents(String database, String collection) {
        MongoClient mongoClient = null;
        mongoClient = mongoDBHandler.getMongoClient();
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection table = db.getCollection(collection);
        return table.count();
    }

    /**
     * Retrieves the result from EI and checks if it equals the expected data
     *
     * @param expectedData - A Map<String, JsonNode> which contains the expected
     *                     data
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    private void checkResult(final Map<String, JsonNode> expectedData)
            throws IOException, URISyntaxException, InterruptedException {
        Iterator iterator = expectedData.entrySet().iterator();

        JsonNode expectedJSON = objectMapper.createObjectNode();
        JsonNode actualJSON = objectMapper.createObjectNode();

        boolean foundMatch = false;
        while (!foundMatch && iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            String id = (String) pair.getKey();
            expectedJSON = (JsonNode) pair.getValue();

            long maxWaitTime = 300000;
            long stopTime = System.currentTimeMillis() + maxWaitTime;
            while (!foundMatch && stopTime > System.currentTimeMillis()) {
                actualJSON = queryAggregatedObject(id);

                /*
                 * This is a workaround for expectedJSON.equals(acutalJSON) as that does not
                 * work with strict equalization
                 */
                try {
                    JSONAssert.assertEquals(expectedJSON.toString(), actualJSON.toString(), false);
                    foundMatch = true;
                } catch (AssertionError e) {
                    TimeUnit.SECONDS.sleep(1);
                }
            }
        }

        JSONAssert.assertEquals(expectedJSON.toString(), actualJSON.toString(), false);
    }

    /**
     * Retrieves the aggregatedObject from EI by querying
     *
     * @param id - A string which contains the id used in the query
     * @return the responseEntity within the body.
     * @throws URISyntaxException
     * @throws IOException
     */
    private JsonNode queryAggregatedObject(String id) throws URISyntaxException, IOException {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);
        String endpoint = "/aggregated-objects";

        httpRequest.setHost(eiHost)
                   .setPort(port)
                   .addHeader("Content-type", "application/json")
                   .addParam("ID", id)
                   .setEndpoint(endpoint);

        ResponseEntity<String> response = httpRequest.performRequest();
        JsonNode body = objectMapper.readTree(response.getBody());
        JsonNode responseEntity = body.get("queryResponseEntity");

        return responseEntity;
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
        return rabbitTemplate;
    }
}
