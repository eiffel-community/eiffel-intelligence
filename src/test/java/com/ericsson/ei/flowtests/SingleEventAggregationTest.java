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
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SingleEventAggregationTest extends FlowTestBase {

    private static Logger log = LoggerFactory.getLogger(FlowTest.class);

    static protected String jsonFilePath = "src/test/resources/test_All_Events.json";
    static protected String rulePath = "src/test/resources/all_event_rules.json";

    @Autowired
    RulesHandler rulesHandler;

    protected void setSpecificTestCaseParameters() {
        setJsonFilePath(jsonFilePath);
        rulesHandler.setRulePath(rulePath);
    }

    protected ArrayList<String> getEventNamesToSend() {
        ArrayList<String> eventNames = new ArrayList<>();
        eventNames.add("EiffelActivityCanceledEvent");
        eventNames.add("EiffelActivityStartedEvent");
        eventNames.add("EiffelActivityFinishedEvent");
        eventNames.add("EiffelActivityTriggeredEvent");
        eventNames.add("EiffelAnnouncementPublishedEvent");
        eventNames.add("EiffelArtifactCreatedEvent");
        eventNames.add("EiffelArtifactPublishedEvent");
        eventNames.add("EiffelArtifactReusedEvent");
        eventNames.add("EiffelCompositionDefinedEvent");
        eventNames.add("EiffelConfidenceLevelModifiedEvent");
        eventNames.add("EiffelEnvironmentDefinedEvent");
        eventNames.add("EiffelFlowContextDefinedEvent");
        eventNames.add("EiffelIssueVerifiedEvent");
        eventNames.add("EiffelSourceChangeCreatedEvent");
        eventNames.add("EiffelSourceChangeSubmittedEvent");
        eventNames.add("EiffelTestCaseCanceledEvent");
        eventNames.add("EiffelTestCaseFinishedEvent");
        eventNames.add("EiffelTestCaseStartedEvent");
        eventNames.add("EiffelTestCaseTriggeredEvent");
        eventNames.add("EiffelTestExecutionRecipeCollectionCreatedEvent");
        eventNames.add("EiffelTestSuiteFinishedEvent");
        eventNames.add("EiffelTestSuiteStartedEvent");
        eventNames.add("EiffelArtifactDeployedEvent");
        eventNames.add("EiffelServiceAllocatedEvent");
        eventNames.add("EiffelServiceDeployedEvent");
        eventNames.add("EiffelServiceDiscontinuedEvent");
        eventNames.add("EiffelServiceReturnedEvent");
        eventNames.add("EiffelServiceStartedEvent");
        eventNames.add("EiffelServiceStoppedEvent");
        eventNames.add("EiffelAlertAcknowledgedEvent");
        eventNames.add("EiffelAlertCeasedEvent");
        eventNames.add("EiffelAlertRaisedEvent");

        return eventNames;
    }

    protected void checkResult() {
        try {
            ArrayList<String> eventNames = getEventNamesToSend();
            String eventsDocument = FileUtils.readFileToString(new File(jsonFilePath), "UTF-8");
            ObjectMapper objectmapper = new ObjectMapper();
            JsonNode eventsJson = objectmapper.readTree(eventsDocument);

            for (String temp : eventNames) {
                String expectedEvent = eventsJson.at("/" + temp).toString();
                String actualEventID = eventsJson.at("/" + temp + "/meta/id").asText();
                String document = objectHandler.findObjectById(actualEventID);
                JsonNode actualJson = objectmapper.readTree(document);
                JsonNode actualEvent = actualJson.at("/aggregatedObject");
                ObjectNode objectActualEvent = (ObjectNode) actualEvent;
                objectActualEvent.without("TemplateName");
                assertEquals(objectActualEvent.toString(), expectedEvent);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
