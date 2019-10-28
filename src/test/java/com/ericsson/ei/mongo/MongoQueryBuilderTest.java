package com.ericsson.ei.mongo;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.json.JSONObject;
import org.junit.Test;

public class MongoQueryBuilderTest {

    @Test
    public void testAndQuery() {
        JSONObject arg1 = new JSONObject();
        arg1.put("first", "critera");

        JSONObject arg2 = new JSONObject();
        arg2.put("second", "critera");

        String actual = MongoQueryBuilder.buildAnd(arg1, arg2).toString();
        String expect = "{\"$and\":[{\"first\":\"critera\"},{\"second\":\"critera\"}]}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testAndQueryFirstEmpty() {
        JSONObject arg1 = new JSONObject();

        JSONObject arg2 = new JSONObject();
        arg2.put("second", "critera");

        String actual = MongoQueryBuilder.buildAnd(arg1, arg2).toString();
        String expect = "{\"second\":\"critera\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testAndQueryFirstNull() {
        JSONObject arg1 = null;

        JSONObject arg2 = new JSONObject();
        arg2.put("second", "critera");

        String actual = MongoQueryBuilder.buildAnd(arg1, arg2).toString();
        String expect = "{\"second\":\"critera\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testAndQueryOneArgument() {
        JSONObject arg = new JSONObject();
        arg.put("second", "critera");

        String actual = MongoQueryBuilder.buildAnd(arg).toString();
        String expect = "{\"second\":\"critera\"}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testAndQueryAllNull() {
        JSONObject arg1 = null;
        String actual = MongoQueryBuilder.buildAnd(arg1).toString();
        String expect = "{}";
        assertThat(actual, is(equalTo(expect)));
    }

    @Test
    public void testAndQueryToMongoConditions() {
        MongoCondition arg1 = MongoCondition.subscriptionNameCondition("subscription-name");
        MongoCondition arg2 = MongoCondition.ldapUserNameCondition("ldap-user-name");

        String actual = MongoQueryBuilder.buildAnd(arg1, arg2).toString();
        String expect = "{\"$and\":"
                + "[{\"subscriptionName\":\"subscription-name\"},"
                + "{\"ldapUserName\":\"ldap-user-name\"}]}";
        assertThat(actual, is(equalTo(expect)));
    }
}
