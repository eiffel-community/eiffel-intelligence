package com.ericsson.ei.queryservice.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.encryption.Encryptor;
import com.ericsson.ei.mongo.MongoQuery;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.ericsson.ei.utils.TestContextInitializer;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: TestProcessQueryParams",
        "failed.notifications.collection.name: QueryServiceRESTAPITest-failedNotifications",
        "rabbitmq.exchange.name: TestProcessQueryParams-exchange",
        "rabbitmq.queue.suffix: TestProcessQueryParams" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class TestProcessQueryParams {

    private static final String inputPath = "src/test/resources/AggregatedObject.json";
    private static final String QUERY_WITH_CRITERIA_AND_OPTIONS = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"} }";
    private static final String QUERY_WITH_CRITERIA = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }}";
    private static final String DATA_BASE_NAME = "TestProcessQueryParams";
    private static JSONArray expected;

    @Value("${aggregations.collection.name}")
    private String aggregationsCollection;

    @Autowired
    private ProcessQueryParams processQueryParams;

    @MockBean
    private ProcessAggregatedObject processAggregatedObject;
    
    @MockBean
    private Encryptor encryptor;

    @BeforeClass
    public static void setUp() throws JSONException {
        String input = FileUtils.readFileAsString(new File(inputPath));
        expected = new JSONArray("[" + input + "]");
    }

    @Test
    public void testRunQuery() throws IOException {
        try {
            JSONObject query = new JSONObject(QUERY_WITH_CRITERIA_AND_OPTIONS);
            JSONObject criteria = (JSONObject) query.get("criteria");
            JSONObject options = (JSONObject) query.get("options");
            String filter = null;

            String requestString = "{\"$and\":[" + criteria.toString() + ","
                    + options.toString() + "]}";
            ArgumentMatcher<MongoQuery> requestStringMatches = mQ -> mQ.toString()
                                                                       .equals(requestString);
            when(processAggregatedObject.processQueryAggregatedObject(
                    ArgumentMatchers.argThat(requestStringMatches),
                    eq(DATA_BASE_NAME),
                    eq(aggregationsCollection))).thenReturn(expected);
            JSONArray result = processQueryParams.runQuery(criteria, options, filter);
            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRunQueryWithOnlyCriteria() throws IOException {
        try {
            JSONObject query = new JSONObject(QUERY_WITH_CRITERIA);
            JSONObject criteria = (JSONObject) query.get("criteria");
            JSONObject options = null;
            String filter = null;
            String criteriaString = criteria.toString();
            ArgumentMatcher<MongoQuery> requestStringMatches = mQ -> mQ.toString()
                                                                       .equals(criteriaString);
            when(processAggregatedObject.processQueryAggregatedObject(
                    ArgumentMatchers.argThat(requestStringMatches),
                    eq(DATA_BASE_NAME),
                    eq(aggregationsCollection))).thenReturn(expected);
            JSONArray result = processQueryParams.runQuery(criteria, options, filter);
            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRunQueryWithFilter() {
        JSONObject query = new JSONObject(QUERY_WITH_CRITERIA);
        JSONObject criteria = (JSONObject) query.get("criteria");
        JSONObject options = null;
        String filter = "{time:time, type: type}";

        when(processAggregatedObject.processQueryAggregatedObject(any(), any(), any())).thenReturn(
                expected);

        JSONArray result = processQueryParams.runQuery(criteria, options, filter);

        JSONArray expectedFilterResult = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
                "{\"time\":1481875891763,\"type\":\"ARTIFACT_1\"}");
        expectedFilterResult.put(jsonObject);

        /*
         * Comparing string values as we don't build up the structure here in the test as in
         * filterResult
         */
        assertEquals(expectedFilterResult.toString(), result.toString());
    }
}
