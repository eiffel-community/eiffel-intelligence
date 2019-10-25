package com.ericsson.ei.handlers;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

public class MongoCondition implements MongoQuery{

    private static final String ID = "_id";
    private static final String SUBSCRIPTION_ID = "subscriptionId";

    private JSONObject condition;

    /**
     * Creates a MongoCondition to find a document with a given id. Called with
     * <code>my-document-id</code> the JSON will look like this:
     * <code>{"_id":"my-document-id"}</code>
     *
     * @param documentId The id value
     * @return A MongoCondition with id set
     */
    public static MongoCondition idCondition(String documentId) {
        return condition(ID, documentId);
    }

    /**
     * Creates a MongoCodition just as {@link #idCondition(String)} but with the
     * <code>jsonNode</code> value fetched correctly.
     *
     * @param jsonNode Id condition value as <code>JsonNode</code>
     * @return A MongoCondition with id set
     */
    public static MongoCondition idCondition(JsonNode jsonNode) {
        return idCondition(jsonNode.asText());
    }

    /**
     * Creates a MongoCondition to find a document containing a subscription matching the given
     * subscription id. Called with <code>subscription-id</code> the JSON will look like:<p>
     * <code>{"subscriptionId":"subscription-id"}
     *
     * @param subscriptionIdValue the value of the subscription id
     * @return A MongoCondition with subscription id set
     */
    public static MongoCondition subscriptionCondition(String subscriptionIdValue) {
        return condition(SUBSCRIPTION_ID, subscriptionIdValue);
    }

    /**
     * Creates a MongoCondition with key as <code>key</code> and value as <code>value</code>. Called
     * with <code>my-key</code> and <code>my-value</code> the JSON will look like this:
     * <code>{"my-key":"my-value"}</code>
     *
     * @param key
     * @param value
     * @return
     */
    public static MongoCondition condition(String key, String value) {
        return  new MongoCondition(key,value);
    }

    /**
     * Creates an empty MongoCondition. As JSON it will look like this: <code>{}</code>
     *
     * @return An empty MongoCondition
     */
    public static MongoCondition emptyCondition() {
        return new MongoCondition();
    }

    /**
     * Returns this MongoCondition as JSON
     *
     * @return Condition as JSON
     */
    @Override
    public String getQueryString() {
        return condition.toString();
    }

    /**
     * See {@link #getQueryString()}
     */
    @Override
    public String toString() {
        return getQueryString();
    }

    /**
     * Private constructor to force the user to give mandatory parameters
     */
    private MongoCondition() {
        condition = new JSONObject();
    }

    /**
     * Private constructor to force the user to give mandatory parameters
     */
    private MongoCondition(String key, String value) {
        condition = new JSONObject();
        condition.put(key, value);
    }


}
