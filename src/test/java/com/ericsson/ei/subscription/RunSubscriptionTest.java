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
package com.ericsson.ei.subscription;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.controller.model.QueryResponse;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.utils.TestContextInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: SubscriptionHandlerTest",
        "failed.notifications.collection.name: SubscriptionHandlerTest-failedNotifications",
        "rabbitmq.exchange.name: SubscriptionHandlerTest-exchange",
        "subscriptions.repeat.handler.collection.name: SubscriptionHandlerTestCollection",
        "rabbitmq.queue.name: SubscriptionHandlerTest",
        "failed.notifications.collection.ttl: 1"})
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
@AutoConfigureMockMvc
public class RunSubscriptionTest {

    private static final String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static final String aggregatedInternalPath = "src/test/resources/AggregatedDocumentInternalCompositionLatest.json";
    private static final String subscriptionPath = "src/test/resources/SubscriptionObject.json";
    private static final String artifactRequirementSubscriptionPath = "src/test/resources/artifactRequirementSubscription.json";

    private static String aggregatedObject;
    private static String aggregatedInternalObject;
    private static String subscriptionData;
    private static String artifactRequirementSubscriptionData;
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RunSubscription runSubscription;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private static String subscriptionRepeatFlagTruePath = "src/test/resources/SubscriptionRepeatFlagTrueObject.json";
    private static String subscriptionRepeatFlagTrueData;


    @Value("${spring.data.mongodb.database}")
    private String subRepeatFlagDataBaseName;

    @Value("${subscriptions.repeat.handler.collection.name}")
    private String subRepeatFlagCollectionName;

    @Mock
    private QueryResponse queryResponse;

    @Before
    public void beforeTests() throws IOException {
        aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath), "UTF-8");
        aggregatedInternalObject = FileUtils.readFileToString(new File(aggregatedInternalPath), "UTF-8");

        artifactRequirementSubscriptionData = FileUtils.readFileToString(new File(artifactRequirementSubscriptionPath),
                "UTF-8");
        subscriptionRepeatFlagTrueData = FileUtils.readFileToString(new File(subscriptionRepeatFlagTruePath), "UTF-8");

        subscriptionData = FileUtils.readFileToString(new File(subscriptionPath), "UTF-8");
    }

    @After
    public void afterTests() throws IOException {
        mongoDBHandler.dropCollection(subRepeatFlagDataBaseName, subRepeatFlagCollectionName);
    }


    @Test
    public void runSubscriptionOnObjectTest() throws Exception {
        JsonNode subscriptionJson = mapper.readTree(subscriptionData);
        ArrayNode requirementNode = (ArrayNode) subscriptionJson.get("requirements");
        Iterator<JsonNode> requirementIterator = requirementNode.elements();

        boolean output = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator,
                subscriptionJson, "someID");
        assertTrue(output);
    }

    @Test
    public void runRequirementSubscriptionOnObjectTest() throws Exception {
        JsonNode subscriptionJson = mapper.readTree(artifactRequirementSubscriptionData);
        JsonNode aggregatedDocument = mapper.readTree(aggregatedInternalObject);
        ArrayNode requirementNode = (ArrayNode) subscriptionJson.get("requirements");
        Iterator<JsonNode> requirementIterator = requirementNode.elements();
        String aggregationStr = aggregatedDocument.toString();
        boolean output = runSubscription.runSubscriptionOnObject(aggregationStr, requirementIterator, subscriptionJson,
                "someID");
        assertTrue(output);
    }

    @Test
    public void runSubscriptionOnObjectRepeatFlagFalseTest() throws Exception {
        JsonNode subscriptionJson = mapper.readTree(subscriptionData);
        ArrayNode requirementNode = (ArrayNode) subscriptionJson.get("requirements");
        Iterator<JsonNode> requirementIterator = requirementNode.elements();
        Iterator<JsonNode> requirementIterator2 = requirementNode.elements();

        boolean output1 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator,
                subscriptionJson, "someID");
        boolean output2 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator2,
                subscriptionJson, "someID");
        assertTrue(output1);
        assertFalse(output2);
    }

    @Test
    public void runSubscriptionOnObjectRepeatFlagTrueTest() throws Exception {
        JsonNode subscriptionJson = mapper.readTree(subscriptionRepeatFlagTrueData);
        ArrayNode requirementNode = (ArrayNode) subscriptionJson.get("requirements");
        Iterator<JsonNode> requirementIterator = requirementNode.elements();
        Iterator<JsonNode> requirementIterator2 = requirementNode.elements();

        boolean output1 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator,
                subscriptionJson, "someID");
        boolean output2 = runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator2,
                subscriptionJson, "someID");
        assertTrue(output1);
        assertTrue(output2);
    }

}
