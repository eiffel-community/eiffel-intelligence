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

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.exception.PropertyNotFoundException;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.eiffelcommons.utils.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class UpStreamEventsHandler.
 */
@Component
public class UpStreamEventsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpStreamEventsHandler.class);

    @Autowired
    private ERQueryService eventRepositoryQueryService;
    @Autowired
    private HistoryExtractionHandler historyExtractionHandler;
    @Autowired
    private RulesHandler rulesHandler;

    public void upstreamEventsHandler() throws URISyntaxException {
        eventRepositoryQueryService = new ERQueryService();
    }

    // setters used for injecting mocks
    public void setEventRepositoryQueryService(final ERQueryService eventRepositoryQueryService) {
        this.eventRepositoryQueryService = eventRepositoryQueryService;
    }

    public void setHistoryExtractionHandler(final HistoryExtractionHandler historyExtractionHandler) {
        this.historyExtractionHandler = historyExtractionHandler;
    }

    /**
     * Run history extraction rules on all upstream events.
     *
     * @param aggregatedObjectId
     *                               the aggregated object id
     * @throws Exception 
     * @throws PropertyNotFoundException 
     */
    public void runHistoryExtractionRulesOnAllUpstreamEvents(String aggregatedObjectId) throws PropertyNotFoundException, Exception {

        // Use aggregatedObjectId as eventId since they are the same for start
        // events.
        long start = System.currentTimeMillis();
        final ResponseEntity responseEntity = eventRepositoryQueryService
                .getEventStreamDataById(aggregatedObjectId, SearchOption.UP_STREAM, -1, -1, true);
        
        long stop = System.currentTimeMillis();
        LOGGER.debug("%%%% Response time for upstream query for id: {}: {} ", aggregatedObjectId, stop-start);

        final String searchResultString = responseEntity.getBody();
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode searchResult = mapper.readTree(searchResultString);

        if (searchResult == null) {
            LOGGER.warn("Asked for upstream from {} but got null result back!", aggregatedObjectId);
            return;
        }

        final JsonNode upstreamLinkObjects = searchResult.get("upstreamLinkObjects");

        if (upstreamLinkObjects == null) {
            LOGGER.warn("Asked for upstream from {} but got null result back!", aggregatedObjectId);
            return;
        }

        if (!upstreamLinkObjects.isArray()) {
            LOGGER.warn("Expected upstreamLinkObjects to be an array but is: {}", upstreamLinkObjects.getNodeType());
        }

        // apply history extract rules on each node in the tree
        traverseTree(upstreamLinkObjects, aggregatedObjectId, "");
    }

    /**
     * Traverses the tree from ER. The tree is defined as an array of either an
     * event or a list of events. E.g:
     *
     * <pre>
     *      [A, [B, C, [D, E]], [F, [N, [G, H]]], [I, [J, [K, L, [M]]]]]
     * </pre>
     *
     * Where the corresponding tree looks like this:
     *
     * <pre>
     * A -> B -> C -> D -> E -> F -> N -> G -> H -> I -> J -> K -> L -> M
     * </pre>
     *
     * @param jsonArray
     *                                   the array to traverse
     * @param aggregatedObjectId
     *                                   the id of the aggregated object
     * @param pathInAggregatedObject
     *                                   the current path in the aggregated object
     */
    private void traverseTree(final JsonNode jsonArray, final String aggregatedObjectId,
            final String pathInAggregatedObject) {

        String np = pathInAggregatedObject;
        final JsonNode parent = jsonArray.get(0);
        if (parent != null) {
            JsonNode parentId = parent.at("/meta/id");
            if (!aggregatedObjectId.equals(parentId.textValue())) {
                // parent event is not the same as the starting event so we can
                // start collecting history
                RulesObject rules = rulesHandler.getRulesForEvent(parent.toString());

                if (rules != null) {
                    np = historyExtractionHandler.runHistoryExtraction(aggregatedObjectId, rules, parent.toString(), pathInAggregatedObject);
                }
            }
        }

        String prevNp = null;
        for (int i = 1; i < jsonArray.size(); i++) {
            if (jsonArray.get(i).isObject()) {
                String event = jsonArray.get(i).toString();
                RulesObject rules = rulesHandler.getRulesForEvent(event);

                if (rules != null) {
                    np = historyExtractionHandler.runHistoryExtraction(aggregatedObjectId, rules, event, pathInAggregatedObject);
                }
            } else {
                // if we have prevNp then we should use that because it is the
                // "parent" of the list we are now going to
                // traverse. But if we don't have it, use the new path from the
                // parent node.
                traverseTree(jsonArray.get(i), aggregatedObjectId, prevNp != null ? prevNp : np);
            }
        }
    }
}
