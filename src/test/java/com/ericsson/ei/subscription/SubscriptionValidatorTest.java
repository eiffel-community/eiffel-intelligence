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
package com.ericsson.ei.subscription;

import static org.junit.Assert.fail;
import static org.powermock.reflect.Whitebox.invokeMethod;

import org.junit.Test;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.controller.model.Condition;
import com.ericsson.ei.controller.model.NotificationMessageKeyValue;
import com.ericsson.ei.controller.model.Requirement;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;

@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
public class SubscriptionValidatorTest {

    private Subscription subscriptionValid;
    private Subscription subscriptionInvalid;

    public SubscriptionValidatorTest() {
        // subscriptionValidator -------------------------
        subscriptionValid = new Subscription();

        subscriptionValid.setSubscriptionName("Kalle1");

        // new stuff
        subscriptionValid.setRestPostBodyMediaType(
                MediaType.APPLICATION_FORM_URLENCODED.toString());

        NotificationMessageKeyValue notificationMessageKeyValuevalid = new NotificationMessageKeyValue();
        notificationMessageKeyValuevalid.setFormkey("jsontest");
        notificationMessageKeyValuevalid.setFormvalue("@");
        subscriptionValid.getNotificationMessageKeyValues().add(notificationMessageKeyValuevalid);

        subscriptionValid.setNotificationMeta("kalle1.kalle2@domain.com");
        subscriptionValid.setNotificationType("MAIL");
        subscriptionValid.setRepeat(true);
        Requirement requirement = new Requirement();
        Condition condition = new Condition();
        condition.setJmespath("split('identity', '\\') | [2] =='com.mycompany.myproduct'");
        requirement.getConditions().add(condition);
        subscriptionValid.getRequirements().add(requirement);

        // subscriptionInvalid -------------------------
        subscriptionInvalid = new Subscription();

        subscriptionInvalid.setSubscriptionName("Kalle1");

        // new stuff
        subscriptionInvalid.setRestPostBodyMediaType(null);

        NotificationMessageKeyValue notificationMessageKeyValueinvalid = new NotificationMessageKeyValue();
        notificationMessageKeyValueinvalid.setFormkey("");
        notificationMessageKeyValueinvalid.setFormvalue("@");
        subscriptionInvalid.getNotificationMessageKeyValues()
                           .add(notificationMessageKeyValueinvalid);

        subscriptionInvalid.setNotificationMeta("kalle1.kal  le2@domain.com");
        subscriptionInvalid.setNotificationType("MAIL");
        subscriptionInvalid.setRepeat(true);
        Requirement requirementInvalid = new Requirement();
        Condition conditionInvalid = new Condition();
        conditionInvalid.setJmespath(
                "split(data.identity', '/') | [2] =='com.mycompany.myproduct'");
        requirementInvalid.getConditions().add(conditionInvalid);
        subscriptionInvalid.getRequirements().add(requirementInvalid);
    }

