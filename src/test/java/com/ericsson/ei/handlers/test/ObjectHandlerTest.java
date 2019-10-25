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
package com.ericsson.ei.handlers.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.handlers.MongoCondition;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.subscription.SubscriptionHandler;
import com.ericsson.ei.test.utils.TestConfigs;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

public class ObjectHandlerTest {

    final Logger log = LoggerFactory.getLogger(ObjectHandlerTest.class);

    private ObjectHandler objHandler = new ObjectHandler();

    private MongodForTestsFactory testsFactory;
    private MongoClient mongoClient = null;

    private MongoDBHandler mongoDBHandler = new MongoDBHandler();

    private JmesPathInterface jmesPathInterface = new JmesPathInterface();

    private SubscriptionHandler subscriptionHandler = new SubscriptionHandler();

    private RulesObject rulesObject;
    private final String inputFilePath = "src/test/resources/RulesHandlerOutput2.json";
    private JsonNode rulesJson;

    private String dataBaseName = "ObjectHandlerTestDB";
    private String collectionName = "SampleEvents";
    private String input = "{\"_id\":\"eventId\",\"TemplateName\":\"ARTIFACT_1\",\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\":[{\"event_id\":\"testcaseid1\",\"test_data\":\"testcase1data\"},{\"event_id\":\"testcaseid2\",\"test_data\":\"testcase2data\"}]}";
    private String event = "{\"meta\":{\"id\":\"eventId\"}}";
    private MongoCondition condition = MongoCondition.idCondition("eventId");

    @Before
    public void init() throws Exception {
        TestConfigs.init();
        mongoDBHandler.setMongoClient(TestConfigs.getMongoClient());
        subscriptionHandler.setMongoDBHandler(mongoDBHandler);
        EventToObjectMapHandler eventToObjectMapHandler = mock(EventToObjectMapHandler.class);
        objHandler.setEventToObjectMap(eventToObjectMapHandler);
        objHandler.setMongoDbHandler(mongoDBHandler);
        objHandler.setJmespathInterface(jmesPathInterface);
        objHandler.setCollectionName(collectionName);
        objHandler.setDatabaseName(dataBaseName);
        objHandler.setSubscriptionHandler(subscriptionHandler);

        try {
            String rulesString = FileUtils.readFileToString(new File(inputFilePath), "UTF-8");
            ObjectMapper objectmapper = new ObjectMapper();
            rulesJson = objectmapper.readTree(rulesString);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        rulesObject = new RulesObject(rulesJson);
        objHandler.insertObject(input, rulesObject, event, null);
    }

    @Test
    public void testFindInsertedObject() {
        String document = objHandler.findObjectById("eventId").replace(" ", "");
        assertEquals(input.replace(" ", ""), document);
    }

    @After
    public void dropCollection() {
        mongoDBHandler.dropDocument(dataBaseName, collectionName, condition);
        if (mongoClient != null)
            mongoClient.close();
        if (testsFactory != null)
            testsFactory.shutdown();
    }
}
