package com.ericsson.ei.handlers;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

public class MongoCondition {

    private static final String ID = "_id";

    private JSONObject condition = new JSONObject();;

    public void setId(String string) {
        condition.put(ID, string);
    }

    public void setId(JsonNode jsonNode) {
        setId(jsonNode.asText());
    }

    public String getAsJson() {
        boolean noConditionsSet = (condition.length() == 0);
        if(noConditionsSet) {
            throw new IllegalArgumentException("No condition was set");
        }

        return condition.toString();
    }
}
