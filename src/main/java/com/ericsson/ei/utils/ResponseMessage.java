package com.ericsson.ei.utils;

import org.json.JSONObject;

/**
 * Contains a single method that is used in the controller classes to put error response messages in
 * a JSON formatted string.
 *
 * @author ezcorch
 *
 */
public class ResponseMessage {
    private static final String KEY = "message";

    /**
     * Puts the message in a JSON formatted key value pair. The key name is "message" and the value
     * is the provided message.
     *
     * @param message
     *            the message to wrap
     * @return the formatted string
     */
    public static String createJsonMessage(String message) {
        return new JSONObject().put(KEY, message).toString();
    }
}
