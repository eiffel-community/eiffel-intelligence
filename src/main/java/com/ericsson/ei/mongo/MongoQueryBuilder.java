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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class MongoQueryBuilder implements MongoQuery {

    private static final String AND = "$and";
    private JSONObject query;

    /**
     * Will build an "and" query of the given JSONObjects removing null and empty objects. If the
     * user only gives one object or one object remains after filtering, the builder will return a
     * query containing only that condition. If all objects are filtered out the build will return
     * an empty object.
     * <p>
     * <code>MongoQueryBuilder.buildAnd(jsonObject1, jsonObject2).toString()</code><br/>
     * will result in<br/>
     * <code>{"$and":[&lt;jsonObject1&gt;,&lt;jsonObject2&gt;]}</code><br/>
     * </p>
     *
     * <p>
     * <code>MongoQueryBuilder.buildAnd(jsonObject).toString()</code><br/>
     * will result in<br/>
     * <code>&lt;jsonObject&gt;</code><br/>
     * </p>
     *
     * <p>
     * <code>MongoQueryBuilder.buildAnd(nullJsonObject).toString()</code><br/>
     * will result in<br/>
     * <code>{}</code><br/>
     * </p>
     *
     * @param jsonObjects Objects to build the "and" query with
     * @return the "and" query
     */
    public static MongoQuery buildAnd(JSONObject... jsonObjects) {
        return new MongoQueryBuilder(AND, jsonObjects);
    }

    /**
     * Will build an "and" query see {@link #buildAnd(JSONObject...)} but with {@link MongoCondition}.
     * @param mongoConditions condition to build the "and" query with
     * @return the "and" query
     */
    public static MongoQuery buildAnd(MongoCondition... mongoConditions) {
        JSONObject[] conditionsAsJSON = getJsonObjects(mongoConditions);
        return buildAnd(conditionsAsJSON);
    }

    /**
     * Returns this MongoQueryBuilder as JSON
     *
     * @return Condition as JSON
     */
    @Override
    public String getQueryString() {
        return query.toString();
    }

    /**
     * See {@link #getQueryString()}
     */
    @Override
    public String toString() {
        return getQueryString();
    }

    private static JSONObject[] getJsonObjects(MongoCondition... mongoConditions) {
        JSONObject[] conditionsAsJSON = Arrays.asList(mongoConditions)
                                              .stream()
                                              .map(condition -> condition.asJSONObject())
                                              .toArray(JSONObject[]::new);
        return conditionsAsJSON;
    }

    /**
     * Private constructor to force user to give mandatory parameters
     *
     * @param operand
     * @param jsonObjects
     */
    private MongoQueryBuilder(String operand, JSONObject... jsonObjects) {

        List<JSONObject> validJsonObjects = getNonEmpty(jsonObjects);

        if (validJsonObjects.isEmpty()) {
            query = new JSONObject();
        } else if (validJsonObjects.size() == 1) {
            query = validJsonObjects.get(0);
        } else {
            JSONArray jsonArray = new JSONArray(validJsonObjects);
            query = new JSONObject();
            query.put(operand, jsonArray);
        }

    }

    private List<JSONObject> getNonEmpty(JSONObject... jsonObjects) {

        Predicate<JSONObject> isNotEmpty = jsonObject -> (jsonObject != null)
                && (jsonObject.length() > 0);

        List<JSONObject> validJsonObjects = Arrays.asList(jsonObjects)
                                                  .stream()
                                                  .filter(isNotEmpty)
                                                  .collect(Collectors.toList());
        return validJsonObjects;
    }

}
