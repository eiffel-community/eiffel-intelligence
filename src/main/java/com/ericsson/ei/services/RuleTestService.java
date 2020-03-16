package com.ericsson.ei.services;

import com.ericsson.ei.exception.InvalidRulesException;
import com.ericsson.ei.handlers.EventHandler;
import com.ericsson.ei.handlers.EventToObjectMapHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.queryservice.ProcessAggregatedObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class RuleTestService implements IRuleTestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleTestService.class);

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private EventHandler eventHandler;

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    @Autowired
    private EventToObjectMapHandler eventToObjectMapHandler;

    @Override
    public String prepareAggregatedObject(JSONArray listRulesJson, JSONArray listEventsJson)
            throws JSONException, IOException, InvalidRulesException {
        eventHandler.getRulesHandler().setParsedJson(listRulesJson.toString());
        String response = "";
        // Looping all events and add suffix template name to id and links, For
        // identifying the test aggregated events.
        List<String> templateNames = new ArrayList<String>();
        for (int i = 0; i < listRulesJson.length(); i++) {
            String templateName = jmesPathInterface.runRuleOnEvent("TemplateName",
                    listRulesJson.getJSONObject(i).toString()).asText("TEST");
            if (!templateNames.contains(templateName)) {
                templateNames.add(templateName);
            }
        }

        if (templateNames.size() != 1) {
            String errorMessage = "Different template names are not allowed in rules, Please use "
                    + "one template name for all rules.";
            throw new InvalidRulesException(errorMessage);
        }

        String templateName = templateNames.iterator().next();
        for (int i = 0; i < listEventsJson.length(); i++) {
            addTemplateNameToIds(listEventsJson.getJSONObject(i), templateName);
            LOGGER.debug("Event to prepare aggregated object :: {}",
                    listEventsJson.getJSONObject(i).toString());
            eventHandler.eventReceived(listEventsJson.getJSONObject(i).toString());
        }
        List<String> responseList = processAggregatedObject.getAggregatedObjectByTemplateName(templateName);
        response = responseList.toString();

        // Delete the aggregated object
        processAggregatedObject.deleteAggregatedObject(templateName);
        // Delete the event object mapper
        eventToObjectMapHandler.deleteEventObjectMap(templateName);

        return response;
    }

    private void addTemplateNameToIds(JSONObject jsonObject, final String templateName) throws JSONException {
        String idTemplateSuffix = jmesPathInterface.runRuleOnEvent("meta.id", jsonObject.toString()).asText() + "_"
                + templateName;
        if (jsonObject.has("meta"))
            jsonObject.getJSONObject("meta").put("id", idTemplateSuffix);
        if (jsonObject.has("links")) {
            for (int i = 0; i < jsonObject.getJSONArray("links").length(); i++) {
                JSONObject link = jsonObject.getJSONArray("links").getJSONObject(i);
                link.put("target", link.getString("target") + "_" + templateName);
            }
        }
    }
}
