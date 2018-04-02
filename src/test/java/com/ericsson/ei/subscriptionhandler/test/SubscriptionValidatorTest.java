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
package com.ericsson.ei.subscriptionhandler.test;


import com.ericsson.ei.controller.model.Condition;
import com.ericsson.ei.controller.model.NotificationMessageKeyValue;
import com.ericsson.ei.controller.model.Requirement;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionValidationException;
import com.ericsson.ei.subscriptionhandler.SubscriptionValidator;
import org.junit.Test;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;
import static org.powermock.reflect.Whitebox.invokeMethod;

@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
public class SubscriptionValidatorTest {

    private SubscriptionValidator subscriptionValidator;
    private Subscription subscriptionValid;
    private Subscription subscriptionInvalid;

    public SubscriptionValidatorTest() {
        subscriptionValidator = new SubscriptionValidator();


        // subscriptionValidator -------------------------
        subscriptionValid = new Subscription();

        subscriptionValid.setSubscriptionName("Kalle1");

        // new stuff
        subscriptionValid.setRestPostBodyMediaType(MediaType.APPLICATION_FORM_URLENCODED.toString());

        NotificationMessageKeyValue notificationMessageKeyValuevalid = new NotificationMessageKeyValue();
        notificationMessageKeyValuevalid.setFormkey("jsontest");
        notificationMessageKeyValuevalid.setFormvalue("@");
        subscriptionValid.getNotificationMessageKeyValues().add(notificationMessageKeyValuevalid);

        subscriptionValid.setNotificationMeta("kalle1.kalle2@domain.com");
        subscriptionValid.setNotificationType("MAIL");
        subscriptionValid.setRepeat(true);
        Requirement requirement = new Requirement();
        Condition condition = new Condition();
        condition.setJmespath("gav.groupId=='com.mycompany.myproduct'");
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
        subscriptionInvalid.getNotificationMessageKeyValues().add(notificationMessageKeyValueinvalid);

        subscriptionInvalid.setNotificationMeta("kalle1.kal  le2@domain.com");
        subscriptionInvalid.setNotificationType("MAIL");
        subscriptionInvalid.setRepeat(true);
        Requirement requirementInvalid = new Requirement();
        Condition conditionInvalid = new Condition();
        conditionInvalid.setJmespath("gav.groupId=='com.mycompany.myproduct'");
        requirementInvalid.getConditions().add(conditionInvalid);
        subscriptionInvalid.getRequirements().add(requirementInvalid);
    }

