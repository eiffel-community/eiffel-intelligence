package com.ericsson.ei.queryservice.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.ericsson.ei.utils.TestContextInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: TestProcessQueryParams",
        "missedNotificationDataBaseName: QueryServiceRESTAPITest-missedNotifications",
        "rabbitmq.exchange.name: TestProcessQueryParams-exchange",
        "rabbitmq.consumerName: TestProcessQueryParams" })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class TestProcessQueryParams {

    private static final String inputPath = "src/test/resources/AggregatedObject.json";
    private static final String REQUEST = "{\"criteria\":{\"testCaseExecutions.testCase.verdict\":\"PASSED\"}}";
    private static final String QUERY_WITH_CRITERIA_AND_OPTIONS = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"} }";
    private static final String QUERY_WITH_CRITERIA = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }}";
    private static final String QUERY_WITH_UNIVERSAL_OBJECT_NAME = "{\"criteria\" :{\"object.testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"object.identity\": \"pkg:maven/com.mycompany.myproduct/artifact-name@1.0.0\"} }";
    private static final String QUERY_WITH_CONFIGURED_OBJECT_NAME = "{\"criteria\" :{\"aggregatedObject.testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"aggregatedObject.identity\": \"pkg:maven/com.mycompany.myproduct/artifact-name@1.0.0\"} }";
    private static final String DATA_BASE_NAME = "TestProcessQueryParams";
    private static final String AGGREGATION_COLLECTION_NAME = "aggregated_objects";
    private static JSONArray expected;
    private ObjectMapper mapper = new ObjectMapper();

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

            String request = "{ \"$and\" : [ " + criteria.toString() + "," + options.toString() + " ] }";
            when(processAggregatedObject.processQueryAggregatedObject(request, DATA_BASE_NAME,
                    AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
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
            when(processAggregatedObject.processQueryAggregatedObject(criteria.toString(), DATA_BASE_NAME,
                    AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
            JSONArray result = processQueryParams.runQuery(criteria, options, filter);
            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFilterFormParamForObjectName() throws IOException {
        try {
            JSONObject query = new JSONObject(QUERY_WITH_UNIVERSAL_OBJECT_NAME);
            JSONObject criteria = (JSONObject) query.get("criteria");
            JSONObject options = (JSONObject) query.get("options");
            String filter = null;
            JSONObject queryConf = new JSONObject(QUERY_WITH_CONFIGURED_OBJECT_NAME);
            JSONObject criteriaConf = (JSONObject) queryConf.get("criteria");
            JSONObject optionsConf = (JSONObject) queryConf.get("options");

            String request = "{ \"$and\" : [ " + criteriaConf.toString() + "," + optionsConf.toString() + " ] }";
            when(processAggregatedObject.processQueryAggregatedObject(request, DATA_BASE_NAME,
                    AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
            JSONArray result = processQueryParams.runQuery(criteria, options, filter);
            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFilterQueryParam() throws IOException {
        JsonNode criteria = mapper.readValue(REQUEST, JsonNode.class).get("criteria");
        when(processAggregatedObject.processQueryAggregatedObject(criteria.toString(), DATA_BASE_NAME,
                AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
        JSONArray result = processQueryParams.filterQueryParam(REQUEST);
        assertEquals(expected, result);
    }

}
