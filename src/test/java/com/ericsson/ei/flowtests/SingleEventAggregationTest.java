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

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.handlers.UpStreamEventsHandler;
import com.ericsson.ei.services.ISubscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class,
        SingleEventAggregationTest.class })
@SpringBootTest
public class SingleEventAggregationTest extends FlowTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleEventAggregationTest.class);

    private static final String RULES_FILE_PATH = "src/test/resources/all_event_rules.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/test_All_Events.json";
    private static final String subscriptionJsonPath = "src/test/resources/subscription_CLME.json";

    @Autowired
    private ObjectHandler objectHandler;

    @Autowired
    private UpStreamEventsHandler upStreamEventsHandler;

    @Autowired
    private ISubscriptionService subscriptionService;

    @Mock
    private ERQueryService erQueryService;

    @Override
    String getRulesFilePath() {
        return RULES_FILE_PATH;
    }

    @Override
    String getEventsFilePath() {
        return EVENTS_FILE_PATH;
    }

    @Before
    public void before() {

        MockitoAnnotations.initMocks(this);
        upStreamEventsHandler.setEventRepositoryQueryService(erQueryService);
        when(erQueryService.getEventStreamDataById(anyString(), any(SearchOption.class), anyInt(), anyInt(),
                anyBoolean())).thenReturn(null);
        super.setFirstEventWaitTime(5000);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String readFileToString = FileUtils.readFileToString(new File(subscriptionJsonPath), "UTF-8");
            JSONArray jsonArray = new JSONArray(readFileToString);
            Subscription subscription = mapper.readValue(jsonArray.getJSONObject(0).toString(), Subscription.class);
            subscriptionService.addSubscription(subscription);
        } catch (Exception e) {

        }
    }

    @Override
    List<String> getEventNamesToSend() {
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
            ArrayList<String> eventNames = (ArrayList<String>) getEventNamesToSend();
            String eventsDocument = FileUtils.readFileToString(new File(EVENTS_FILE_PATH), "UTF-8");
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
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    Map<String, JsonNode> getCheckData() {
        Map<String, JsonNode> checkInfo = new HashMap<>();
        return checkInfo;
    }
}
