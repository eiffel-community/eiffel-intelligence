package com.ericsson.ei.rules.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.services.IRuleCheckService;
import com.ericsson.ei.test.utils.TestConfigs;
import com.ericsson.ei.utils.TestContextInitializer;
import com.mongodb.MongoClient;

@TestPropertySource(properties = { "spring.data.mongodb.database: TestRulesService",
        "rabbitmq.exchange.name: TestRulesService-exchange", "rabbitmq.consumerName: TestRulesService" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        App.class
    })
public class TestRulesService {
    private static final String EVENTS = "src/test/resources/AggregateListEvents.json";
    private static final String RULES = "src/test/resources/AggregateListRules.json";
    private static final String AGGREGATED_RESULT_OBJECT = "src/test/resources/AggregateResultObject.json";

    final static Logger LOGGER = LoggerFactory.getLogger(TestRulesService.class);

    @Autowired
    private IRuleCheckService ruleCheckService;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    // private static MongodForTestsFactory testsFactory;
    private static MongoClient mongoClient = null;

    // @BeforeClass
    // public static void setMongoDB() throws IOException, JSONException {
    // try {
    // testsFactory = MongodForTestsFactory.with(Version.V3_4_1);
    // mongoClient = testsFactory.newMongo();
    // String port = "" + mongoClient.getAddress().getPort();
    // System.setProperty("spring.data.mongodb.port", port);
    // } catch (Exception e) {
    // LOGGER.error(e.getMessage(), e);
    // e.printStackTrace();
    // }
    // }

    @PostConstruct
    public void initMocks() {
        mongoClient = TestConfigs.getMongoClient();
        mongoDBHandler.setMongoClient(mongoClient);
    }

    // @AfterClass
    // public static void tearDownMongoDB() throws Exception {
    // mongoClient.close();
    // testsFactory.shutdown();
    // }

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
            String result = ruleCheckService.prepareAggregatedObject(new JSONArray(extractionRules_test),
                    new JSONArray(jsonInput));
            JSONArray actualAggObject = new JSONArray(result);
            assertEquals(expectedAggObject.toString(), actualAggObject.toString());
        } catch (Exception e) {
        }

    }
}
