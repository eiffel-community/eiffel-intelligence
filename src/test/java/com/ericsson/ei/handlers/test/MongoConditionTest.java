package com.ericsson.ei.handlers.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.ericsson.ei.handlers.MongoCondition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class MongoConditionTest {

    @Test
    public void testGetIdConditionFromString() {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.setId("id-as-string");

        String actual = mongoCondition.getAsJson();
        String expect = "{\"_id\":\"id-as-string\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testGetIdConditionFromJsonNode() {
        MongoCondition mongoCondition = new MongoCondition();
        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(false);
        JsonNode jsonNode = jsonNodeFactory.textNode("id-as-JsonNode");
        mongoCondition.setId(jsonNode);
        String actual = mongoCondition.getAsJson();
        String expect = "{\"_id\":\"id-as-JsonNode\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIdConditionFromEmpty() {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.getAsJson();
    }
}
