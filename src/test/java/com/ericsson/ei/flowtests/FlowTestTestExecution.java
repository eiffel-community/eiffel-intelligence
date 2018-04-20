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
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, FlowTestTestExecution.class })
@SpringBootTest(classes = App.class)
public class FlowTestTestExecution extends FlowTestBase {

    private static final String RULES_FILE_PATH = "src/test/resources/TestExecutionObjectRules.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/TestExecutionTestEvents.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/test/resources/aggregatedTestActivityObject.json";
    private static final String AGGREGATED_OBJECT_ID = "b46ef12d-25gb-4d7y-b9fd-8763re66de47";

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
        ArrayList<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelActivityTriggeredEvent");
        eventNames.add("event_EiffelActivityStartedEvent");
        eventNames.add("event_EiffelTestExecutionRecipeCollectionCreatedEvent");
        eventNames.add("event_EiffelTestSuiteStartedEvent");
        eventNames.add("event_EiffelTestCaseTriggeredEvent");
        eventNames.add("event_EiffelTestCaseTriggeredEvent_2");
        eventNames.add("event_EiffelTestCaseStartedEvent");
        eventNames.add("event_EiffelTestCaseStartedEvent_2");
        eventNames.add("event_EiffelTestCaseFinishedEvent");
        eventNames.add("event_EiffelTestCaseFinishedEvent_2");
        eventNames.add("event_EiffelActivityFinishedEvent");
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
