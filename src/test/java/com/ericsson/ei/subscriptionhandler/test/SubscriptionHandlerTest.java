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
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
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

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SubscriptionHandlerTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SubscriptionHandlerTest.class);
    private static final String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static final String subscriptionPath = "src/test/resources/SubscriptionObject.json";
    private static final String DB_NAME = "MissedNotification";
    private static final String COLLECTION_NAME = "Notification";
    private static final String REGEX = "^\"|\"$";
    private static final String MISSED_NOTIFICATION_URL = "/queryMissedNotifications";
    private static final int STATUS_OK = 200;
    private static String aggregatedObject;
    private static String subscriptionData;
    private static String url;
    private static String headerContentMediaType;
    private static MongodForTestsFactory testsFactory;
    private static MongoClient mongoClient = null;

    @Autowired
    private RunSubscription runSubscription;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private JmesPathInterface jmespath;

    @Autowired
    private InformSubscription subscription;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProcessMissedNotification processMissedNotification;

    @MockBean
    private SpringRestTemplate springRestTemplate;

    @Autowired
    private SendMail sendMail;

    @Mock
    private QueryResponse queryResponse;

    public static void setUpEmbeddedMongo() throws JSONException, IOException {
        testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
        mongoClient = testsFactory.newMongo();
        aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath), "UTF-8");
        subscriptionData = FileUtils.readFileToString(new File(subscriptionPath), "UTF-8");
        url = new JSONObject(subscriptionData).getString("notificationMeta").replaceAll(REGEX, "");
        headerContentMediaType = new JSONObject(subscriptionData).getString("restPostBodyMediaType");
    }

    @BeforeClass
    public static void init() throws Exception {
        setUpEmbeddedMongo();
        System.setProperty("notification.ttl.value", "1");
    }

    @AfterClass
    public static void close() {
        testsFactory.shutdown();
    }

    @PostConstruct
    public void initMocks() {
        mongoDBHandler.setMongoClient(mongoClient);
        LOGGER.debug("Database connected");
    }

    @Test
    public void runSubscriptionOnObjectTest() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode subscriptionJson;
        ArrayNode requirementNode;
        Iterator<JsonNode> requirementIterator = null;
        try {
            subscriptionJson = mapper.readTree(subscriptionData);
            requirementNode = (ArrayNode) subscriptionJson.get("requirements");
            requirementIterator = requirementNode.elements();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        boolean output = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator);
        assertEquals(output, true);
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
            assertTrue(false);
            LOGGER.error(e.getMessage(), e);
        }
        JsonNode output = jsonResult.get("AggregatedObject");
        assertEquals(expectedOutput, output);
    }

    @Test
    public void missedNotificationWithTTLTest() throws IOException, InterruptedException {
        subscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionData));
        Thread.sleep(70000);
        assertTrue(mongoDBHandler.getAllDocuments(DB_NAME, COLLECTION_NAME).isEmpty());
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
        when(springRestTemplate.postDataMultiValue(url, mapNotificationMessage(), headerContentMediaType)).thenReturn(STATUS_OK);
        subscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionData));
        verify(springRestTemplate, times(1)).postDataMultiValue(url, mapNotificationMessage(), headerContentMediaType);
    }

    @Test
    public void testRestPostTriggerFailure() throws IOException {
        subscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionData));
        verify(springRestTemplate, times(4)).postDataMultiValue(url, mapNotificationMessage(), headerContentMediaType);
        assertFalse(mongoDBHandler.getAllDocuments(DB_NAME, COLLECTION_NAME).isEmpty());
    }

    @Test
    public void testQueryMissedNotificationEndPoint() throws Exception {
        String subscriptionName = new JSONObject(subscriptionData).getString("subscriptionName").replaceAll(REGEX, "");
        JSONObject input = new JSONObject(aggregatedObject);
        subscription.informSubscriber(aggregatedObject, new ObjectMapper().readTree(subscriptionData));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(MISSED_NOTIFICATION_URL)
                .param("SubscriptionName", subscriptionName)).andReturn();
        String response = result.getResponse().getContentAsString().replace("\\", "");
        assertEquals("{\"responseEntity\":\"[" + input.toString().replace("\\", "") + "]\"}", response);
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    private MultiValueMap<String, String> mapNotificationMessage() throws IOException {
        MultiValueMap<String, String> mapNotificationMessage = new LinkedMultiValueMap<>();
        ArrayNode arrNode = (ArrayNode) new ObjectMapper().readTree(subscriptionData).get("notificationMessageKeyValues");
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                mapNotificationMessage.add(objNode.get("formkey").toString().replaceAll(REGEX, ""),
                        jmespath.runRuleOnEvent(objNode.get("formvalue").toString().replaceAll(REGEX, ""),
                                aggregatedObject).toString().replaceAll(REGEX, ""));
            }
        }
        return mapNotificationMessage;
    }
}