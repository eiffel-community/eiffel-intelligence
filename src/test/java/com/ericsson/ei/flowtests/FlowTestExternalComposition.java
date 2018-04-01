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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FlowTestExternalComposition extends FlowTestBase {

    private static final String RULES_FILE_PATH = "src/test/resources/ArtifactRules_new.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/test_events.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/test/resources/aggregatedExternalComposition.json";
    private static final String AGGREGATED_OBJECT_ID_1 = "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43";
    private static final String AGGREGATED_OBJECT_ID_2 = "cfce572b-c3j4-441e-abc9-b62f48080ca2";
    private static final String AGGREGATED_OBJECT_ID_3 = "cfce572b-c3j4-441e-abc9-b62f48080ca2";

    @Override String setRulesFilePath() {
        return RULES_FILE_PATH;
    }

    @Override String setEventsFilePath() {
        return EVENTS_FILE_PATH;
    }

    @Override List<String> setEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("event_EiffelArtifactCreatedEvent_3");
        //eventNames.add("event_EiffelCompositionDefinedEvent_4");
        eventNames.add("event_EiffelArtifactCreatedEvent_4");
        //eventNames.add("event_EiffelCompositionDefinedEvent_5");
        eventNames.add("event_EiffelArtifactCreatedEvent_5");
        return eventNames;
    }

    @Override Map<String, String> setCheckInfo() {
        Map<String, String> checkInfo = new HashMap<>();
        return checkInfo;
    }
}
