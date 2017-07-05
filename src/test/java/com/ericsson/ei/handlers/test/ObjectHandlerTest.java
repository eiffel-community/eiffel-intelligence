package com.ericsson.ei.handlers.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.rules.test.TestRulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectHandlerTest {

    static Logger log = (Logger) LoggerFactory.getLogger(ObjectHandlerTest.class);

    static ObjectHandler objHandler = new ObjectHandler();

    static MongoDBHandler mongoDBHandler = new MongoDBHandler();
    static JmesPathInterface jmesPathInterface = new JmesPathInterface();

    static private RulesObject rulesObject;
    static private final String inputFilePath = "src/test/resources/RulesHandlerOutput2.json";
    static private JsonNode rulesJson;

    static String host = "localhost";
    static int port = 27017;

    static String dataBaseName = "EventStorageDBbbb";
    static String collectionName = "SampleEvents";
    static String input = "{\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase1data\"},{\"event_id\" : \"testcaseid2\", \"test_data\" : \"testcase2data\"}]}";
    String updateInput = "{\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase2data\"},{\"event_id\" : \"testcaseid3\", \"test_data\" : \"testcase3data\"}]}";
    static String condition = "{\"_id\" : \"eventId\"}";
    static String event = "{\"meta\":{\"id\":\"eventId\"}}";

    @BeforeClass
    public static void init()
    {
        mongoDBHandler.createConnection(host,port);
        objHandler.setMongoDbHandler(mongoDBHandler);
        objHandler.setJmespathInterface(jmesPathInterface);
        objHandler.setCollectionName(collectionName);
        objHandler.setDatabaseName(dataBaseName);

        try {
            String rulesString = FileUtils.readFileToString(new File(inputFilePath));
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
        assertEquals(input, document);

    }

    @AfterClass
    public static void dropCollection()
    {
        mongoDBHandler.dropDocument(dataBaseName, collectionName, condition);
    }
}
