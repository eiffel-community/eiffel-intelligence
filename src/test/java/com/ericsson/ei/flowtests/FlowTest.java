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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.ericsson.ei.App;
import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.exception.PropertyNotFoundException;
import com.ericsson.ei.handlers.UpStreamEventsHandler;
import com.ericsson.ei.utils.TestContextInitializer;
import com.ericsson.eiffelcommons.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, FlowTest.class })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@SpringBootTest(classes = App.class)
@TestPropertySource(properties = {
        "rules.path: src/test/resources/ArtifactRules.json",
        "spring.data.mongodb.database: FlowTest",
        "failed.notifications.collection.name: FlowTest-failedNotifications",
        "rabbitmq.exchange.name: FlowTest-exchange",
        "rabbitmq.queue.suffix: FlowTest" })
public class FlowTest extends FlowTestBase {

    private static final String UPSTREAM_RESULT_FILE = "upStreamResultFile.json";
    private static final String UPSTREAM_INPUT_FILE = "upStreamInput.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/ArtifactFlowTestEvents.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/test/resources/AggregatedDocumentInternalCompositionLatestUnitTest.json";
    private static final String AGGREGATED_OBJECT_ID = "aacc3c87-75e0-4b6d-88f5-b1a5d4e62b43";

    @Autowired
    private UpStreamEventsHandler upStreamEventsHandler;

    @Mock
    private ERQueryService erQueryService;

    @Autowired
    private RabbitTemplate rabbitMqTemplate;

    @Before
    public void before() throws PropertyNotFoundException, Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        if (!systemTest) {
            final URL upStreamResult = this.getClass().getClassLoader().getResource(UPSTREAM_RESULT_FILE);
            MockitoAnnotations.initMocks(this);
            upStreamEventsHandler.setEventRepositoryQueryService(erQueryService);

            ObjectNode objectNode = objectMapper.createObjectNode();
            JsonNode upstreamJson = objectMapper.readTree(upStreamResult);
            objectNode.set("upstreamLinkObjects", upstreamJson);
            objectNode.set("downstreamLinkObjects", objectMapper.createArrayNode());

            Header[] headers = {};
            when(erQueryService.getEventStreamDataById(anyString(), any(SearchOption.class), anyInt(), anyInt(),
                    anyBoolean())).thenReturn(new ResponseEntity(200, objectNode.toString(), headers));
        } else {
            final URL upStreamInput = this.getClass().getClassLoader().getResource(UPSTREAM_INPUT_FILE);
            ArrayNode upstreamJson = (ArrayNode) objectMapper.readTree(upStreamInput);
            if (upstreamJson != null) {
                for (JsonNode event : upstreamJson) {
                    String eventStr = event.toString();
                    rabbitMqTemplate.convertAndSend(eventStr);
                }
            }
        }
    }

    @Override
    String getEventsFilePath() {
        return EVENTS_FILE_PATH;
    }

    @Override
    protected int extraEventsCount() {
        return 8;
    }

    @Override
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

    @Override
    Map<String, JsonNode> getCheckData() throws IOException {
        JsonNode expectedJSON = getJSONFromFile(AGGREGATED_OBJECT_FILE_PATH);
        Map<String, JsonNode> checkData = new HashMap<>();
        checkData.put(AGGREGATED_OBJECT_ID, expectedJSON);
        return checkData;
    }
}
