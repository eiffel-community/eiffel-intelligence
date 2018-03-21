package com.ericsson.ei.services;

import java.io.IOException;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IRuleCheckService {

    /**
     * This method used for the prepare the aggregated object using list of
     * rules and list events This method uses the mongodb to store and update
     * aggregated object, once aggregation done it return and remove from the
     * mongodb. All these test events aggregated objects having the suffiex name
     * "_"<templateName>
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
    String prepareAggregatedObject(String listRulesJson, String listEventsJson)
            throws JSONException, JsonProcessingException, IOException;

}
