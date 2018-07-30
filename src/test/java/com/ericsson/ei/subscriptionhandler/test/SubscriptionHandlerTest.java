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
package com.ericsson.ei.subscriptionhandler.test;

import com.ericsson.ei.App;
import com.ericsson.ei.controller.model.QueryResponse;
import com.ericsson.ei.exception.SubscriptionValidationException;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.queryservice.ProcessMissedNotification;
import com.ericsson.ei.subscriptionhandler.InformSubscription;
import com.ericsson.ei.subscriptionhandler.RunSubscription;
import com.ericsson.ei.subscriptionhandler.SendMail;
import com.ericsson.ei.subscriptionhandler.SpringRestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {App.class})
@AutoConfigureMockMvc
public class SubscriptionHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionHandlerTest.class);
    private static final String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static final String subscriptionPath = "src/test/resources/SubscriptionObject.json";
    private static final String subscriptionPathForAuthorization = "src/test/resources/SubscriptionObjectForAuthorization.json";
    private static final String DB_NAME = "MissedNotification";
    private static final String COLLECTION_NAME = "Notification";
    private static final String REGEX = "^\"|\"$";
    private static final String MISSED_NOTIFICATION_URL = "/queryMissedNotifications";
    private static final int STATUS_OK = 200;
    private static String aggregatedObject;
    private static String subscriptionData;
    private static String subscriptionDataForAuthorization;
    private static String url;
    private static String headerContentMediaType;
    private static String urlAuthorization;
    private static String headerContentMediaTypeAuthorization;
    private static final String formkey = "Authorization";
    private static final String formvalue = "Basic XX0=";

    @Autowired
    private RunSubscription runSubscription;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private JmesPathInterface jmespath;

    private static String subscriptionRepeatFlagTruePath = "src/test/resources/SubscriptionRepeatFlagTrueObject.json";
    private static String subscriptionPathForEmail = "src/test/resources/SubscriptionForMail.json";
    private static String subscriptionRepeatFlagTrueData;
    private static String subscriptionDataEmail;

    @Autowired
    private InformSubscription subscription;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessMissedNotification processMissedNotification;

    @MockBean
    private SpringRestTemplate springRestTemplate;

    private static String subRepeatFlagDataBaseName = "eiffel_intelligence";
    private static String subRepeatFlagCollectionName = "subscription_repeat_handler";

    @Autowired
    private SendMail sendMail;

    @Mock
    private QueryResponse queryResponse;

    private static void setUpEmbeddedMongo() throws JSONException {
        try {
            aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath), "UTF-8");
            subscriptionData = FileUtils.readFileToString(new File(subscriptionPath), "UTF-8");
            subscriptionRepeatFlagTrueData = FileUtils.readFileToString(new File(subscriptionRepeatFlagTruePath),
                    "UTF-8");
            subscriptionDataForAuthorization = FileUtils.readFileToString(new File(subscriptionPathForAuthorization), "UTF-8");
            subscriptionDataEmail = FileUtils.readFileToString(new File(subscriptionPathForEmail), "UTF-8");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        url = new JSONObject(subscriptionData).getString("notificationMeta").replaceAll(REGEX, "");
        headerContentMediaType = new JSONObject(subscriptionData).getString("restPostBodyMediaType");
        urlAuthorization = new JSONObject(subscriptionDataForAuthorization).getString("notificationMeta").replaceAll(REGEX, "");
        headerContentMediaTypeAuthorization = new JSONObject(subscriptionDataForAuthorization).getString("restPostBodyMediaType");
    }

    @BeforeClass
    public static void init() throws Exception {
        setUpEmbeddedMongo();
        System.setProperty("notification.ttl.value", "1");
    }

    @Before
    public void beforeTests() {
        mongoDBHandler.dropCollection(subRepeatFlagDataBaseName, subRepeatFlagCollectionName);
    }

    @Test
    public void runSubscriptionOnObjectTest() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode subscriptionJson = null;
        ArrayNode requirementNode;
        Iterator<JsonNode> requirementIterator = null;
        try {
            subscriptionJson = mapper.readTree(subscriptionData);
            requirementNode = (ArrayNode) subscriptionJson.get("requirements");
            requirementIterator = requirementNode.elements();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        boolean output = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator,
                subscriptionJson, "someID");
        assertTrue(output);
    }

    @Test
    public void runSubscriptionOnObjectRepeatFlagFalseTest() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode subscriptionJson = null;
        ArrayNode requirementNode = null;
        Iterator<JsonNode> requirementIterator = null;
        try {
            subscriptionJson = mapper.readTree(subscriptionData);
            requirementNode = (ArrayNode) subscriptionJson.get("requirements");
            requirementIterator = requirementNode.elements();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        boolean output1 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator,
                subscriptionJson, "someID");
        boolean output2 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator,
                subscriptionJson, "someID");
        assertTrue(output1);
        assertFalse(output2);
    }

    @Test
    public void runSubscriptionOnObjectRepeatFlagTrueTest() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode subscriptionJson = null;
        ArrayNode requirementNode = null;
        ArrayNode requirementNode2 = null;
        Iterator<JsonNode> requirementIterator = null;
        Iterator<JsonNode> requirementIterator2 = null;
        try {
            subscriptionJson = mapper.readTree(subscriptionRepeatFlagTrueData);
            requirementNode = (ArrayNode) subscriptionJson.get("requirements");
            requirementNode2 = (ArrayNode) subscriptionJson.get("requirements");
            requirementIterator = requirementNode.elements();
            requirementIterator2 = requirementNode2.elements();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        boolean output1 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator,
                subscriptionJson, "someID");
        boolean output2 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator2,
                subscriptionJson, "someID");
        assertTrue(output1);
        assertTrue(output2);
    }

    @Test
    public void missedNotificationTest() throws IOException {
        subscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionData));
        Iterable<String> outputDoc = mongoDBHandler.getAllDocuments(DB_NAME, COLLECTION_NAME);
        Iterator itr = outputDoc.iterator();
        String data = itr.next().toString();
        JsonNode jsonResult = null;
        JsonNode expectedOutput = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            expectedOutput = mapper.readTree(aggregatedObject);
            jsonResult = mapper.readTree(data);
        } catch (IOException e) {
            fail();
            LOGGER.error(e.getMessage(), e);
        }
        JsonNode output = jsonResult.get("AggregatedObject");
        assertEquals(expectedOutput, output);
    }

    @Test
    public void missedNotificationWithTTLTest() throws IOException, InterruptedException {
        System.out.println(subscriptionData);
        subscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionData));
        // Time to live lower than 60 seconds will not have any effect since
        // removal runs every 60 seconds
        Thread.sleep(65000);
        List<String> allDocs = mongoDBHandler.getAllDocuments(DB_NAME, COLLECTION_NAME);
        System.out.println(allDocs.toString());
        assertTrue(allDocs.isEmpty());
    }

    @Test
    public void sendMailTest() {
        Set<String> extRec = new HashSet<>();
        String recievers = "asdf.hklm@ericsson.se, affda.fddfd@ericsson.com, sasasa.dfdfdf@fdad.com, abcd.defg@gmail.com";
        try {
            extRec = (sendMail.extractEmails(recievers));
        } catch (SubscriptionValidationException e) {
            // TODO Auto-generated catch block
            LOGGER.error(e.getMessage(), e);
        }
        assertEquals(String.valueOf(extRec.toArray().length), "4");
    }

    @Test
    public void testRestPostTrigger() throws IOException {
        when(springRestTemplate.postDataMultiValue(url, mapNotificationMessage(subscriptionData),
                headerContentMediaType)).thenReturn(STATUS_OK);
        subscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionData));
        verify(springRestTemplate, times(1)).postDataMultiValue(url, mapNotificationMessage(subscriptionData),
                headerContentMediaType);
    }

    @Test
    public void testRestPostTriggerForAuthorization() throws IOException {
        when(springRestTemplate.postDataMultiValue(urlAuthorization, mapNotificationMessage(subscriptionDataForAuthorization),
                headerContentMediaTypeAuthorization, formkey, formvalue)).thenReturn(STATUS_OK);
        subscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionDataForAuthorization));
        verify(springRestTemplate, times(1)).postDataMultiValue(urlAuthorization,
                mapNotificationMessage(subscriptionDataForAuthorization), headerContentMediaTypeAuthorization, formkey, formvalue);
    }

    @Test
    public void testRestPostTriggerFailure() throws IOException {
        subscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionData));
        verify(springRestTemplate, times(4)).postDataMultiValue(url, mapNotificationMessage(subscriptionData),
                headerContentMediaType);
        assertFalse(mongoDBHandler.getAllDocuments(DB_NAME, COLLECTION_NAME).isEmpty());
    }

    @Test
    public void testQueryMissedNotificationEndPoint() throws Exception {
        String subscriptionName = new JSONObject(subscriptionData).getString("subscriptionName").replaceAll(REGEX, "");
        JSONObject input = new JSONObject(aggregatedObject);
        subscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionData));
        MvcResult result = mockMvc
                .perform(
                        MockMvcRequestBuilders.get(MISSED_NOTIFICATION_URL).param("SubscriptionName", subscriptionName))
                .andReturn();
        String response = result.getResponse().getContentAsString().replace("\\", "");
        assertEquals("{\"responseEntity\":\"[" + input.toString().replace("\\", "") + "]\"}", response);
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    private MultiValueMap<String, String> mapNotificationMessage(String data) throws IOException {
        MultiValueMap<String, String> mapNotificationMessage = new LinkedMultiValueMap<>();

        ArrayNode arrNode = (ArrayNode) new ObjectMapper().readTree(data).get("notificationMessageKeyValues");
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                if (!objNode.get("formkey").toString().replaceAll(REGEX, "").equals("Authorization")) {

                    mapNotificationMessage.add(objNode.get("formkey").toString().replaceAll(REGEX, ""), jmespath
                            .runRuleOnEvent(objNode.get("formvalue").toString().replaceAll(REGEX, ""), aggregatedObject)
                            .toString().replaceAll(REGEX, ""));
                }
            }
        }
        return mapNotificationMessage;
    }
}