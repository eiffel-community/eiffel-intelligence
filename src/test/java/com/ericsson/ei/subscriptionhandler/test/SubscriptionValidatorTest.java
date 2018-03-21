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


import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.controller.model.NotificationMessageKeyValue;
import com.ericsson.ei.exception.SubscriptionValidationException;
import com.ericsson.ei.controller.model.Condition;
import com.ericsson.ei.controller.model.Requirement;
import com.ericsson.ei.subscriptionhandler.SubscriptionValidator;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
public class SubscriptionValidatorTest {
	
	SubscriptionValidator subscriptionValidator;
	Subscription subscritptionValid;
	Subscription subscritptionInvalid;
	
	public SubscriptionValidatorTest() {
		subscriptionValidator = new SubscriptionValidator();


		// subscriptionValidator -------------------------
		subscritptionValid = new Subscription();

		subscritptionValid.setSubscriptionName("Kalle1");

		// new stuff
		subscritptionValid.setRestPostBodyMediaType(MediaType.APPLICATION_FORM_URLENCODED.toString());

		NotificationMessageKeyValue notificationMessageKeyValuevalid = new NotificationMessageKeyValue();
		notificationMessageKeyValuevalid.setFormkey("jsontest");
		notificationMessageKeyValuevalid.setFormvalue("@");
		subscritptionValid.getNotificationMessageKeyValues().add(notificationMessageKeyValuevalid);

		subscritptionValid.setNotificationMeta("kalle1.kalle2@domain.com");
		subscritptionValid.setNotificationType("MAIL");
		subscritptionValid.setRepeat(true);
		Requirement requirement = new Requirement();
		Condition condition = new Condition();
		condition.setJmespath("gav.groupId=='com.mycompany.myproduct'");
		requirement.getConditions().add(condition);
		subscritptionValid.getRequirements().add(requirement);

		// subscritptionInvalid -------------------------
		subscritptionInvalid = new Subscription();

		subscritptionInvalid.setSubscriptionName("Kalle1");

		// new stuff
		subscritptionInvalid.setRestPostBodyMediaType(null);

		NotificationMessageKeyValue notificationMessageKeyValueinvalid = new NotificationMessageKeyValue();
		notificationMessageKeyValueinvalid.setFormkey("");
		notificationMessageKeyValueinvalid.setFormvalue("@");
		subscritptionInvalid.getNotificationMessageKeyValues().add(notificationMessageKeyValueinvalid);

		subscritptionInvalid.setNotificationMeta("kalle1.kal  le2@domain.com");
		subscritptionInvalid.setNotificationType("MAIL");
		subscritptionInvalid.setRepeat(true);
		Requirement requirementInvalid = new Requirement();
		Condition conditionInvalid = new Condition();
		conditionInvalid.setJmespath("gav.groupId=='com.mycompany.myproduct'");
		requirementInvalid.getConditions().add(conditionInvalid);
		subscritptionInvalid.getRequirements().add(requirementInvalid);
	}
	
	/*
	 * Validator unit tests for SubscriptionName parameter in Subscription.
	 * Valid "SubscriptionName" values:
	 * - All letters and numbers: [A-Za-z0-9_].
	 *
	 */
	@Test
	public void validateSubscriptionNameValidNameTest() {
		String subscriptionName = "Kalle1";
		try {
			subscriptionValidator.validateSubscriptionName(subscriptionName);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}
	
	@Test
	public void validateSubscriptionNameValidNameTest2() {
		String subscriptionName = "Kalle_1";
		try {
			subscriptionValidator.validateSubscriptionName(subscriptionName);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}
	
	@Test
	public void validateSubscriptionNameInvalidNameTest() {
		String subscriptionName = "Kal--l[[e1";
		try {
			subscriptionValidator.validateSubscriptionName(subscriptionName);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), true);
			return;
		}
		assertTrue(false);
	}
	
