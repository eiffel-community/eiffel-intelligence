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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.services.IRuleCheckService;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestRulesService {
    private static final String EVENTS = "src/test/resources/AggregateListEvents.json";
    private static final String RULES = "src/test/resources/AggregateListRules.json";
    private static final String AGGREGATED_RESULT_OBJECT = "src/test/resources/AggregateResultObject.json";

    @Autowired
    private IRuleCheckService ruleCheckService;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private static MongodForTestsFactory testsFactory;
    private static MongoClient mongoClient = null;

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
        String jsonInput;
        String extractionRules_test;
        String aggregatedResult;
        JSONArray expectedAggObject;
        try {
            jsonInput = FileUtils.readFileToString(new File(EVENTS), "UTF-8");
            extractionRules_test = FileUtils.readFileToString(new File(RULES), "UTF-8");
            aggregatedResult = FileUtils.readFileToString(new File(AGGREGATED_RESULT_OBJECT), "UTF-8");
            expectedAggObject = new JSONArray(aggregatedResult);
            String result = ruleCheckService.prepareAggregatedObject(new JSONArray(extractionRules_test), new JSONArray(jsonInput));
            JSONArray actualAggObject = new JSONArray(result);
            assertEquals(expectedAggObject.toString(), actualAggObject.toString());
        } catch (Exception e) {
        }

    }
}
