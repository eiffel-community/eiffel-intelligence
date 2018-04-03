package com.ericsson.ei.rules.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.services.IRuleCheckService;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestRulesService {

    @Autowired
    IRuleCheckService ruleCheckService;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private static MongodForTestsFactory testsFactory;

    static MongoClient mongoClient = null;

    private final String events = "src/test/resources/AggregateListEvents.json";
    private final String rules = "src/test/resources/AggregateListRules.json";
    private final String aggregateResultObject = "src/test/resources/AggregateResultObject.json";

    @BeforeClass
    public static void setMongoDB() throws IOException, JSONException {
        testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
        mongoClient = testsFactory.newMongo();
    }

    @PostConstruct
    public void initMocks() {
        mongoDBHandler.setMongoClient(mongoClient);
    }

    @AfterClass
    public static void tearDownMongoDB() throws Exception {
        testsFactory.shutdown();
        mongoClient.close();
    }

    @Test
    public void prepareAggregatedObject() {
        String jsonInput = null;
        String extractionRules_test = null;
        String aggregatedResult = null;
        JSONArray expectedAggObject = null;
        try {
            jsonInput = FileUtils.readFileToString(new File(events));
            extractionRules_test = FileUtils.readFileToString(new File(rules));
            aggregatedResult = FileUtils.readFileToString(new File(aggregateResultObject));
            expectedAggObject = new JSONArray(aggregatedResult);
            String result = ruleCheckService.prepareAggregatedObject(extractionRules_test, jsonInput);
            JSONArray actualAggObject = new JSONArray(result);
            assertEquals(expectedAggObject.toString(), actualAggObject.toString());
        } catch (Exception e) {
        }

    }
}