	@Test
	public void validateSubscriptionNameInvalidName2Test() {
		String subscriptionName = "@Kal$le´1";
		try {
			subscriptionValidator.validateSubscriptionName(subscriptionName);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), true);
			return;
		}
		assertTrue(false);
	}
	

	@Test
	public void validateNotificationMessageValidMessageTest() {

		Subscription subscription = new Subscription();
		NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
		notificationMessageKeyValue.setFormkey("");
		notificationMessageKeyValue.setFormvalue("@");
		subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);

		try {
			subscriptionValidator.validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(), "");
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}


	@Test
	public void validateNotificationMessageValidMessage2Test() {

		Subscription subscription = new Subscription();
		NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
		notificationMessageKeyValue.setFormkey("");
		notificationMessageKeyValue.setFormvalue("{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
		subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);

		try {
			subscriptionValidator.validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_JSON.toString());
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}


	@Test
	public void validateNotificationMessageValidMessage3Test() {

		Subscription subscription = new Subscription();
		NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
		notificationMessageKeyValue.setFormkey("json");
		notificationMessageKeyValue.setFormvalue("{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
		subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);

		try {
			subscriptionValidator.validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_FORM_URLENCODED.toString());
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}

	@Test
	public void validateNotificationMessageValidMessage4Test() {

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
			subscriptionValidator.validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_FORM_URLENCODED.toString());
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}


	@Test
	public void validateNotificationMessageInvalidMessage1Test() {

		Subscription subscription = new Subscription();
		NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
		notificationMessageKeyValue.setFormkey("mykey");
		notificationMessageKeyValue.setFormvalue("kalle.kalle@domain.com");
		subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);

		try {
			subscriptionValidator.validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(), "");
		} catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), true);
			return;
		}
		assertTrue(false);
	}


	@Test
	public void validateNotificationMessageInvalidMessage2Test() {

		Subscription subscription = new Subscription();
		NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
		notificationMessageKeyValue.setFormkey("mykey");
		notificationMessageKeyValue.setFormvalue("{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
		subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);

		try {
			subscriptionValidator.validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_JSON.toString());
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), true);
			return;
		}
		assertTrue(false);
	}


	@Test
	public void validateNotificationMessageInvalidMessage3Test() {

		Subscription subscription = new Subscription();
		NotificationMessageKeyValue notificationMessageKeyValue = new NotificationMessageKeyValue();
		notificationMessageKeyValue.setFormkey("");
		notificationMessageKeyValue.setFormvalue("{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}");
		subscription.getNotificationMessageKeyValues().add(notificationMessageKeyValue);

		try {
			subscriptionValidator.validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_FORM_URLENCODED.toString());
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), true);
			return;
		}
		assertTrue(false);
	}


	@Test
	public void validateNotificationMessageInvalidMessage4Test() {

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
			subscriptionValidator.validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_FORM_URLENCODED.toString());
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), true);
			return;
		}
		assertTrue(false);
	}

	@Test
	public void validateNotificationMessageInvalidMessage5Test() {

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
			subscriptionValidator.validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(), MediaType.APPLICATION_FORM_URLENCODED.toString());
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), true);
			return;
		}
		assertTrue(false);
	}

	@Test
	public void validateRestPostMediaTypeValidMessageTest() {

		try {
			subscriptionValidator.RestPostMediaType(MediaType.APPLICATION_FORM_URLENCODED.toString());
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}

	@Test
	public void validateRestPostMediaTypeValidMessage2Test() {

		try {
			subscriptionValidator.RestPostMediaType(MediaType.APPLICATION_JSON.toString());
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}

	@Test
	public void validateRestPostMediaTypeInvalidMessageTest() {

		try {
			subscriptionValidator.RestPostMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE.toString());
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), true);
			return;
		}
		assertTrue(false);
	}

	@Test
	public void validateRestPostMediaTypeInvalidMessage2Test() {

		try {
			subscriptionValidator.RestPostMediaType("");
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), true);
			return;
		}
		assertTrue(false);
	}

	/*
	 * Validator unit tests for NotificationMeta parameter in Subscription.
	 * Valid "NotificationMeta" value:
	 * - "http://127.0.0.1:3000/ei/test_subscription_rest"
	 * - "kalle.kalle@domain.com"
	 */
	@Test
	public void validateNotificationMetaValidMetaTest() {
		String notificationMeta = "kalle.kalle@domain.com";
		try {
			subscriptionValidator.validateNotificationMeta(notificationMeta);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}

	@Test
	public void validateNotificationMetaInvalidMetaTest() {
		String notificationMeta = "kalle.kall  e@domain.com";
		try {
			subscriptionValidator.validateNotificationMeta(notificationMeta);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), true);
			return;
		}
		assertTrue(false);
	}
	
	/*
	 * Validator unit tests for NotificationType parameter in Subscription.
	 * Valid "NotificationType" value: true or false
	 */
	@Test
	public void validateNotificationTypeValidTypeMAILTest() {
		String notificationType = "MAIL";
		try {
			subscriptionValidator.validateNotificationType(notificationType);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}
	
	@Test
	public void validateNotificationTypeValidTypeRESTPOSTTest() {
		String notificationType = "REST_POST";
		try {
			subscriptionValidator.validateNotificationType(notificationType);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);
	}
	
	@Test
	public void validateNotificationTypeInvalidTypeTest() {
		String notificationType = "INVALID_TYPE";
		try {
			subscriptionValidator.validateNotificationType(notificationType);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}

	
	// TODO: Validator Unit tests for Jmepath syntax validator needs to be implemented here.
	
	
	/*
	 * Unit tests for testing a whole  Subscription Json object.
	 * Valid Result: true or false
	 */
	@Test
	public void validateFullSubscriptionWithValidSubscriptionParameters() {
		try {
			subscriptionValidator.validateSubscription(subscritptionValid);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(e.getMessage(), false);
			return;
		}
		assertTrue(true);;
	}
	
	@Test
	public void validateFullSubscriptionWithInvalidSubscriptionParameters() {
		try {
			subscriptionValidator.validateSubscription(subscritptionInvalid);
		}
		catch (SubscriptionValidationException e) {
			assertTrue(true);
			return;
		}
		assertTrue(false);
	}
	
}
