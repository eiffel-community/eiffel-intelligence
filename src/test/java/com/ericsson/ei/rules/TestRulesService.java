package com.ericsson.ei.rules;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.annotation.PostConstruct;

import com.ericsson.ei.exception.InvalidRulesException;
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
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.services.IRuleTestService;
import com.ericsson.ei.test.utils.TestConfigs;
import com.ericsson.ei.utils.TestContextInitializer;
import com.mongodb.MongoClient;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: TestRulesService",
        "failed.notifications.collection.name: TestRulesService-missedNotifications",
        "rabbitmq.exchange.name: TestRulesService-exchange",
        "rabbitmq.queue.suffix: TestRulesService" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class TestRulesService {
    private static final String EVENTS = "src/test/resources/AggregateListEvents.json";
    private static final String RULES = "src/test/resources/AggregateListRules.json";
    private static final String AGGREGATED_RESULT_OBJECT = "src/test/resources/AggregateResultObject.json";
    private static final String INVALID_RULES = "src/test/resources/InvalidRules.json";

    final static Logger LOGGER = LoggerFactory.getLogger(TestRulesService.class);

    @Autowired
    private IRuleTestService ruleTestService;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    private static MongoClient mongoClient = null;

    @PostConstruct
    public void initMocks() {
        mongoClient = TestConfigs.getMongoClient();
        mongoDBHandler.setMongoClient(mongoClient);
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
            String result = ruleTestService.prepareAggregatedObject(new JSONArray(extractionRules_test),
                    new JSONArray(jsonInput));
            JSONArray actualAggObject = new JSONArray(result);
            assertEquals(expectedAggObject.toString(), actualAggObject.toString());
        } catch (Exception e) {
        }
    }

    @Test(expected = InvalidRulesException.class)
    public void testInvalidRulesThrowsException() throws Throwable {
        String events = FileUtils.readFileToString(new File(EVENTS), "UTF-8");
        String invalidRules = FileUtils.readFileToString(new File(INVALID_RULES),"UTF-8");
        ruleTestService.prepareAggregatedObject(new JSONArray(invalidRules), new JSONArray(events));
    }
}
