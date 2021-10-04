package com.ericsson.ei.rules;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.annotation.PostConstruct;

import com.ericsson.ei.exception.InvalidRulesException;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.test.utils.TestConfigs;
import com.ericsson.ei.utils.TestContextInitializer;
//import com.mongodb.MongoClient;
import com.mongodb.client.MongoClient;

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
    public void prepareAggregatedObject() throws Throwable {
        String events = FileUtils.readFileToString(new File(EVENTS), "UTF-8");
        String extractionRules = FileUtils.readFileToString(new File(RULES), "UTF-8");
        String aggregationResult =
                FileUtils.readFileToString(new File(AGGREGATED_RESULT_OBJECT), "UTF-8");
        JSONArray expectedAggregation = new JSONArray(aggregationResult);

        String result = ruleTestService.prepareAggregatedObject(new JSONArray(extractionRules),
                new JSONArray(events));
        JSONArray actualAggregation = new JSONArray(result);
        for(int i=0;i<actualAggregation.length();i++) {
        	JSONObject jsonobject = actualAggregation.getJSONObject(i);
        	jsonobject.remove("Time");
        }
        assertEquals(expectedAggregation.toString(), actualAggregation.toString());
    }

    @Test(expected = InvalidRulesException.class)
    public void testInvalidRulesThrowsException() throws Throwable {
        String events = FileUtils.readFileToString(new File(EVENTS), "UTF-8");
        String invalidRules = FileUtils.readFileToString(new File(INVALID_RULES),"UTF-8");
        ruleTestService.prepareAggregatedObject(new JSONArray(invalidRules), new JSONArray(events));
    }
}
