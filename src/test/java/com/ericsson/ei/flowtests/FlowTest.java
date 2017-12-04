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


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FlowTest extends FlowTestBase {

    private static Logger log = LoggerFactory.getLogger(FlowTest.class);

    protected ArrayList<String> getEventNamesToSend() {
         ArrayList<String> eventNames = new ArrayList<>();
         eventNames.add("event_EiffelConfidenceLevelModifiedEvent_3_2");
         eventNames.add("event_EiffelArtifactPublishedEvent_3");
         eventNames.add("event_EiffelArtifactCreatedEvent_3");
         eventNames.add("event_EiffelTestCaseTriggeredEvent_3");
         eventNames.add("event_EiffelTestCaseStartedEvent_3");
         eventNames.add("event_EiffelTestCaseFinishedEvent_3");

         return eventNames;
    }
}
