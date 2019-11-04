/*
   Copyright 2019 Ericsson AB.
   For a full list of individual contributors, please see the commit history.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.ei.mongo;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

public class MongoCondition implements MongoQuery {

    private static final String ID = "_id";
    private static final String LOCK = "lock";
    private static final String SUBSCRIPTION_ID = "subscriptionId";
    private static final String SUBSCRIPTION_NAME = "subscriptionName";
    private static final String LDAP_USER_NAME = "ldapUserName";
    public static final Object NULL = JSONObject.NULL;

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
     * subscription id. Called with <code>subscription-id</code> the JSON will look like:
     * <p>
     * <code>{"subscriptionId":"subscription-id"}
     *
     * @param subscriptionIdValue the value of the subscription id
     * @return A MongoCondition with subscription id set
     */
    public static MongoCondition subscriptionCondition(String subscriptionIdValue) {
        return condition(SUBSCRIPTION_ID, subscriptionIdValue);
    }

    /**
     * Creates a MongoCondition to find a document containing a subscription matching the given
     * subscription name. Called with <code>subscription-name</code> the JSON will look like:
     * <p>
     * <code>{"subscriptionName":"subscription-name"}
     *
     * @param subscriptionName the value of the subscription name
     * @return A MongoCondition with subscription name set
     */
    public static MongoCondition subscriptionNameCondition(String subscriptionName) {
        return condition(SUBSCRIPTION_NAME, subscriptionName);
    }

    /**
     * Creates a MongoCondition to find a document containing a ldapUserName matching the given
     * name. Called with <code>ldap-user-name</code> the JSON will look like:
     * <p>
     * <code>{"ldapUserName":"ldap-user-name"}
     *
     * @param ldapUserName
     * @return A MongoCondition with ladap username set
     */
    public static MongoCondition ldapUserNameCondition(String ldapUserName) {
        return condition(LDAP_USER_NAME, ldapUserName);
    }

    /**
     * Creates a MongoCondition to find a document containing a lock matching the given value.
     * Called with <code>0</code> the JSON will look like:
     * <p>
     * <code>{"lock":"0"}
     *
     * @param lockValue
     * @return A MongoCondition with lock set
     */
    public static MongoCondition lockCondition(String lock) {
        return condition(LOCK, lock);
    }

    /**
     * Creates a MongoCondition to find a document not containing a lock. The JSON will look like:
     * <p>
     * <code>{"lock":null}
     *
     * @return A MongoCondition with lock missing
     */
    public static MongoCondition lockNullCondition() {
        return new MongoCondition(LOCK, NULL);
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
     * Creates a MongoCondition with key as <code>key</code> and value as <code>value</code>. Called
     * with <code>my-key</code> and <code>my-value</code> the JSON will look like this:
     * <code>{"my-key":"my-value"}</code>
     *
     * @param key
     * @param value
     * @return
     */
    protected static MongoCondition condition(String key, String value) {
        return new MongoCondition(key, value);
    }

    protected JSONObject asJSONObject() {
        return condition;
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

    /**
     * Private constructor intended to handle the null case
     */
    private MongoCondition(String key, Object object) {
        condition = new JSONObject();
        condition.put(key, object);
    }
}
