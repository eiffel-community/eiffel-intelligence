package com.ericsson.ei.rules;

import com.ericsson.ei.exception.InvalidRulesException;
import com.ericsson.ei.exception.MongoDBConnectionException;
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
            throws JSONException, IOException, InvalidRulesException, Exception {
        eventHandler.getRulesHandler().setParsedJson(listRulesJson.toString());

        String templateName = validateRuleTemplateNames(listRulesJson);
        try {
            prepareEventsForTestAggregation(listEventsJson, templateName);
        } catch (MongoDBConnectionException e) {
            LOGGER.info("Mongodb connection down");
        }

        List<String> responseList = processAggregatedObject.getAggregatedObjectByTemplateName(templateName);
        String response = responseList.toString();

        // Delete the aggregated object
        processAggregatedObject.deleteAggregatedObject(templateName);
        // Delete the event object mapper
        eventToObjectMapHandler.deleteEventObjectMap(templateName);

        return response;
    }

    /**
     * This method iterates through the list of rules to extract the template name from each rule.
     * All rules should contain the same template name, which is returned.
     *
     * @throws  InvalidRulesException if the rules contain several template names
     * @return a single template name used in all rules
     * */
    private String validateRuleTemplateNames(JSONArray listRulesJson)
            throws InvalidRulesException {
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
        return templateName;
    }

    /**
     * Iterates through a list of events and adding a suffix to their ids and the ids in their
     * links. This is to easily identify which events are used in a test aggregation.
     * @throws Exception 
     * @throws JSONException 
     */
    private void prepareEventsForTestAggregation(JSONArray listEventsJson, String suffix) throws JSONException, Exception{
        for (int i = 0; i < listEventsJson.length(); i++) {
            addTemplateNameToIds(listEventsJson.getJSONObject(i), suffix);
            LOGGER.debug("Event to prepare aggregated object :: {}",
                    listEventsJson.getJSONObject(i).toString());
            eventHandler.eventReceived(listEventsJson.getJSONObject(i).toString(), false);
        }
    }

    /**
     * Takes an Eiffel event and a suffix to attach to the event id and all ids in the links.
     * @throws JSONException
     * */
    private void addTemplateNameToIds(JSONObject jsonObject, final String suffix) throws JSONException {
        String idTemplateSuffix = jmesPathInterface.runRuleOnEvent("meta.id", jsonObject.toString()).asText() + "_"
                + suffix;
        if (jsonObject.has("meta"))
            jsonObject.getJSONObject("meta").put("id", idTemplateSuffix);
        if (jsonObject.has("links")) {
            for (int i = 0; i < jsonObject.getJSONArray("links").length(); i++) {
                JSONObject link = jsonObject.getJSONArray("links").getJSONObject(i);
                link.put("target", link.getString("target") + "_" + suffix);
            }
        }
    }
}
