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