    /**
     * Validator unit tests for SubscriptionName parameter in Subscription. Valid "SubscriptionName"
     * values: - All letters and numbers: [A-Za-z0-9_].
     */
    @Test
    public void validateSubscriptionNameValidNameTest() throws Exception {
        String[] validSubscriptionNameList = { "Kalle", "kalle", "KALLE", "kall_e", "Kalle1",
                "KAlle_123" };
        for (String subscriptionName : validSubscriptionNameList) {
            try {
                invokeMethod(SubscriptionValidator.class, "validateSubscriptionName",
                        subscriptionName);
            } catch (SubscriptionValidationException e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void validateSubscriptionNameInvalidNamesTest() throws Exception {
        String[] invalidSubscriptionNameList = { "@Kalle", "Kal@le", "<Kalle", "Kall<e", "Kal*le",
                "*Kalle", "Kall||e", "Ka\\lle", "\\Kalle", "\\\\Kalle", "-K-a-l-l-e-", ",Kalle" };
        for (String subscriptionName : invalidSubscriptionNameList) {
            try {
                invokeMethod(SubscriptionValidator.class, "validateSubscriptionName",
                        subscriptionName);
                fail(String.format("%s should be an invalid name but was valid.",
                        subscriptionName));
            } catch (SubscriptionValidationException e) {
                continue;
            }
        }
    }

    @Test
    public void validateNotificationMessageValidMessageTest() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("");
        notificationMessageKeyValue.setFormvalue("@");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMessageKeyValues",
                    subscription.getNotificationMessageKeyValues(), "");
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateNotificationMessageValidMessage2Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("");
        notificationMessageKeyValue.setFormvalue(
                "{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMessageKeyValues",
                    subscription.getNotificationMessageKeyValues(),
                    MediaType.APPLICATION_JSON.toString());
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateNotificationMessageValidMessage3Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("json");
        notificationMessageKeyValue.setFormvalue(
                "{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMessageKeyValues",
                    subscription.getNotificationMessageKeyValues(),
                    MediaType.APPLICATION_FORM_URLENCODED.toString());
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateNotificationMessageValidMessage4Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("json");
        notificationMessageKeyValue.setFormvalue(
                "{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        NotificationMessageKeyValue notificationMessageKeyValue2 = new NotificationMessageKeyValue();
        notificationMessageKeyValue2.setFormkey("json2");
        notificationMessageKeyValue2.setFormvalue(
                "{parameter2: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue2);
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMessageKeyValues",
                    subscription.getNotificationMessageKeyValues(),
                    MediaType.APPLICATION_FORM_URLENCODED.toString());
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateNotificationMessageInvalidMessage1Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("mykey");
        notificationMessageKeyValue.setFormvalue("kalle.kalle@domain.com");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMessageKeyValues",
                    subscription.getNotificationMessageKeyValues(), "");
            fail("Validation did not throw exception when validating message");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    @Test
    public void validateNotificationMessageInvalidMessage2Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("mykey");
        notificationMessageKeyValue.setFormvalue(
                "{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMessageKeyValues",
                    subscription.getNotificationMessageKeyValues(),
                    MediaType.APPLICATION_JSON.toString());
            fail("Validation did not throw exception when validating message");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    @Test
    public void validateNotificationMessageInvalidMessage3Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("");
        notificationMessageKeyValue.setFormvalue(
                "{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMessageKeyValues",
                    subscription.getNotificationMessageKeyValues(),
                    MediaType.APPLICATION_FORM_URLENCODED.toString());
            fail("Expected subscriptionvalidation to throw exception.");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    @Test
    public void validateNotificationMessageInvalidMessage4Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("json");
        notificationMessageKeyValue.setFormvalue(
                "{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        NotificationMessageKeyValue notificationMessageKeyValue2 = new NotificationMessageKeyValue();
        notificationMessageKeyValue2.setFormkey("");
        notificationMessageKeyValue2.setFormvalue(
                "{parameter2: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue2);
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMessageKeyValues",
                    subscription.getNotificationMessageKeyValues(),
                    MediaType.APPLICATION_FORM_URLENCODED.toString());
            fail("Expected subscriptionvalidation to throw exception.");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    @Test
    public void validateNotificationMessageInvalidMessage5Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("json");
        notificationMessageKeyValue.setFormvalue("");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        NotificationMessageKeyValue notificationMessageKeyValue2 = new NotificationMessageKeyValue();
        notificationMessageKeyValue2.setFormkey("json");
        notificationMessageKeyValue2.setFormvalue(
                "{parameter2: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue2);
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMessageKeyValues",
                    subscription.getNotificationMessageKeyValues(),
                    MediaType.APPLICATION_FORM_URLENCODED.toString());
            fail("Expected subscriptionvalidation to throw exception.");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    @Test
    public void validateRestPostMediaTypeValidMessageTest() throws Exception {
        try {
            invokeMethod(SubscriptionValidator.class, "validateRestPostMediaType",
                    MediaType.APPLICATION_FORM_URLENCODED.toString());
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateRestPostMediaTypeValidMessage2Test() throws Exception {
        try {
            invokeMethod(SubscriptionValidator.class, "validateRestPostMediaType",
                    MediaType.APPLICATION_JSON.toString());
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateRestPostMediaTypeInvalidMessageTest() throws Exception {
        try {
            invokeMethod(SubscriptionValidator.class, "validateRestPostMediaType",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE.toString());
            fail("Expected subscritpionvalidation to throw Exception");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    @Test
    public void validateRestPostMediaTypeInvalidMessage2Test() throws Exception {
        try {
            invokeMethod(SubscriptionValidator.class, "validateRestPostMediaType", "");
            fail("Expected subscritpionvalidation to throw Exception");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    /**
     * Validator unit tests for NotificationMeta parameter in Subscription. Valid "NotificationMeta"
     * value: - "http://127.0.0.1:3000/ei/test_subscription_rest" - "kalle.kalle@domain.com"
     */
    @Test
    public void validateNotificationMetaValidMailMetaTest() throws Exception {
        String notificationMeta = "kalle.kalle@domain.com";
        String notificationType = "MAIL";
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMeta", notificationMeta,
                    notificationType);
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateNotificationMetaInvalidMailMetaTest() throws Exception {
        String notificationType = "MAIL";
        String[] invalidNotificationMeta = { "kalle. kalle@domain.com", "kalle.kalledomain.com",
                "kalle@kalle@domain.com", "kalle.kalle@domain", "kalle.kalle@" };

        for (String notificationMeta : invalidNotificationMeta) {
            try {
                invokeMethod(SubscriptionValidator.class, "validateNotificationMeta",
                        notificationMeta, notificationType);
                fail(String.format("%s should be invalid but was valid.", notificationMeta));
            } catch (SubscriptionValidationException e) {
                continue;
            }
        }

        String notificationMeta = "kalle.kall  e@domain.com";
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMeta", notificationMeta,
                    notificationType);
            fail("Expected subscritpionvalidation to throw Exception");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    @Test
    public void validateNotificationMetaInvalidMetaTest() throws Exception {
        String notificationMeta = "";
        String notificationType = "REST_POST";
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMeta", notificationMeta,
                    notificationType);
            fail("Expected subscritpionvalidation to throw Exception");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    @Test
    public void validateNotificationMetaValidRestPostMetaTest() throws Exception {
        String notificationMeta = "http://127.0.0.1:3000/ei/test_subscription_rest?json=links[?type=='SUBJECT'].target | [0]";
        String notificationType = "REST_POST";
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationMeta", notificationMeta,
                    notificationType);
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Validator unit tests for NotificationType parameter in Subscription. Valid "NotificationType"
     * value: true or false
     */
    @Test
    public void validateNotificationTypeValidTypeMAILTest() throws Exception {
        String notificationType = "MAIL";
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationType", notificationType);
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateNotificationTypeValidTypeRESTPOSTTest() throws Exception {
        String notificationType = "REST_POST";
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationType", notificationType);
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateNotificationTypeInvalidTypeTest() throws Exception {
        String notificationType = "INVALID_TYPE";
        try {
            invokeMethod(SubscriptionValidator.class, "validateNotificationType", notificationType);
            fail("Expected subscritpionvalidation to throw Exception");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    // TODO: Validator Unit tests for Jmepath syntax validator needs to be
    // implemented here.

    /**
     * Unit tests for testing a whole Subscription Json object. Valid Result: true or false
     */
    @Test
    public void validateFullSubscriptionWithValidSubscriptionParameters() {
        try {
            SubscriptionValidator.validateSubscription(subscriptionValid);
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateFullSubscriptionWithInvalidSubscriptionParameters() {
        try {
            SubscriptionValidator.validateSubscription(subscriptionInvalid);
            fail("Expected subscritpionvalidation to throw Exception");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }

    @Test
    public void validateSubscriptionWithSchemaTest() throws Exception {

        try {
            invokeMethod(SubscriptionValidator.class, "validateWithSchema", subscriptionValid);
        } catch (SubscriptionValidationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void validateSubscriptionWithoutName() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Subscription subscriptionValidCopy = objectMapper.readValue(
                objectMapper.writeValueAsString(subscriptionValid),
                Subscription.class);

        subscriptionValidCopy.setSubscriptionName(null);
        try {
            invokeMethod(SubscriptionValidator.class, "validateWithSchema", subscriptionValidCopy);
            fail("Expected subscritpionvalidation to throw Exception");
        } catch (SubscriptionValidationException e) {
            return;
        }
    }
}