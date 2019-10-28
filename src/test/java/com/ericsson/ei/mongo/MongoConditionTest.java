package com.ericsson.ei.mongo;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.json.JSONObject;
import org.junit.Test;

import com.ericsson.ei.mongo.MongoCondition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class MongoConditionTest {

    @Test
    public void testGetIdConditionFromString() {
        MongoCondition mongoCondition = MongoCondition.idCondition("id-as-string");

        String actual = mongoCondition.getQueryString();
        String expect = "{\"_id\":\"id-as-string\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testGetIdConditionFromJsonNode() {
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(false);
        JsonNode jsonNode = jsonNodeFactory.textNode("id-as-JsonNode");
        MongoCondition mongoCondition = MongoCondition.idCondition((jsonNode));

        String actual = mongoCondition.getQueryString();
        String expect = "{\"_id\":\"id-as-JsonNode\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testGetsubscriptionConditionFromString() {
        MongoCondition mongoCondition = MongoCondition.subscriptionCondition(
                "subscription-as-string");

        String actual = mongoCondition.getQueryString();
        String expect = "{\"subscriptionId\":\"subscription-as-string\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testGetsubscriptionNameConditionFromString() {
        MongoCondition mongoCondition = MongoCondition.subscriptionNameCondition(
                "subscription-name");

        String actual = mongoCondition.getQueryString();
        String expect = "{\"subscriptionName\":\"subscription-name\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testLdapUserNameConditionFromString() {
        MongoCondition mongoCondition = MongoCondition.ldapUserNameCondition(
                "ldap-user-name");

        String actual = mongoCondition.getQueryString();
        String expect = "{\"ldapUserName\":\"ldap-user-name\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testGetArbitraryConditionFromString() {
        MongoCondition mongoCondition = MongoCondition.condition("arbitraryKey",
                "id-as-string");

        String actual = mongoCondition.getQueryString();
        String expect = "{\"arbitraryKey\":\"id-as-string\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testIdConditionToString() {
        MongoCondition mongoCondition = MongoCondition.idCondition("id-as-string");

        String actual = mongoCondition.toString();
        String expect = "{\"_id\":\"id-as-string\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    /**
     * Interface is only used in the package to support building queries of Mongo Conditions
     */
    @Test
    public void testIdConditionAsJSONObject() {
        String conditionString = "id-as-string";
        MongoCondition mongoCondition = MongoCondition.idCondition(conditionString);

        JSONObject asJSONObject = mongoCondition.asJSONObject();
        Object actual = asJSONObject.get("_id");

        assertThat(actual, is(equalTo(conditionString)));
    }

    @Test
    public void testEmptyCondition() {
        MongoCondition mongoCondition = MongoCondition.emptyCondition();

        String actual = mongoCondition.getQueryString();
        String expect = "{}";
        assertThat(actual, is(equalTo(expect)));
    }

}
