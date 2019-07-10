package com.ericsson.ei.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.subscription.HttpRequest;
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
        // TODO: Implement . notation. authenticationDetails.username would return the
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
