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

import com.ericsson.ei.App;
import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.handlers.UpStreamEventsHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, FlowSourceChangeObject.class})
@SpringBootTest(classes = App.class)
public class FlowSourceChangeObject extends FlowTestBase {

    private static final String RULES_FILE_PATH = "src/test/resources/TestSourceChangeObjectRules.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/TestSourceChangeObject.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/test/resources/aggregatedSourceChangeObject.json";
    private static final String AGGREGATED_OBJECT_ID = "fb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47";
    private static final String UPSTREAM_FILE = "UpstreamEventsForSourceChange.json";

    @Override
    String getRulesFilePath() {
        return RULES_FILE_PATH;
    }

    @Override
    String getEventsFilePath() {
        return EVENTS_FILE_PATH;
    }

    @Autowired
    private UpStreamEventsHandler upStreamEventsHandler;

    @Mock
    private ERQueryService erQueryService;

    @Before
    public void before() throws IOException {
        MockitoAnnotations.initMocks(this);
        upStreamEventsHandler.setEventRepositoryQueryService(erQueryService);
        final URL upStreamResult = this.getClass().getClassLoader().getResource(UPSTREAM_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.set("upstreamLinkObjects", objectMapper.readTree(upStreamResult));
        objectNode.set("downstreamLinkObjects", objectMapper.createArrayNode());
        when(erQueryService.getEventStreamDataById(anyString(), any(SearchOption.class), anyInt(), anyInt(),
                anyBoolean())).thenReturn(new ResponseEntity<>(objectNode, HttpStatus.OK));
    }

    @Override
    List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelSourceChangeSubmittedEvent_3");
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3");
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
        eventNames.add("event_EiffelActivityTriggeredEvent_3");
        eventNames.add("event_EiffelActivityTriggeredEvent_3_2");
        eventNames.add("event_EiffelActivityStartedEvent_3");
        eventNames.add("event_EiffelActivityStartedEvent_3_2");
        eventNames.add("event_EiffelActivityFinishedEvent_3");
        eventNames.add("event_EiffelActivityFinishedEvent_3_2");
        eventNames.add("event_EiffelActivityCanceledEvent_3");
        eventNames.add("event_EiffelActivityCanceledEvent_3_2");
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
