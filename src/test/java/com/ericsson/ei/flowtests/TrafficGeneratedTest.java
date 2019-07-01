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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.handlers.RmqHandler;
import com.ericsson.ei.handlers.UpStreamEventsHandler;
import com.ericsson.ei.rules.RulesHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.Channel;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, TrafficGeneratedTest.class })
@SpringBootTest
public class TrafficGeneratedTest extends FlowTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrafficGeneratedTest.class);
    private static final int EVENT_PACKAGES = 10;
    private static final String RULES_FILE_PATH = "src/test/resources/ArtifactRules.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/test_events_MP.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/test/resources/aggregated_document_MP.json";
    private static final String AGGREGATED_OBJECT_ID = "6acc3c87-75e0-4b6d-88f5-b1a5d4";

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    private ObjectHandler objectHandler;

    @Autowired
    private RulesHandler rulesHandler;

    @Autowired
    private UpStreamEventsHandler upStreamEventsHandler;

    @Mock
    private ERQueryService erQueryService;

    @Value("${spring.data.mongodb.database}")
    private String database;
    @Value("${event_object_map.collection.name}")
    private String event_map;

    @Before
    public void before() throws IOException {
        MockitoAnnotations.initMocks(this);
        upStreamEventsHandler.setEventRepositoryQueryService(erQueryService);

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.set("upstreamLinkObjects", objectMapper.createArrayNode());
        objectNode.set("downstreamLinkObjects", objectMapper.createArrayNode());

        when(erQueryService.getEventStreamDataById(anyString(), any(SearchOption.class), anyInt(), anyInt(),
                anyBoolean())).thenReturn(new ResponseEntity<>(objectNode, HttpStatus.OK));
    }

    @Override
    public void flowTest() {
        try {
            List<String> eventNames = getEventNamesToSend();
            List<String> events = getPreparedEventsToSend(eventNames);
            int eventsCount = eventNames.size() * EVENT_PACKAGES;

            String queueName = rmqHandler.getQueueName();
            String exchange = "ei-poc-4";
            getFlowTestConfigs().createExchange(exchange, queueName);
            Channel channel = getFlowTestConfigs().getConn().createChannel();

            rulesHandler.setRulePath(RULES_FILE_PATH);

            long timeBefore = System.currentTimeMillis();

            for (String event : events) {
                try {
                    channel.basicPublish(exchange, queueName, null, event.getBytes());
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

            waitForEventsToBeProcessed(eventsCount);

            long timeAfter = System.currentTimeMillis();
            long diffTime = timeAfter - timeBefore;

            checkResult();

            String time = "" + diffTime / 60000 + "m " + (diffTime / 1000) % 60 + "s " + diffTime % 1000;
            LOGGER.debug("Number of events, that were sent: " + eventsCount);
            LOGGER.debug("Time of execution: " + time);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * This method loops through every package of events and changes their ids and
     * targets to unique value. Ids of events that are located in the same package
     * are related. Events are sent to RabbitMQ queue. Deterministic traffic is
     * used.
     * 
     * @param eventNames list of events to be sent.
     * @return list of ready to send events.
     */
    private List<String> getPreparedEventsToSend(List<String> eventNames) throws IOException {
        List<String> events = new ArrayList<>();
        String newID;
        String jsonFileContent = FileUtils.readFileToString(new File(EVENTS_FILE_PATH), "UTF-8");
        JsonNode parsedJSON = objectMapper.readTree(jsonFileContent);
        for (int i = 0; i < EVENT_PACKAGES; i++) {
            for (String eventName : eventNames) {
                JsonNode eventJSON = parsedJSON.get(eventName);
                newID = eventJSON.at("/meta/id").textValue().substring(0, 30).concat(String.format("%06d", i));
                ((ObjectNode) eventJSON.path("meta")).put("id", newID);
                for (JsonNode link : eventJSON.path("links")) {
                    if (link.has("target")) {
                        newID = link.path("target").textValue().substring(0, 30).concat(String.format("%06d", i));
                        ((ObjectNode) link).put("target", newID);
                    }
                }
                events.add(eventJSON.toString());
            }
        }
        return events;
    }

    List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();

        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
        eventNames.add("event_EiffelArtifactPublishedEvent_3");
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
        eventNames.add("event_EiffelTestCaseStartedEvent_3");
        eventNames.add("event_EiffelTestCaseFinishedEvent_3");
        eventNames.add("event_EiffelArtifactPublishedEvent_3_1");
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3_1");
        eventNames.add("event_EiffelTestCaseStartedEvent_3_1");
        eventNames.add("event_EiffelTestCaseFinishedEvent_3_1");

        return eventNames;
    }

    private void checkResult() throws IOException, JSONException {
        String expectedDocument = FileUtils.readFileToString(new File(AGGREGATED_OBJECT_FILE_PATH), "UTF-8");
        ObjectMapper objectmapper = new ObjectMapper();
        JsonNode expectedJSON = objectmapper.readTree(expectedDocument);
        for (int i = 0; i < EVENT_PACKAGES; i++) {
            String document = objectHandler.findObjectById(AGGREGATED_OBJECT_ID.concat(String.format("%06d", i)));
            JsonNode actualJSON = objectmapper.readTree(document);
            LOGGER.debug("Complete aggregated object #" + i + ": " + actualJSON);
            assertEquals(expectedJSON.toString().length(), actualJSON.toString().length());
        }
    }

    @Override
    String getRulesFilePath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    String getEventsFilePath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    Map<String, JsonNode> getCheckData() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
