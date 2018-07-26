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

import com.ericsson.ei.App;
import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.subscriptionhandler.SubscriptionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {App.class})
public class ObjectHandlerTest {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ObjectHandlerTest.class);

    @Autowired
    private ObjectHandler objHandler;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private SubscriptionHandler subscriptionHandler;

    private RulesObject rulesObject;
    private final String inputFilePath = "src/test/resources/RulesHandlerOutput2.json";
    private JsonNode rulesJson;
    private String dataBaseName = "EventStorageDBbbb";
    private String collectionName = "SampleEvents";
    private String input = "{\"TemplateName\":\"ARTIFACT_1\",\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\":[{\"event_id\":\"testcaseid1\",\"test_data\":\"testcase1data\"},{\"event_id\":\"testcaseid2\",\"test_data\":\"testcase2data\"}]}";
    private String condition = "{\"_id\" : \"eventId\"}";
    private String event = "{\"meta\":{\"id\":\"eventId\"}}";

    @Before
    public void init() {
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
            LOGGER.error(e.getMessage(), e);
        }
        rulesObject = new RulesObject(rulesJson);
        assertTrue(objHandler.insertObject(input, rulesObject, event, null));
    }

    @Test
    public void test() {
        String document = objHandler.findObjectById("eventId");
        JsonNode result = objHandler.getAggregatedObject(document);
        assertEquals(input, result.toString());
    }

    @After
    public void dropCollection() {
        mongoDBHandler.dropDocument(dataBaseName, collectionName, condition);
    }
}