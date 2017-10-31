package com.ericsson.ei.handlers.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.http.ResponseEntity;

import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.handlers.HistoryExtractionHandler;
import com.ericsson.ei.handlers.TreeNode;
import com.ericsson.ei.handlers.UpStreamEventsHandler;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@RunWith(SpringJUnit4ClassRunner.class)
public class UpStreamEventHandlerTest {

    private UpStreamEventsHandler classUnderTest;

    static Logger log = (Logger) LoggerFactory.getLogger(UpStreamEventHandlerTest.class);

    //@Autowired private ERQueryService eventRepositoryQueryService;
    @Autowired private HistoryExtractionHandler historyExtractionHandler;

    @Test
    public void testRunHistoryExtractionRulesOnAllUpstreamEvents() {


        String upStreamString =
                "{\"upstreamLinkObjects\":[\n"
                    + "\t\t{\"_id\":\"event1_level_1\"},[\n"
                        + "\t\t\t{\"_id\":\"event2_level_2\"},\n"
                        + "\t\t\t{\"_id\":\"event3_level_2\"},[\n"
                            + "\t\t\t\t{\"_id\":\"event4_level_3\"},[\n"
                                + "\t\t\t\t\t{\"_id\":\"event5_level_4\"}],\n"
                            + "\t\t\t\t{\"_id\":\"event6_level_3\"}],\n"
                        + "\t\t\t{\"_id\":\"event7_level_2\"},\n"
                        + "\t\t\t{\"_id\":\"event8_level_2\"}]]}\n";

        RulesObject rulesObject = null;
        ERQueryService mockedERQueryService = mock(ERQueryService.class);
        String aggregatedObjectId = "0123456789abcdef";
        when(mockedERQueryService.getEventStreamDataById(aggregatedObjectId, mockedERQueryService.UPSTREAM, -1, -1, true)).thenReturn((ResponseEntity)upStreamString);

        classUnderTest.runHistoryExtractionRulesOnAllUpstreamEvents("0123456789abcdef", rulesObject);

        assertEquals("0", "1");
    }

    @Test
    public void testTraverseTree() {



//        ERQueryService mockedERQueryService = mock(ERQueryService.class);
//        when(mockedERQueryService.getEventSteamDataById(eventId, mockedERQueryService.UPSTREAM, -1, -1, true)).thenReturn(upStreamString);
//
////      TreeNode<JsonNode> event2_level_2 = new TreeNode<JsonNode>(Utils.stringToJsonNode("{\"_id\":\"event2_level_2\"}"));
////      TreeNode<JsonNode> event3_level_2 = new TreeNode<JsonNode>(Utils.stringToJsonNode("{\"_id\":\"event3_level_2\"}"));
////      TreeNode<JsonNode> event4_level_3 = new TreeNode<JsonNode>(Utils.stringToJsonNode("{\"_id\":\"event4_level_3\"}"));
////      TreeNode<JsonNode> event5_level_4 = new TreeNode<JsonNode>(Utils.stringToJsonNode("{\"_id\":\"event5_level_4\"}"));
////      TreeNode<JsonNode> event6_level_3 = new TreeNode<JsonNode>(Utils.stringToJsonNode("{\"_id\":\"event6_level_3\"}"));
////      TreeNode<JsonNode> event7_level_2 = new TreeNode<JsonNode>(Utils.stringToJsonNode("{\"_id\":\"event7_level_2\"}"));
////      TreeNode<JsonNode> event8_level_2 = new TreeNode<JsonNode>(Utils.stringToJsonNode("{\"_id\":\"event8_level_2\"}"));
////
//
//
//        public void traverseTree(TreeNode<JsonNode> node, String aggregatedObjectId, RulesObject rulesObject, String pathInAggregatedObject){
//
//        classUnderTest.traverseTree(aggregatedObjectId, rulesObject);

        assertEquals("0", "1");
    }

}
