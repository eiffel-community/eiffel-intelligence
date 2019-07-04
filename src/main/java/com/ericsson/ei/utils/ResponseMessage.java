package com.ericsson.ei.utils;

import org.json.JSONObject;

public class ResponseMessage {
    private static final String KEY = "message";

    public static String createJsonMessage(String message) {
        return new JSONObject().put(KEY, message).toString();  
    }
}
