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

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FlowSourceChangeObject extends FlowTestBase {

        private static final Logger LOGGER = LoggerFactory.getLogger(FlowTest.class);

        private static final String RULES_FILE_PATH = "src/test/resources/TestSourceChangeObjectRules.json";
        private static final String JSON_FILE_PATH = "src/test/resources/TestSourceChangeObject.json";
        private static final String INPUT_FILE_PATH = "src/test/resources/aggregatedSourceChangeObject.json";
        private static final String AGGREGATED_OBJECT_ID = "fb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47";

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
                eventNames.add("event_EiffelSourceChangeSubmittedEvent_3");
                eventNames.add("event_EiffelSourceChangeCreatedEvent_3");
                eventNames.add("event_EiffelSourceChangeCreatedEvent_3_2");
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

}
