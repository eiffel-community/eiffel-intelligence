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

package com.ericsson.ei.encryption;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Used for encryption/decryption of strings.
 *
 */
@Component
public class Encryptor {

    @Value("${jasypt.encryptor.password:}")
    private String jasyptEncryptorPassword;

    private StandardPBEStringEncryptor encryptor;

    @PostConstruct
    private void init() {
        encryptor = new StandardPBEStringEncryptor();
        if (isJasyptPasswordSet()) {
            encryptor.setPassword(jasyptEncryptorPassword);
        }
    }

    /**
     * Encrypts a message.
     *
     * @param message
     * @return
     */
    public String encrypt(String message) {
        return encryptor.encrypt(message);
    }

    /**
     * Decrypts a message.
     *
     * @param message
     * @return
     */
    public String decrypt(String message) {
        return encryptor.decrypt(message);
    }

    public boolean isJasyptPasswordSet() {
        return !StringUtils.isEmpty(jasyptEncryptorPassword);
    }
}
