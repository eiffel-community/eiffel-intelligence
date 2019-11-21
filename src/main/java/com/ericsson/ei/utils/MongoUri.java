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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class MongoUri {

    private static final String HIDDEN_PASSWORD = "********";
    /**
     * This method replaces the given secret if any
     * @param uri
     * @return
     */
    public static String getSafeUri(String uri) {
        if (StringUtils.isBlank(uri)) {
            return "";
        }
        String secret = extractSecretFromUri(uri);
        String modifiedUri = uri.replaceFirst(secret, HIDDEN_PASSWORD);
        return modifiedUri;
    }

    /**
     * This method should be moved to decrypt library once EI 2.0.2 is merget to master
     *
     * @param uri
     * @return
     */
    public static String extractSecretFromUri(String uri) {
        String secret = "";
        // Matcher that match string between : and @
        Matcher matcher = Pattern.compile("(?<=:)(.*)(?=@)").matcher(uri);
        if (matcher.find()) {
            String authenticationDetails = matcher.group(0).replace("//", "");
            if (authenticationDetails.contains(":")) {
                String[] authenticationDetailsList = authenticationDetails.split(":");
                secret = authenticationDetailsList[1];
            }
        }
        return secret;
    }
}
