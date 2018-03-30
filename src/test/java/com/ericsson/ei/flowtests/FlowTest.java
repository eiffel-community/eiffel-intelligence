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

import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.handlers.UpStreamEventsHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FlowTest extends FlowTestBase {

        private static final Logger LOGGER = LoggerFactory.getLogger(FlowTest.class);

        private static final String UPSTREAM_RESULT_FILE = "upStreamResultFile.json";

        private static final String RULES_FILE_PATH = "src/test/resources/ArtifactRules_new.json";
        private static final String JSON_FILE_PATH = "src/test/resources/test_events.json";
        private static final String INPUT_FILE_PATH = "src/test/resources/AggregatedDocumentInternalComposition.json";
        private static final String AGGREGATED_OBJECT_ID = "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43";

        @Autowired
        private UpStreamEventsHandler upStreamEventsHandler;

        @Mock
        private ERQueryService erQueryService;

        @Before
        public void before() throws IOException {
                upStreamEventsHandler.setEventRepositoryQueryService(erQueryService);

                final URL upStreamResult = this.getClass().getClassLoader().getResource(UPSTREAM_RESULT_FILE);
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode objectNode = objectMapper.createObjectNode();
                objectNode.set("upstreamLinkObjects", objectMapper.readTree(upStreamResult));
                objectNode.set("downstreamLinkObjects", objectMapper.createArrayNode());

                when(erQueryService.getEventStreamDataById(anyString(), any(SearchOption.class), anyInt(), anyInt(), anyBoolean()))
                        .thenReturn(new ResponseEntity<>(objectNode, HttpStatus.OK));
        }

        @Override
        String setJsonFilePath() {
                return JSON_FILE_PATH;
        }

        @Override
        String setRulesFilePath() {
                return RULES_FILE_PATH;
        }

        @Override
        Map<String, String> setInputFiles() {
                Map<String, String> inputFiles = new HashMap<>();
                inputFiles.put(AGGREGATED_OBJECT_ID, INPUT_FILE_PATH);
                return inputFiles;
        }

        @Override
        List<String> setEventNamesToSend() {
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
}
