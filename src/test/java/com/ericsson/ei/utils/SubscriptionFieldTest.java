/*
   Copyright 2017 Ericsson AB.
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.eiffelcommons.subscriptionobject.RestPostSubscriptionObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionFieldTest {

    private static final String URL = "http://www.somehost.com/some-endpoint/?param1='my_token'&param2=my_second_param";

    private static final String USERNAME = "my_user_name";
    private static final String AUTH_TYPE = "BASIC_AUTH";
    private static final String PASSWORD = "my_password";

    private RestPostSubscriptionObject subscription;
    private final ObjectMapper mapper = new ObjectMapper();

    SubscriptionField subscriptionField;

    @Before
    public void beforeTests() throws IOException {
        subscription = new RestPostSubscriptionObject("Test");
        subscription.addNotificationMessageKeyValue("Key", "message")
                    .setAuthenticationType(AUTH_TYPE)
                    .setNotificationMeta(URL)
                    .setUsername(USERNAME)
                    .setPassword(PASSWORD);
        final JsonNode subscriptionNode = mapper.readValue(
                subscription.getSubscriptionJson().toString(), JsonNode.class);
        subscriptionField = new SubscriptionField(subscriptionNode);
    }

    @Test
    public void testGetFields() throws Exception {
        assertEquals("Username: ", USERNAME, subscriptionField.get("userName"));
        assertEquals("Password: ", PASSWORD, subscriptionField.get("password"));
        assertEquals("Username: ", URL, subscriptionField.get("notificationMeta"));
    }

    @Test
    public void testFieldWithDotNotification() throws Exception {
        // TODO: To be implemented
    }

}
