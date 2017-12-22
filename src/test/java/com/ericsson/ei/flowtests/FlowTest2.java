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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FlowTest2 extends FlowTestBase {

    private static Logger log = LoggerFactory.getLogger(FlowTest2.class);

    static private final String inputFilePath2 = "src/test/resources/AggregatedDocument2.json";

    protected ArrayList<String> getEventNamesToSend() {
        ArrayList<String> eventNames = new ArrayList<>();
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

    protected void checkResult() {
        try {
            String document = objectHandler.findObjectById("6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43");
            String expectedDocument = FileUtils.readFileToString(new File(inputFilePath), "UTF-8");
            ObjectMapper objectmapper = new ObjectMapper();
            JsonNode expectedJson = objectmapper.readTree(expectedDocument);
            JsonNode actualJson = objectmapper.readTree(document);
            String breakString = "breakHere";
            assertEquals(expectedJson.toString().length(), actualJson.toString().length());
            String expectedDocument2 = FileUtils.readFileToString(new File(inputFilePath2), "UTF-8");
            String document2 = objectHandler.findObjectById("ccce572c-c364-441e-abc9-b62fed080ca2");
            JsonNode expectedJson2 = objectmapper.readTree(expectedDocument2);
            JsonNode actualJson2 = objectmapper.readTree(document2);
            assertEquals(expectedJson2.toString().length(), actualJson2.toString().length());
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }
}
