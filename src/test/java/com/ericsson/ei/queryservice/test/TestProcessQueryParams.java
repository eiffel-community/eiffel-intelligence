package com.ericsson.ei.queryservice.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.qpid.server.util.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.handlers.MongoQuery;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.ericsson.ei.utils.TestContextInitializer;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: TestProcessQueryParams",
        "failed.notification.database-name: QueryServiceRESTAPITest-failedNotifications",
        "rabbitmq.exchange.name: TestProcessQueryParams-exchange",
        "rabbitmq.consumerName: TestProcessQueryParams" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class TestProcessQueryParams {

    private static final String inputPath = "src/test/resources/AggregatedObject.json";
    private static final String QUERY_WITH_CRITERIA_AND_OPTIONS = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"} }";
    private static final String QUERY_WITH_CRITERIA = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }}";
    private static final String DATA_BASE_NAME = "TestProcessQueryParams";
    private static final String AGGREGATION_COLLECTION_NAME = "aggregated_objects";
    private static JSONArray expected;

    @Autowired
    private ProcessQueryParams processQueryParams;

    @MockBean
    private ProcessAggregatedObject processAggregatedObject;

    @BeforeClass
    public static void setUp() throws JSONException {
        String input = FileUtils.readFileAsString(new File(inputPath));
        expected = new JSONArray("[" + input + "]");
    }

    @Test
    public void testFilterFormParam() throws IOException {
        try {
            JSONObject query = new JSONObject(QUERY_WITH_CRITERIA_AND_OPTIONS);
            JSONObject criteria = (JSONObject) query.get("criteria");
            JSONObject options = (JSONObject) query.get("options");
            String filter = null;

            String requestString = "{ \"$and\" : [ " + criteria.toString() + ","
                    + options.toString() + " ] }";
            ArgumentMatcher<MongoQuery> requestStringMatches =  mQ -> mQ.toString().equals(requestString);
            when(processAggregatedObject.processQueryAggregatedObject(
                    ArgumentMatchers.argThat(requestStringMatches),
                    eq(DATA_BASE_NAME),
                    eq(AGGREGATION_COLLECTION_NAME))).thenReturn(expected);
            JSONArray result = processQueryParams.runQuery(criteria, options, filter);
            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFilterFormParamWithOnlyCriteria() throws IOException {
        try {
            JSONObject query = new JSONObject(QUERY_WITH_CRITERIA);
            JSONObject criteria = (JSONObject) query.get("criteria");
            JSONObject options = null;
            String filter = null;
            String criteriaString = criteria.toString();
            ArgumentMatcher<MongoQuery> requestStringMatches = mQ -> mQ.toString().equals(criteriaString);
            when(processAggregatedObject.processQueryAggregatedObject(
                    ArgumentMatchers.argThat(requestStringMatches),
                    eq(DATA_BASE_NAME),
                    eq(AGGREGATION_COLLECTION_NAME))).thenReturn(expected);
            JSONArray result = processQueryParams.runQuery(criteria, options, filter);
            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    // TODO: emalinn - Create a test to test the filter

}
