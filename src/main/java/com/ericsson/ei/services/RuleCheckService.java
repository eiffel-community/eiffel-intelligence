package com.ericsson.ei.services;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.EventHandler;
import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class RuleCheckService implements IRuleCheckService {

    @Autowired
    JmesPathInterface jmesPathInterface;

    @Autowired
    EventHandler eventHandler;

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    @Autowired
    EventToObjectMapHandler eventToObjectMapHandler;

    private static final Logger LOG = LoggerFactory.getLogger(RuleCheckService.class);

    @Override
    public String prepareAggregatedObject(String listRulesJson, String listEventsJson)
            throws JSONException, JsonProcessingException, IOException {
        JSONArray jsonObj = new JSONArray(listRulesJson);
        JSONArray jsonObj2 = new JSONArray(listEventsJson);
        eventHandler.getRulesHandler().setParsedJason(jsonObj.toString());
        String aggregatedObjectId = null;
        // Looping all events and add suffix template name to id and links, For
        // identifying the test aggregated events.
        for (int i = 0; i < jsonObj2.length(); i++) {
            String templateName = jmesPathInterface.runRuleOnEvent("TemplateName", jsonObj.getJSONObject(0).toString())
                    .asText("TEST");
            String addTemplateNameToIds = addTemplateNameToIds(jsonObj2.getJSONObject(i), templateName);
            LOG.info("event to prepare aggregated object :: " + jsonObj2.getJSONObject(i).toString());
            if (aggregatedObjectId == null) {
                aggregatedObjectId = addTemplateNameToIds;
            }
            eventHandler.eventReceived(jsonObj2.getJSONObject(i).toString());
        }

        if (aggregatedObjectId != null) {
            ArrayList<String> response = processAggregatedObject.processQueryAggregatedObject(aggregatedObjectId);
            // Delete the aggregated object
            processAggregatedObject.deleteAggregatedObject(aggregatedObjectId);
            // Delete the event object mapper
            eventToObjectMapHandler.deleteEventObjectMap(aggregatedObjectId);
            return response.toString();
        } else {
            return null;
        }
    }

    private String addTemplateNameToIds(JSONObject jsonObject, String templateName) throws JSONException {
        String idTemplateSuffix = jmesPathInterface.runRuleOnEvent("meta.id", jsonObject.toString()).asText() + "_"
                + templateName;
        jsonObject.getJSONObject("meta").put("id", idTemplateSuffix);
        for (int i = 0; i < jsonObject.getJSONArray("links").length(); i++) {
            JSONObject link = jsonObject.getJSONArray("links").getJSONObject(i);
            link.put("target", link.getString("target") + "_" + templateName);
        }
        return idTemplateSuffix;
    }
}
