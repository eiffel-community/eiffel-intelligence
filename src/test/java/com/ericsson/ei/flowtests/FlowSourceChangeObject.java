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

import com.ericsson.ei.rules.RulesHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.qpid.util.FileUtils;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FlowSourceChangeObject extends FlowTest {
   
    private static String inputFilePath = "src/test/resources/aggregatedSourceChangeObject.json";
    private static String jsonFilePath = "src/test/resources/TestSourceChangeObject.json";
    private static String rulePath = "src/test/resources/TestSourceChangeObjectRules.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowTest.class);

    @Autowired
    private RulesHandler rulesHandler;

    protected void setSpecificTestCaseParameters() {
        setJsonFilePath(jsonFilePath);
        rulesHandler.setRulePath(rulePath);
    }

    protected ArrayList<String> getEventNamesToSend() {
        ArrayList<String> eventNames = new ArrayList<>();
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

    protected void checkResult() {
        try {
            String expectedDocuments = FileUtils.readFileAsString(new File(inputFilePath));
            ObjectMapper objectmapper = new ObjectMapper();
            JsonNode expectedJson = objectmapper.readTree(expectedDocuments);
            String document = objectHandler.findObjectById("fb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47");
            JsonNode actualJson = objectmapper.readTree(document);
            JSONAssert.assertEquals(expectedJson.toString(), actualJson.toString(), false);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
