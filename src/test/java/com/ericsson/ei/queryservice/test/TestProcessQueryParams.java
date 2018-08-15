package com.ericsson.ei.queryservice.test;

import com.ericsson.ei.App;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.qpid.util.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        App.class,
        EmbeddedMongoAutoConfiguration.class
})
public class TestProcessQueryParams {

    private static final String inputPath = "src/test/resources/AggregatedObject.json";
    private static final String REQUEST = "{\"criteria\":{\"testCaseExecutions.testCase.verdict\":\"PASSED\"}}";
    private static final String QUERY_WITH_CRITERIA_AND_OPTIONS = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\"} }";
    private static final String QUERY_WITH_CRITERIA = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }}";
    private static final String QUERY_WITH_WRONG_FORMAT = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43\" }";
    private static final String DATA_BASE_NAME = "eiffel_intelligence";
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
        int port = SocketUtils.findAvailableTcpPort();
        System.setProperty("spring.data.mongodb.port", "" + port);
        expected = new JSONArray("[" + input + "]");
    }

    @Test
    public void testFilterFormParam() throws IOException {
        JsonNode criteria = mapper.readTree(QUERY_WITH_CRITERIA_AND_OPTIONS).get("criteria");
        JsonNode options = mapper.readTree(QUERY_WITH_CRITERIA_AND_OPTIONS).get("options");
        String request = "{ \"$and\" : [ " + criteria.toString() + "," + options.toString() + " ] }";
        when(processAggregatedObject.processQueryAggregatedObject(request, DATA_BASE_NAME, AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
        JSONArray result = processQueryParams.filterFormParam(new ObjectMapper().readTree(QUERY_WITH_CRITERIA_AND_OPTIONS));
        assertEquals(expected, result);
    }

    @Test
    public void testFilterFormParamWithOnlyCriteria() throws IOException {
        JsonNode criteria = mapper.readTree(QUERY_WITH_CRITERIA).get("criteria");
        when(processAggregatedObject.processQueryAggregatedObject(criteria.toString(), DATA_BASE_NAME, AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
        JSONArray result = processQueryParams.filterFormParam(new ObjectMapper().readTree(QUERY_WITH_CRITERIA));
        assertEquals(expected, result);
    }

    @Test(expected = JsonEOFException.class)
    public void testFilterFormParamWithWrongFormat() throws IOException {
        JSONArray result = processQueryParams.filterFormParam(new ObjectMapper().readTree(QUERY_WITH_WRONG_FORMAT));
        assertEquals(expected, result);
    }

    @Test
    public void testFilterQueryParam() throws IOException {
        JsonNode criteria = mapper.readValue(REQUEST, JsonNode.class).get("criteria");
        when(processAggregatedObject.processQueryAggregatedObject(criteria.toString(), DATA_BASE_NAME, AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
        JSONArray result = processQueryParams.filterQueryParam(REQUEST);
        assertEquals(expected, result);
    }
}