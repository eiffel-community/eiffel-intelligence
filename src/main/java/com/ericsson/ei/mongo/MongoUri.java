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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class MongoUri {

    private static final String HIDDEN_PASSWORD = "********";

    /**
     * This method replaces the given password if any.
     *
     * Example input uri: mongodb://username:password@hostname1:27017,hostname2:27017
     *
     * Returns mongodb://username:********@hostname1:27017,hostname2:27017
     *
     * @param uri
     * @return
     */
    public static String getUriWithHiddenPassword(String uri) {
        if (StringUtils.isBlank(uri)) {
            return "";
        }

        String oldPassword = extractPasswordFromUri(uri);
        String modifiedUri = replacePassword(uri, oldPassword, HIDDEN_PASSWORD);
        return modifiedUri;
    }

    /**
     * This method replaces the given password if any.
     *
     * Example input uri: mongodb://username:password@hostname1:27017,hostname2:27017 input new_password
     *
     * Returns mongodb://username:new_password@hostname1:27017,hostname2:27017
     *
     * @param uri
     * @param newPassword
     * @return
     */
    public static String getUriWithNewPassword(String uri, String newPassword) {
        if (StringUtils.isBlank(uri)) {
            return "";
        }

        String oldPassword = extractPasswordFromUri(uri);
        String modifiedUri = replacePassword(uri, oldPassword, newPassword);
        return modifiedUri;
    }

    /**
     * Returns the password if any exist in a URI
     *
     * Example input uri: mongodb://username:mypasswd@hostname1:27017,hostname2:27017
     *
     * Returns mypasswd
     *
     * @param uri
     * @return
     */
    public static String extractPasswordFromUri(String uri) {
        String secret = "";
        /**
         * Matcher that match string between first : and @.
         *
         * Example uri: mongodb://username:mypasswd@hostname1:27017,hostname2:27017
         *
         * The first match would be [//username:mypasswd] After removing the // we split the string
         * at the : character. This gives authenticationDetailsList[0] as username and
         * authenticationDetailsList[1] as password.
         *
         */
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


    /**
     * By ensuring password is behoind : we know we do not replace username in case username and
     * password is the same.
     *
     * @param uri
     * @param oldPassword
     * @param newPassword
     * @return
     */
    private static String replacePassword(String uri, String oldPassword, String newPassword) {
        return uri.replaceFirst(String.format(":%s", oldPassword),
                String.format(":%s", newPassword));
    }
}
