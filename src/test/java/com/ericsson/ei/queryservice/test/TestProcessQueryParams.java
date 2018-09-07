package com.ericsson.ei.queryservice.test;

import com.ericsson.ei.App;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.ericsson.ei.queryservice.ProcessQueryParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.qpid.util.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import static org.junit.Assert.fail;
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
    private static final String QUERY_WITH_UNIVERSAL_OBJECT_NAME = "{\"criteria\" :{\"object.testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"object.gav.groupId\": \"com.mycompany.myproduct\"} }";
    private static final String QUERY_WITH_CONFIGURED_OBJECT_NAME = "{\"criteria\" :{\"aggregatedObject.testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"aggregatedObject.gav.groupId\": \"com.mycompany.myproduct\"} }";
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
        try {
            JSONObject query = new JSONObject(QUERY_WITH_CRITERIA_AND_OPTIONS);
            JSONObject criteria = (JSONObject) query.get("criteria");
            JSONObject options = (JSONObject) query.get("options");
            
            

            String request = "{ \"$and\" : [ " + criteria.toString() + "," + options.toString() + " ] }";
            when(processAggregatedObject.processQueryAggregatedObject(
                    request, DATA_BASE_NAME, AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
            JSONArray result = processQueryParams.filterFormParam(criteria, options);
            assertEquals(expected, result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFilterFormParamWithOnlyCriteria() throws IOException {
        try {
            JSONObject queryBody = new JSONObject(QUERY_WITH_CRITERIA);
            JSONObject criteria = (JSONObject) queryBody.get("criteria");
            JSONObject options = null;

            when(processAggregatedObject.processQueryAggregatedObject(
                    criteria.toString(), DATA_BASE_NAME, AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
            JSONArray result = processQueryParams.filterFormParam(criteria, options);
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
             JSONObject queryConf = new JSONObject(QUERY_WITH_CONFIGURED_OBJECT_NAME);
             JSONObject criteriaConf = (JSONObject) queryConf.get("criteria");
             JSONObject optionsConf = (JSONObject) queryConf.get("options");
             

             String request = "{ \"$and\" : [ " + criteriaConf.toString() + "," + optionsConf.toString() + " ] }";
             when(processAggregatedObject.processQueryAggregatedObject(
                     request, DATA_BASE_NAME, AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
             JSONArray result = processQueryParams.filterFormParam(criteria, options);
             assertEquals(expected, result);
         } catch (Exception e) {
             fail(e.getMessage());
         }
    }

    @Test
    public void testFilterQueryParam() throws IOException {
        JsonNode criteria = mapper.readValue(REQUEST, JsonNode.class).get("criteria");
        when(processAggregatedObject.processQueryAggregatedObject(
                criteria.toString(), DATA_BASE_NAME, AGGREGATION_COLLECTION_NAME)).thenReturn(expected);
        JSONArray result = processQueryParams.filterQueryParam(REQUEST);
        assertEquals(expected, result);
    }
    
    
}
