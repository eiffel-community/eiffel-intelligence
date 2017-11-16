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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import com.ericsson.ei.subscriptionhandler.SubscriptionHandler;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.rules.test.TestRulesObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import de.flapdoodle.embed.process.runtime.Network;

public class ObjectHandlerTest {

    static Logger log = (Logger) LoggerFactory.getLogger(ObjectHandlerTest.class);

    static ObjectHandler objHandler = new ObjectHandler();

    private static MongodForTestsFactory testsFactory;
    static MongoClient mongoClient = null;


    static MongoDBHandler mongoDBHandler = new MongoDBHandler();

    static JmesPathInterface jmesPathInterface = new JmesPathInterface();

    static SubscriptionHandler subscriptionHandler = new SubscriptionHandler();

    static private RulesObject rulesObject;
    static private final String inputFilePath = "src/test/resources/RulesHandlerOutput2.json";
    static private JsonNode rulesJson;

    static String dataBaseName = "EventStorageDBbbb";
    static String collectionName = "SampleEvents";
    static String input = "{\"TemplateName\":\"ARTIFACT_1\",\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\":[{\"event_id\":\"testcaseid1\",\"test_data\":\"testcase1data\"},{\"event_id\":\"testcaseid2\",\"test_data\":\"testcase2data\"}]}";
    String updateInput = "{\"TemplateName\":\"ARTIFACT_1\",\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase2data\"},{\"event_id\" : \"testcaseid3\", \"test_data\" : \"testcase3data\"}]}";
    static String condition = "{\"_id\" : \"eventId\"}";
    static String event = "{\"meta\":{\"id\":\"eventId\"}}";

    public static void setUpEmbeddedMongo() throws Exception {
         testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
         mongoClient = testsFactory.newMongo();
    }

    @BeforeClass
    public static void init() throws Exception
    {
        setUpEmbeddedMongo();
        mongoDBHandler.setMongoClient(mongoClient);
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
        assertTrue(objHandler.insertObject(input, rulesObject, event, null));
    }

    @Test
    public void test() {
        String document = objHandler.findObjectById("eventId");
        JsonNode result = objHandler.getAggregatedObject(document);

        assertEquals(input, result.toString());
    }

    @AfterClass
    public static void dropCollection()
    {
        mongoDBHandler.dropDocument(dataBaseName, collectionName, condition);
    }
}
