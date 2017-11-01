/*
    Copyright 2017 Ericsson AB.
    For a full list of individual contributors, please see the commit history.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

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


// TODO: Auto-generated Javadoc
/**
 * The Class UpStreamEventsHandler.
 */
@Component
public class UpStreamEventsHandler {

    @Autowired private ERQueryService eventRepositoryQueryService;
    @Autowired private HistoryExtractionHandler historyExtractionHandler;

    static Logger log = (Logger) LoggerFactory.getLogger(UpStreamEventsHandler.class);

    /**
     * Run history extraction rules on all upstream events.
     *
     * @param aggregatedObjectId the aggregated object id
     * @param rulesObject the rules object
     */
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


    /**
     * Traverse tree.
     *
     * @param node the node
     * @param aggregatedObjectId the aggregated object id
     * @param rulesObject the rules object
     * @param pathInAggregatedObject the path in aggregated object
     */
    private void traverseTree(TreeNode<JsonNode> node, String aggregatedObjectId, RulesObject rulesObject, String pathInAggregatedObject){
        JsonNode historicEvent = node.getData();
        String newPathInAggregatedObject = historyExtractionHandler.runHistoryExtraction(aggregatedObjectId, rulesObject, historicEvent.toString(), pathInAggregatedObject);

        for(TreeNode<JsonNode> each : node.getChildren()){
            traverseTree(each, aggregatedObjectId, rulesObject, newPathInAggregatedObject);
        }

        return;
    }

    /**
     * Parses the up stream events string.
     *
     * @param upStreamEventsString the up stream events string
     * @return the tree node
     */
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


    /**
     * Append to tree.
     *
     * @param parent the parent
     * @param theRest the the rest
     * @return the tree node
     */
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


    /**
     * String to json node.
     *
     * @param jsonString the json string
     * @return the json node
     */
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
