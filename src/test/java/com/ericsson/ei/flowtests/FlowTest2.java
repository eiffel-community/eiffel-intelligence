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
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, FlowTest2.class })
@SpringBootTest(classes = App.class)
public class FlowTest2 extends FlowTestBase {

    private static final String RULES_FILE_PATH = "src/test/resources/ArtifactRules_new.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/test_events.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH_1 = "src/test/resources/AggregatedDocument.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH_2 = "src/test/resources/AggregatedDocument2.json";
    private static final String AGGREGATED_OBJECT_ID_1 = "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43";
    private static final String AGGREGATED_OBJECT_ID_2 = "ccce572c-c364-441e-abc9-b62fed080ca2";

    @Override
    String getRulesFilePath() {
        return RULES_FILE_PATH;
    }

    @Override
    String getEventsFilePath() {
        return EVENTS_FILE_PATH;
    }

    @Override
    List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        eventNames.add("event_EiffelArtifactPublishedEvent_3");
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
        eventNames.add("event_EiffelTestCaseStartedEvent_3");
        eventNames.add("event_EiffelTestCaseFinishedEvent_3");

        eventNames.add("event_EiffelArtifactCreatedEvent_1");
        eventNames.add("event_EiffelArtifactPublishedEvent_1");
        eventNames.add("event_EiffelConfidenceLevelModifiedEvent_1");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_1");
        eventNames.add("event_EiffelTestCaseStartedEvent_1");
        eventNames.add("event_EiffelTestCaseFinishedEvent_1");

        return eventNames;
    }

    @Override
    Map<String, JsonNode> getCheckData() throws IOException {
        JsonNode expectedJSON1 = getJSONFromFile(AGGREGATED_OBJECT_FILE_PATH_1);
        JsonNode expectedJSON2 = getJSONFromFile(AGGREGATED_OBJECT_FILE_PATH_2);
        Map<String, JsonNode> checkData = new HashMap<>();
        checkData.put(AGGREGATED_OBJECT_ID_1, expectedJSON1);
        checkData.put(AGGREGATED_OBJECT_ID_2, expectedJSON2);
        return checkData;
    }
}
