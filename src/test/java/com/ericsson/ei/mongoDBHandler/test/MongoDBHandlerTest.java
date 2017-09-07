package com.ericsson.ei.mongoDBHandler.test;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.AfterClass;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MongoDBHandlerTest {

    static Logger log = (Logger) LoggerFactory.getLogger(MongoDBHandlerTest.class);

    @Autowired
    static MongoDBHandler mongoDBHandler;

    private static MongodForTestsFactory testsFactory;
    static MongoClient mongoClient = null;

    static String dataBaseName = "EventStorageDBbbb";
    static String collectionName = "SampleEvents";
    static String input = "{\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase1data\"},{\"event_id\" : \"testcaseid2\", \"test_data\" : \"testcase2data\"}]}";
    String updateInput = "{\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase2data\"},{\"event_id\" : \"testcaseid3\", \"test_data\" : \"testcase3data\"}]}";
    static String condition = "{\"test_cases.event_id\" : \"testcaseid1\"}";

    public static void setUpEmbeddedMongo() throws Exception {
        testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
        mongoClient = testsFactory.newMongo();
    }

    @BeforeClass
    public static void init() throws Exception
    {
        setUpEmbeddedMongo();
        mongoDBHandler = new MongoDBHandler();
        mongoDBHandler.setMongoClient(mongoClient);
        assertTrue(mongoDBHandler.insertDocument(dataBaseName, collectionName, input));
    }

    @Test
    public void testGetDocuments(){
        ArrayList<String> documents = mongoDBHandler.getAllDocuments(dataBaseName, collectionName);
        assertTrue(documents.size() > 0);
    }

    @Test
    public void testGetDocumentsOnCondition(){
        ArrayList<String> documents = mongoDBHandler.find(dataBaseName, collectionName, condition);
        assertTrue(documents.size() > 0);
    }

//    @Test
//    TODO fix this test case
//    public void testGetDocumentOnCondition(){
//        ArrayList<String> documents = mongoDBHandler.getDocumentsOnCondition(dataBaseName, collectionName, condition);
//        String document = documents.get(0);
//        assertEquals(document, input);
//    }

    @Test
    public void testUpdateDocument(){
        assertTrue(mongoDBHandler.updateDocument(dataBaseName, collectionName, input, updateInput));
    }

    @AfterClass
    public static void dropCollection()
    {
        assertTrue(mongoDBHandler.dropDocument(dataBaseName, collectionName, condition));
        testsFactory.shutdown();
        mongoClient.close();
    }
}
