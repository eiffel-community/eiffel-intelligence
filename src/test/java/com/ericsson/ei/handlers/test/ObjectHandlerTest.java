package com.ericsson.ei.handlers.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;

import java.io.File;

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

import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.rules.test.TestRulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class ObjectHandlerTest {

    static Logger log = (Logger) LoggerFactory.getLogger(ObjectHandlerTest.class);

    static ObjectHandler objHandler = new ObjectHandler();
    static MongodExecutable mongodExecutable = null;
    static MongoDBHandler mongoDBHandler = null;
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

    public static void setUpEmbeddedMongo() throws Exception {
        System.setProperty("mongodb.port", ""+port);

        MongodStarter starter = MongodStarter.getDefaultInstance();

        String bindIp = "localhost";

        IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(bindIp, port, Network.localhostIsIPv6()))
            .build();


        try {
            mongodExecutable = starter.prepare(mongodConfig);
            MongodProcess mongod = mongodExecutable.start();
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @BeforeClass
    public static void init() throws Exception
    {
        setUpEmbeddedMongo();
        mongoDBHandler = new MongoDBHandler();
        mongoDBHandler.createConnection(host,port);
        EventToObjectMapHandler eventToObjectMapHandler = mock(EventToObjectMapHandler.class);
        objHandler.setEventToObjectMap(eventToObjectMapHandler);
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
        JsonNode result = objHandler.getAggregatedObject(document);
        assertEquals(input, result.asText());
    }

    @AfterClass
    public static void dropCollection()
    {
        mongoDBHandler.dropDocument(dataBaseName, collectionName, condition);

        if (mongodExecutable != null)
            mongodExecutable.stop();
    }
}
