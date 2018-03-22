package com.ericsson.ei.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleCheckService.class);

    @Override
    public String prepareAggregatedObject(String listRulesJson, String listEventsJson)
            throws JSONException, JsonProcessingException, IOException {
        JSONArray rulesList = new JSONArray(listRulesJson);
        JSONArray eventsList = new JSONArray(listEventsJson);
        eventHandler.getRulesHandler().setParsedJson(rulesList.toString());
        String response = null;
        // Looping all events and add suffix template name to id and links, For
        // identifying the test aggregated events.
        HashSet<String> templateNames = new HashSet<>();
        for (int i = 0; i < eventsList.length(); i++) {
            String templateName = jmesPathInterface
                    .runRuleOnEvent("TemplateName", rulesList.getJSONObject(i).toString()).asText("TEST");
            templateNames.add(templateName);
            if (templateNames.size() == 1) {
                addTemplateNameToIds(eventsList.getJSONObject(i), templateName);
                LOGGER.debug("event to prepare aggregated object :: " + eventsList.getJSONObject(i).toString());
                eventHandler.eventReceived(eventsList.getJSONObject(i).toString());
            }
        }

        String templateName = templateNames.iterator().next();
        if (templateNames.size() == 1) {
            ArrayList<String> responseList = processAggregatedObject.getAggregatedObjectByTemplateName(templateName);
            response = responseList.toString();
        } else {
            response = "Multipul template names are not allowed, Please use single name for all rules.";
        }
        // Delete the aggregated object
        processAggregatedObject.deleteAggregatedObject(templateName);
        // Delete the event object mapper
        eventToObjectMapHandler.deleteEventObjectMap(templateName);
        return response;
    }

    private void addTemplateNameToIds(JSONObject jsonObject, final String templateName) throws JSONException {
        String idTemplateSuffix = jmesPathInterface.runRuleOnEvent("meta.id", jsonObject.toString()).asText() + "_"
                + templateName;
        jsonObject.getJSONObject("meta").put("id", idTemplateSuffix);
        for (int i = 0; i < jsonObject.getJSONArray("links").length(); i++) {
            JSONObject link = jsonObject.getJSONArray("links").getJSONObject(i);
            link.put("target", link.getString("target") + "_" + templateName);
        }
    }
}
