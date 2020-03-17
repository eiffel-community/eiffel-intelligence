package com.ericsson.ei.rules;

import java.io.IOException;

import com.ericsson.ei.exception.InvalidRulesException;
import org.json.JSONArray;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IRuleTestService {

    /**
     * This method prepares an aggregated object using a list of rules and a list events. This
     * method uses the mongodb to store and update aggregated object, once aggregation done it
     * return and remove from the mongodb. All these test events aggregated objects having the
     * suffix name _"<templateName>.
     *
     * @param listRulesJson
     *            each event has their own rule set
     * @param listEventsJson
     *            list of events to perform aggregated using the listRulesJson
     * @return
     * @throws JSONException
     * @throws JsonProcessingException
     * @throws IOException
     */
    String prepareAggregatedObject(JSONArray listRulesJson, JSONArray listEventsJson)
            throws JSONException, JsonProcessingException, IOException, InvalidRulesException;

}
