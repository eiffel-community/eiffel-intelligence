package com.ericsson.ei.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.ericsson.ei.erqueryservice.ERQueryService;

import com.fasterxml.jackson.databind.JsonNode;


@Component
public class UpStreamEventsHandler {

    @Autowired private ERQueryService eventRepositoryQueryService;
    @Autowired private HistoryExtractionHandler historyExtractionHandler;

    static Logger log = (Logger) LoggerFactory.getLogger(UpStreamEventsHandler.class);

    public void runHistoryExtractionRulesOnAllUpstreamEvents(String aggregatedObjectId, RulesObject rulesObject) {
        ResponseEntity upStreamEventsString;

        if (rulesObject.isNeedHistoryRule() && rulesObject.isStartEventRules()) {
            // Use aggregatedObjectId as eventId since they are the same (for start events only)
            upStreamEventsString = eventRepositoryQueryService.getEventStreamDataById(aggregatedObjectId, eventRepositoryQueryService.UPSTREAM, -1, -1, true);
        } else {
            return;
        }

        upStreamEventsString.toString();

        traversHistoryTree();

        String pathInAggregatedObject = null;

        //TODO: loop each upStreamEvent {
            String upStreamEvent = "Placeholder event";
            historyExtractionHandler.runHistoryExtraction(aggregatedObjectId, rulesObject, upStreamEvent, pathInAggregatedObject);
        //}

        return;
    }


    private void traversHistoryTree() {
        return;
    }


    private void parseUpStreamEventsString(String upStreamEventsString) {
        // Return a tree-structure containing events events
        return;
    }


//    public void EventStreamParser(String eventStream) {
//
//    }
}
