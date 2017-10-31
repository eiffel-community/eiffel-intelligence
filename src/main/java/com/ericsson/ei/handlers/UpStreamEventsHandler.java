package com.ericsson.ei.handlers;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.erqueryservice.ERQueryService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class UpStreamEventsHandler {

    @Autowired private ERQueryService eventRepositoryQueryService;
    @Autowired private HistoryExtractionHandler historyExtractionHandler;

    static Logger log = (Logger) LoggerFactory.getLogger(UpStreamEventsHandler.class);

    public void runHistoryExtractionRulesOnAllUpstreamEvents(String aggregatedObjectId, RulesObject rulesObject) {
        String upStreamEventsString;

        if (rulesObject.isNeedHistoryRule() && rulesObject.isStartEventRules()) {

            // Use aggregatedObjectId as eventId since they are the same for start events.
            upStreamEventsString = eventRepositoryQueryService.getEventStreamDataById(aggregatedObjectId, eventRepositoryQueryService.UPSTREAM, -1, -1, true).toString();

            TreeNode<JsonNode> upStreamEventsTree = parseUpStreamEventsString(upStreamEventsString);
            String pathInAggregatedObject = "";

            traverseTree(upStreamEventsTree, aggregatedObjectId, rulesObject, pathInAggregatedObject);

            return;

        } else {
            return;
        }
    }


    public void traverseTree(TreeNode<JsonNode> node, String aggregatedObjectId, RulesObject rulesObject, String pathInAggregatedObject){
        JsonNode historicEvent = node.getData();
        String newPathInAggregatedObject = historyExtractionHandler.runHistoryExtraction(aggregatedObjectId, rulesObject, historicEvent.toString(), pathInAggregatedObject);

        for(TreeNode<JsonNode> each : node.getChildren()){
            String updatedPathInAggregatedObject = UpdatedPathInAggregatedObject(pathInAggregatedObject, newPathInAggregatedObject);
            traverseTree(each, aggregatedObjectId, rulesObject, updatedPathInAggregatedObject);
        }
        return;
    }

    private String UpdatedPathInAggregatedObject(String oldPathInAggregatedObject, String newPathInAggregatedObject) {
        return oldPathInAggregatedObject + " " + newPathInAggregatedObject;
    }

    private TreeNode<JsonNode> parseUpStreamEventsString(String upStreamEventsString) {
        JsonNode upStreamEventsJson = stringToJsonNode(upStreamEventsString);

        // Get the correct value
        Iterator upStreamIterator = upStreamEventsJson.elements();
        JsonNode objectTree = (JsonNode) upStreamIterator.next();

        TreeNode<JsonNode> tree = null;

        tree = new TreeNode<JsonNode>(objectTree.get(0));
        JsonNode theRest = objectTree.get(1);
        tree = appendToTree(tree, theRest);

        // Return a tree-structure containing events
        return tree;

    }

    private TreeNode<JsonNode> appendToTree( TreeNode<JsonNode> parent, JsonNode theRest) {
        TreeNode<JsonNode> last = parent;
        for( JsonNode each : theRest) {
            if (each.isArray()) {
                appendToTree(last, each);
            } else {
                last = new TreeNode<JsonNode>(each);
                parent.addChild(last);
            }
        }
        return parent;
    }

    private JsonNode stringToJsonNode(String jsonString) {
        JsonNode json = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.readValue(jsonString, JsonNode.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return json;
    }



}
