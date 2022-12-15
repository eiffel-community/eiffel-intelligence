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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
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

import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.mongo.MongoStringQuery;
import com.ericsson.ei.utils.HttpRequest;
import com.ericsson.ei.utils.HttpRequest.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Setter;

public abstract class IntegrationTestBase extends AbstractTestExecutionListener {
    private static final int SECONDS_1 = 1000;
    private static final int SECONDS_30 = 30000;
    private static final int DEFAULT_DELAY_BETWEEN_SENDING_EVENTS = 350;
    protected static final String MAILHOG_DATABASE_NAME = "mailhog";

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestBase.class);

    protected RabbitTemplate rabbitTemplate;
    @Autowired
    protected MongoDBHandler mongoDBHandler;
    @Value("${ei.host:localhost}")
    protected String eiHost;
    @LocalServerPort
    protected int port;

    @Value("${spring.data.mongodb.database}")
    private String database;
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
    @Value("${rabbitmq.queue.suffix}")
    private String queueSuffix;

    @Value("${aggregations.collection.name}")
    private String aggregatedCollectionName;
    @Value("${waitlist.collection.name}")
    private String waitlistCollectionName;
    @Value("${subscriptions.collection.name}")
    private String subscriptionCollectionName;
    @Value("${event.object.map.collection.name}")
    private String eventObjectMapCollectionName;
    @Value("${subscriptions.repeat.handler.collection.name}")
    private String subscriptionCollectionRepatFlagHandlerName;
    @Value("${failed.notifications.collection.name}")
    private String failedNotificationCollectionName;
    @Value("${sessions.collection.name}")
    private String sessionsCollectionName;
    
    public String aggregatedEventId;
    /*
     * setFirstEventWaitTime: variable to set the wait time after publishing the first event. So any
     * thread looking for the events don't do it before actually populating events in the database
     */
    @Setter
    private int firstEventWaitTime = 0;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        cleanDatabases();
        rabbitTemplate = createRabbitMqTemplate();
    }

    private void cleanDatabases() {
        mongoDBHandler.dropCollection(database, aggregatedCollectionName);
        mongoDBHandler.dropCollection(database, waitlistCollectionName);
        mongoDBHandler.dropCollection(database, subscriptionCollectionName);
        mongoDBHandler.dropCollection(database, eventObjectMapCollectionName);
        mongoDBHandler.dropCollection(database, subscriptionCollectionRepatFlagHandlerName);
        mongoDBHandler.dropCollection(database, failedNotificationCollectionName);
        mongoDBHandler.dropCollection(database, sessionsCollectionName);
    }

    public void setFirstEventWaitTime(int value) {
        firstEventWaitTime = value;
    }

    /**
     * Override this if you have more events that will be registered to event to object map but it
     * is not visible in the test. For example from upstream or downstream from event repository
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
    protected void sendEventsAndConfirm(String aggregatedEvent) throws Exception {
        List<String> eventNames = getEventNamesToSend();
        int eventsCount = eventNames.size();

        JsonNode parsedJSON = getJSONFromFile(getEventsFilePath());
        boolean alreadyExecuted = false;
        for (String eventName : eventNames) {
            JsonNode eventJson = parsedJSON.get(eventName);
            if (eventName.contains(aggregatedEvent)) {
                aggregatedEventId = eventJson.get("meta").get("id").toString();
            }
            String event = eventJson.toString();

            rabbitTemplate.convertAndSend(event);
            if (!alreadyExecuted) {
                TimeUnit.MILLISECONDS.sleep(firstEventWaitTime);
                alreadyExecuted = true;
            }
            /**
             * Without a small delay between the sending of 2 events, one may risk to be lost in an
             * empty void if not received by EI
             */
            TimeUnit.MILLISECONDS.sleep(DEFAULT_DELAY_BETWEEN_SENDING_EVENTS);
        }
        waitForEventsToBeProcessed(eventsCount);
        checkResult(getCheckData());
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
     * @return map, where key - _id of expected aggregated object value - expected aggregated object
     * @throws Exception
     *
     */
    protected abstract Map<String, JsonNode> getCheckData() throws IOException, Exception;

    public static JsonNode getJSONFromFile(String filePath) throws Exception {
        try {
            String expectedDocument = FileUtils.readFileToString(new File(filePath), "UTF-8");
            return objectMapper.readTree(expectedDocument);
        } catch (IOException e) {
            String message = String.format("Failed to load json content from file. Reason: %s",
                    e.getMessage());
            LOGGER.error("", e);
            fail(message);
        }
        return null;
    }

    /**
     * Wait for certain amount of events to be processed.
     * @param eventsCount - An int which indicated how many events that should be processed.
     * @return
     * @throws InterruptedException
     */
    protected void waitForEventsToBeProcessed(int eventsCount) throws InterruptedException {
        // wait for all events to be processed
        long stopTime = System.currentTimeMillis() + SECONDS_30;
        long processedEvents = 0;
        while (processedEvents < eventsCount && stopTime > System.currentTimeMillis()) {
            processedEvents = countProcessedEvents(database, eventObjectMapCollectionName);
            LOGGER.debug("Have gotten: " + processedEvents + " out of: " + eventsCount);
            TimeUnit.MILLISECONDS.sleep(SECONDS_1);
        }

        if (processedEvents < eventsCount) {
            String.format(
                    "EI did not process all sent events. Processed '%s' events out of '%s' sent.",
                    processedEvents, eventsCount);
        }
    }

    /**
     * Counts documents that were processed
     *
     * @param database       - A string with the database to use
     * @param collectionName - A string with the collection to use
     * @return amount of processed events
     */
    private long countProcessedEvents(String database, String collectionName) {
        int count = 0;
        List<String> documents = null;
        String queryString = "{\"_id\": " + aggregatedEventId + "}";
        MongoStringQuery query = new MongoStringQuery(queryString);
        documents = mongoDBHandler.find(database, collectionName, query);
        JSONObject json = new JSONObject(documents.get(0));
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(json.get("objects"));
        String objectId = json.get("objects").toString();
        for (int i = 0; i < objectId.length(); i++) {
            if (objectId.charAt(i) == ',')
                count++;
        }
        count++;
        return count;
    }

    /**
     * StartEvent is Fetched from the given rules
     * @param rules file
     * @return StartEvent type
     */
    public String getStartEvent(String rules) throws Exception {
        String content, type = null;
        content = readRulesFileFromPath(rules);
        JSONArray json = new JSONArray(content);
        if (content.isEmpty()) {
            throw new Exception("Rules content cannot be empty");
        } else {
            for (int i = 0; i < json.length(); i++) {
                JSONObject jsonObj = json.getJSONObject(i);
                if (jsonObj.get("StartEvent").equals("YES")) {
                    type = jsonObj.get("Type").toString();
                    break;
                }
            }
        }
        return type;
    }

    /**
     * Reads the rules from given file path
     * @param rules
     * @return rules content
     * @throws IOException
     */
    private String readRulesFileFromPath(String rules) throws IOException {
        String rulesJsonFileContent = null;
        try (InputStream inputStream = this.getClass().getResourceAsStream(rules)) {
            if (inputStream == null) {
                rulesJsonFileContent = FileUtils.readFileToString(new File(rules),
                        Charset.defaultCharset());
            } else {
                rulesJsonFileContent = IOUtils.toString(inputStream, "UTF-8");
            }
        }
        return rulesJsonFileContent;
    }
    
    /**
     * Retrieves the result from EI and checks if it equals the expected data
     *
     * @param expectedData - A Map<String, JsonNode> which contains the expected data
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

            long stopTime = System.currentTimeMillis() + SECONDS_30;
            while (!foundMatch && stopTime > System.currentTimeMillis()) {
                actualJSON = queryAggregatedObject(id);

                /*
                 * This is a workaround for expectedJSON.equals(acutalJSON) as that does not work
                 * with strict equalization
                 */
                try {
                    JSONAssert.assertEquals(expectedJSON.toString(), actualJSON.toString(), false);
                    foundMatch = true;
                } catch (AssertionError e) {
                    TimeUnit.MILLISECONDS.sleep(SECONDS_1);
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
        String endpoint = "/aggregated-objects/" + id;

        httpRequest.setHost(eiHost)
                   .setPort(port)
                   .addHeader("Content-type", "application/json")
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