    /**
     * Validator unit tests for SubscriptionName parameter in Subscription.
     * Valid "SubscriptionName" values:
     * - All letters and numbers: [A-Za-z0-9_].
     */
    @Test
    public void validateSubscriptionNameValidNameTest() throws Exception {
        String subscriptionName = "Kalle1";
        try {
            invokeMethod(subscriptionValidator, "validateSubscriptionName", subscriptionName);
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateSubscriptionNameValidNameTest2() throws Exception {
        String subscriptionName = "Kalle_1";
        try {
            invokeMethod(subscriptionValidator, "validateSubscriptionName", subscriptionName);
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateSubscriptionNameInvalidNameTest() throws Exception {
        String subscriptionName = "Kal--l[[e1";
        try {
            invokeMethod(subscriptionValidator, "validateSubscriptionName", subscriptionName);
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void validateSubscriptionNameInvalidName2Test() throws Exception {
        String subscriptionName = "@Kal$le´1";
        try {
            invokeMethod(subscriptionValidator, "validateSubscriptionName", subscriptionName);
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void validateNotificationMessageValidMessageTest() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("");
        notificationMessageKeyValue.setFormvalue("@");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMessageKeyValues", subscription.getNotificationMessageKeyValues(), "");
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateNotificationMessageValidMessage2Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("");
        notificationMessageKeyValue.setFormvalue("{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMessageKeyValues", subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_JSON.toString());
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateNotificationMessageValidMessage3Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("json");
        notificationMessageKeyValue.setFormvalue("{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMessageKeyValues", subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_FORM_URLENCODED.toString());
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateNotificationMessageValidMessage4Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("json");
        notificationMessageKeyValue.setFormvalue("{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        NotificationMessageKeyValue notificationMessageKeyValue2 = new NotificationMessageKeyValue();
        notificationMessageKeyValue2.setFormkey("json2");
        notificationMessageKeyValue2.setFormvalue("{parameter2: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue2);
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMessageKeyValues", subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_FORM_URLENCODED.toString());
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateNotificationMessageInvalidMessage1Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("mykey");
        notificationMessageKeyValue.setFormvalue("kalle.kalle@domain.com");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMessageKeyValues", subscription.getNotificationMessageKeyValues(), "");
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void validateNotificationMessageInvalidMessage2Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("mykey");
        notificationMessageKeyValue.setFormvalue("{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMessageKeyValues", subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_JSON.toString());
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void validateNotificationMessageInvalidMessage3Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("");
        notificationMessageKeyValue.setFormvalue("{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMessageKeyValues", subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_FORM_URLENCODED.toString());
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void validateNotificationMessageInvalidMessage4Test() throws Exception {
        Subscription subscription = new Subscription();
        NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
        notificationMessageKeyValue.setFormkey("json");
        notificationMessageKeyValue.setFormvalue("{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);
        NotificationMessageKeyValue notificationMessageKeyValue2 = new NotificationMessageKeyValue();
        notificationMessageKeyValue2.setFormkey("");
        notificationMessageKeyValue2.setFormvalue("{parameter2: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue2);
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMessageKeyValues", subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_FORM_URLENCODED.toString());
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        assertTrue(false);
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
        notificationMessageKeyValue2.setFormvalue("{parameter2: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
        subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue2);
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMessageKeyValues", subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_FORM_URLENCODED.toString());
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void validateRestPostMediaTypeValidMessageTest() throws Exception {
        try {
            invokeMethod(subscriptionValidator, "RestPostMediaType", MediaType.APPLICATION_FORM_URLENCODED.toString());
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateRestPostMediaTypeValidMessage2Test() throws Exception {
        try {
            invokeMethod(subscriptionValidator, "RestPostMediaType", MediaType.APPLICATION_JSON.toString());
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateRestPostMediaTypeInvalidMessageTest() throws Exception {
        try {
            invokeMethod(subscriptionValidator, "RestPostMediaType", MediaType.APPLICATION_OCTET_STREAM_VALUE.toString());
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void validateRestPostMediaTypeInvalidMessage2Test() throws Exception {
        try {
            invokeMethod(subscriptionValidator, "RestPostMediaType", "");
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        assertTrue(false);
    }

    /**
     * Validator unit tests for NotificationMeta parameter in Subscription.
     * Valid "NotificationMeta" value:
     * - "http://127.0.0.1:3000/ei/test_subscription_rest"
     * - "kalle.kalle@domain.com"
     */
    @Test
    public void validateNotificationMetaValidMetaTest() throws Exception {
        String notificationMeta = "kalle.kalle@domain.com";
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMeta", notificationMeta);
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateNotificationMetaInvalidMetaTest() throws Exception {
        String notificationMeta = "kalle.kall  e@domain.com";
        try {
            invokeMethod(subscriptionValidator, "validateNotificationMeta", notificationMeta);
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), true);
            return;
        }
        assertTrue(false);
    }

    /**
     * Validator unit tests for NotificationType parameter in Subscription.
     * Valid "NotificationType" value: true or false
     */
    @Test
    public void validateNotificationTypeValidTypeMAILTest() throws Exception {
        String notificationType = "MAIL";
        try {
            invokeMethod(subscriptionValidator, "validateNotificationType", notificationType);
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateNotificationTypeValidTypeRESTPOSTTest() throws Exception {
        String notificationType = "REST_POST";
        try {
            invokeMethod(subscriptionValidator, "validateNotificationType", notificationType);
        } catch (SubscriptionValidationException e) {
            assertTrue(e.getMessage(), false);
            return;
        }
        assertTrue(true);
    }

    @Test
    public void validateNotificationTypeInvalidTypeTest() throws Exception {
        String notificationType = "INVALID_TYPE";
        try {
            invokeMethod(subscriptionValidator, "validateNotificationType", notificationType);
        } catch (SubscriptionValidationException e) {
            assertTrue(true);
            return;
        }
        assertTrue(false);
    }

	
	// TODO: Validator Unit tests for Jmepath syntax validator needs to be implemented here.
	
	
	/**
	 * Unit tests for testing a whole  Subscription Json object.
	 * Valid Result: true or false
	 */
	@Test
	public void validateFullSubscriptionWithValidSubscriptionParameters() {
		try {
			subscriptionValidator.validateSubscription(subscriptionValid);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}
	
	@Test
	public void validateFullSubscriptionWithInvalidSubscriptionParameters() {
		try {
			subscriptionValidator.validateSubscription(subscriptionInvalid);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}
}