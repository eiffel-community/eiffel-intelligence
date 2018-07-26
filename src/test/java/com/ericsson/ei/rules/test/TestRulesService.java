package com.ericsson.ei.rules.test;

import com.ericsson.ei.App;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.services.IRuleCheckService;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {App.class})
public class TestRulesService {
    private final static Logger LOGGER = (Logger) LoggerFactory.getLogger(TestRulesService.class);

    private static final String EVENTS = "src/test/resources/AggregateListEvents.json";
    private static final String RULES = "src/test/resources/AggregateListRules.json";
    private static final String AGGREGATED_RESULT_OBJECT = "src/test/resources/AggregateResultObject.json";

    @Autowired
    private IRuleCheckService ruleCheckService;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Test
    public void prepareAggregatedObject() throws IOException, JSONException {
        String jsonInput;
        String extractionRules_test;
        String aggregatedResult;
        JSONArray expectedAggObject;
        jsonInput = FileUtils.readFileToString(new File(EVENTS), "UTF-8");
        extractionRules_test = FileUtils.readFileToString(new File(RULES), "UTF-8");
        aggregatedResult = FileUtils.readFileToString(new File(AGGREGATED_RESULT_OBJECT), "UTF-8");
        expectedAggObject = new JSONArray(aggregatedResult);
        String result = ruleCheckService.prepareAggregatedObject(new JSONArray(extractionRules_test), new JSONArray(jsonInput));
        JSONArray actualAggObject = new JSONArray(result);
        assertEquals(expectedAggObject.toString(), actualAggObject.toString());
    }
}