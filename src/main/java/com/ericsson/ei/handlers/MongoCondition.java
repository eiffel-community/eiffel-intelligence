package com.ericsson.ei.handlers;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

public class MongoCondition {

    private static final String ID = "_id";

    private JSONObject condition;

    MongoCondition() {
        condition = new JSONObject();
    }

    public String getAsJson() {

        return condition.toString();
    }

    void set(String key, String value) {
        condition.put(key, value);
    }

    public static MongoCondition idCondition(String string) {
        return condition(ID, string);
    }

    public static MongoCondition idCondition(JsonNode jsonNode) {
        return idCondition(jsonNode.asText());
    }

    public static MongoCondition condition(String key, String value) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.set(key, value);
        return mongoCondition;
    }
}
