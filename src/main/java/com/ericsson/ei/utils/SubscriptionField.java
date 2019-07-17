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
package com.ericsson.ei.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class SubscriptionField {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionField.class);

    private JsonNode subscriptionJson;

    public SubscriptionField(JsonNode subscriptionJson) {
        this.subscriptionJson = subscriptionJson;
    }

    /**
     * Given the field name in a subscription, returns its value.
     *
     * @param subscriptionJson
     * @param fieldName
     * @return field value
     */
    public String get(String fieldName) {
        // TODO: Implement . notation, authenticationDetails.username would return the
        // username value in {"authenticationDetails":{"username":"myName", "password":"secret"}}

        String value;
        if (subscriptionJson.get(fieldName) != null) {
            value = subscriptionJson.get(fieldName).asText();
            LOGGER.debug("Extracted value [{}] from subscription field [{}].", value, fieldName);
        } else {
            value = "";
        }
        return value;
    }
}
