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

import static org.junit.Assert.assertFalse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.MongoClientURI;

public class MongoUri {

    private static final String HIDDEN_PASSWORD = "********";

    /**
     * The input uri will have it´s password replaced wit stars and returned uri is validated not to
     * contain the password.
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

        final String oldPasswordField = String.format(":%s", oldPassword);
        assertFalse("URI contains old password.", modifiedUri.contains(oldPasswordField));

        return modifiedUri;
    }

    /**
     * Raplace existing password in uri with new password.
     *
     * Example input uri: mongodb://username:password@hostname1:27017,hostname2:27017 input
     * new_password
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
     * Example input uri: mongodb://username:mypasswd@hostname1:27017,hostname2:27017
     *
     * Returns mypasswd
     *
     * @param uri
     * @return
     */
    public static String extractPasswordFromUri(String inputUri) {
        String password = "";
        MongoClientURI uri = new MongoClientURI(inputUri);
        char[] passwordCharList = uri.getPassword();
        if (passwordCharList != null) {
            password =  new String(passwordCharList);
        }
        return password;
    }

    /**
     * The password will always have ':' in front. Username has ':' after. By replacing (':' +
     * password) we know we don´t replace username if username and password is the same, like
     * /admin:admin@*.
     *
     * @param uri
     * @param oldPassword
     * @param newPassword
     * @return
     */
    private static String replacePassword(String uri, String oldPassword, String newPassword) {
        final String oldPasswordField = String.format(":%s", oldPassword);
        final String newPasswordField = String.format(":%s", newPassword);

        return uri.replaceFirst(oldPasswordField, newPasswordField);
    }
}
