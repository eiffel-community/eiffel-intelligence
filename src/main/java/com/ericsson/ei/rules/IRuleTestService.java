package com.ericsson.ei.rules;

import java.io.IOException;

import com.ericsson.ei.exception.InvalidRulesException;

import org.json.JSONArray;
import org.json.JSONException;


public interface IRuleTestService {

    /**
     * This method is used to start a test aggregation on given set of rules and events. The
     * rules are validated to ensure they all contain the same template name.
     * Before starting the process the event ids gets a suffix attached, to identify these as
     * test events. When test aggregation is done it is removed from the database.
     *
     * @param listRulesJson a JSONArray containing a list of rules to test
     * @param listEventsJson  a JSONArray containing a list of events to test
     * @throws JSONException
     * @throws IOException
     * @throws InvalidRulesException
     * */
    String prepareAggregatedObject(JSONArray listRulesJson, JSONArray listEventsJson)
            throws JSONException, IOException, InvalidRulesException, Exception;

}
