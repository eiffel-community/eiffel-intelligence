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

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.controller.model.AuthenticationType;

/**
 * An encryption formatter helper class.
 *
 */
@Component
public class Encryptor {

    @Value("${jasypt.encryptor.password:}")
    private String password;

    private static String jasyptEncryptorPassword;
    private static StandardPBEStringEncryptor encryptor;

    @PostConstruct
    public void init() {
        jasyptEncryptorPassword = password;
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(jasyptEncryptorPassword);
    }

    private Encryptor() {
    }

    /**
     * Encrypts a message.
     *
     * @param message
     * @return
     */
    public static String encrypt(String message) {
        return encryptor.encrypt(message);
    }

    /**
     * Decrypts a message.
     *
     * @param message
     * @return
     */
    public static String decrypt(String message) {
        return encryptor.decrypt(message);
    }

    /**
     * Function that removes ENC parentheses from encrypted string. Commonly used for password
     * properties that has a format "ENC(d23d2ferwf4t55)" This function removes "ENC(" text and end
     * ")" parentheses and return only the encryption as string.
     * 
     * Function handle also "ENC(<encrypted password>" with missing end , ')', parentheses.
     * 
     * @param stringWithEncryptionParantheses The string that contains the ENC() string.
     * 
     * @return Then string formatted encrypted password without ENC().
     */
    public static String removeEncryptionParentheses(String stringWithEncryptionParantheses) {
        String formattedEncryptionString = stringWithEncryptionParantheses.replace("ENC(", "");
        int lastParanthesesOccurenxeIndex = formattedEncryptionString.lastIndexOf(")");
        if (lastParanthesesOccurenxeIndex == -1) {
            return formattedEncryptionString;
        }
        formattedEncryptionString = formattedEncryptionString.subSequence(0,
                lastParanthesesOccurenxeIndex).toString();
        return formattedEncryptionString;
    }

    /**
     * Checks if the provided password string contains the encryption wrapper. The encryption
     * wrapper should look like this: ENC(myEncryptedPassword)
     *
     * @param password
     * @return true if password is tagged as encryped, false otherwise
     */
    public static boolean isEncrypted(final String password) {
        return (password.startsWith("ENC(") && password.endsWith(")"));
    }

    /**
     * Returns a boolean indicating that authentication details are provided.
     *
     * @param authType
     * @param username
     * @param password
     * @return
     */
    public static boolean verifyAuthenticationDetails(String authType, String username,
            String password) {
        if (StringUtils.isEmpty(authType)
                || authType.equals(AuthenticationType.NO_AUTH.getValue())) {
            return false;
        }

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return false;
        }

        return true;
    }

    public static boolean isJasyptPasswordSet() {
        return !StringUtils.isEmpty(jasyptEncryptorPassword);
    }
}
