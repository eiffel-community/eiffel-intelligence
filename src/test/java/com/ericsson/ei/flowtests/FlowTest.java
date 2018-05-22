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

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.ericsson.ei.App;
import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.handlers.UpStreamEventsHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, FlowTest.class })
@SpringBootTest(classes = App.class)
public class FlowTest extends FlowTestBase {

    private static final String UPSTREAM_RESULT_FILE = "upStreamResultFile.json";
    private static final String RULES_FILE_PATH = "src/test/resources/ArtifactRules_new.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/test_events.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/test/resources/AggregatedDocumentInternalCompositionLatest.json";
    private static final String AGGREGATED_OBJECT_ID = "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43";

    @Autowired
    private UpStreamEventsHandler upStreamEventsHandler;

    @Mock
    private ERQueryService erQueryService;

    @Before
    public void before() throws IOException {
        MockitoAnnotations.initMocks(this);
        upStreamEventsHandler.setEventRepositoryQueryService(erQueryService);

        final URL upStreamResult = this.getClass().getClassLoader().getResource(UPSTREAM_RESULT_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.set("upstreamLinkObjects", objectMapper.readTree(upStreamResult));
        objectNode.set("downstreamLinkObjects", objectMapper.createArrayNode());

        when(erQueryService.getEventStreamDataById(anyString(), any(SearchOption.class), anyInt(), anyInt(),
                anyBoolean())).thenReturn(new ResponseEntity<>(objectNode, HttpStatus.OK));
    }

    @Override
    String getRulesFilePath() {
        return RULES_FILE_PATH;
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
