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
package com.ericsson.ei.subscriptionhandler;

import com.ericsson.ei.controller.model.NotificationMessageKeyValue;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionValidationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.regex.Pattern;

public class SubscriptionValidator {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionValidator.class);

    /**
     * Validation of parameters values in subscriptions objects.
     * Throws SubscriptionValidationException if validation of a parameter fails due to wrong
     * format of parameter.
     *
     * @param subscription
     */
    public void validateSubscription(Subscription subscription) throws SubscriptionValidationException {
        LOG.debug("Validation of subscription " + subscription.getSubscriptionName() + " Started.");
        this.validateSubscriptionName(subscription.getSubscriptionName());
        this.validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(), subscription.getRestPostBodyMediaType());
        this.validateNotificationMeta(subscription.getNotificationMeta());
        this.validateNotificationType(subscription.getNotificationType());
        this.RestPostMediaType(subscription.getRestPostBodyMediaType());
//		List<Requirement> reqList = subscription.getRequirements();
//		for (int i=0; i < reqList.size(); i++) {
//			this.validateJmespath(reqList.get(i).getConditions().get(0).getJmespath());
//		}
        LOG.debug("Validating of subscription " + subscription.getSubscriptionName() + " finished successfully.");
    }

    /**
     * Validation of subscriptionName parameter
     * Throws SubscriptionValidationException if validation of the parameter fails due to wrong
     * format of parameter.
     *
     * @param subscriptionName
     */
    private void validateSubscriptionName(String subscriptionName) throws SubscriptionValidationException {
        String regex = "^[A-Za-z0-9_]+$";
        if (!Pattern.matches(regex, subscriptionName)) {
            throw new SubscriptionValidationException("Wrong format of SubscriptionName: " + subscriptionName);
        }
    }

    /**
     * Validation of NotificationMessageKeyValues parameters (key/values)
     * Throws SubscriptionValidationException if validation of the parameter fails due to wrong
     * format of parameter.
     *
     * @param notificationMessage
     * @param restPostBodyMediaType
     */
    private void validateNotificationMessageKeyValues(List<NotificationMessageKeyValue> notificationMessage, String restPostBodyMediaType) throws SubscriptionValidationException {
        for (NotificationMessageKeyValue item : notificationMessage) {
            String test_key = item.getFormkey();
            String test_value = item.getFormvalue();
            if (restPostBodyMediaType.equals(MediaType.APPLICATION_FORM_URLENCODED.toString())) { // FORM/POST PARAMETERS
                if ((test_key == null || test_key.isEmpty() || StringUtils.isBlank(test_key)) || (test_value == null || test_value.isEmpty() || StringUtils.isBlank(test_value))) {
                    throw new SubscriptionValidationException("Value & Key  in notificationMessage must have a values: " + notificationMessage);
                }
            } else {
                if (notificationMessage.size() != 1) {
                    throw new SubscriptionValidationException("Only one array is allowed for notificationMessage when NOT using key/value pairs: " + notificationMessage);
                } else if (test_key != null && !test_key.isEmpty()) {
                    throw new SubscriptionValidationException("Key in notificationMessage must be empty when NOT using key/value pairs: " + notificationMessage);
                } else if (StringUtils.isBlank(test_value)) {
                    throw new SubscriptionValidationException("Value in notificationMessage must have a value when NOT using key/value pairs: " + notificationMessage);
                }
            }
        }
    }

    /**
     * Validation of notificationMeta parameter
     * Throws SubscriptionValidationException if validation of the parameter fails due to wrong
     * format of parameter.
     *
     * @param notificationMeta
     */
    private void validateNotificationMeta(String notificationMeta) throws SubscriptionValidationException {
        String regex = ".*[\\s].*";
        if (Pattern.matches(regex, notificationMeta)) {
            throw new SubscriptionValidationException("Wrong format of NotificationMeta: " + notificationMeta);
        }
    }

    /**
     * Validation of notificationType parameter
     * Throws SubscriptionValidationException if validation of the parameter fails due to wrong
     * format of parameter.
     *
     * @param notificationType
     */
    private void validateNotificationType(String notificationType) throws SubscriptionValidationException {
        String regexMail = "[\\s]*MAIL[\\\\s]*";
        String regexRestPost = "[\\s]*REST_POST[\\\\s]*";
        if (!(Pattern.matches(regexMail, notificationType) || Pattern.matches(regexRestPost, notificationType))) {
            throw new SubscriptionValidationException("Wrong format of NotificationType: " + notificationType);
        }
    }

    private void RestPostMediaType(String restPostMediaType) throws SubscriptionValidationException {
        String regexApplication_JSON = "[\\s]*application/json[\\\\s]*";
        String regexApplicationFormUrlEncoded = "[\\s]*application/x-www-form-urlencoded[\\\\s]*";
        if (!(Pattern.matches(regexApplication_JSON, restPostMediaType) || Pattern.matches(regexApplicationFormUrlEncoded, restPostMediaType))) {
            throw new SubscriptionValidationException("Wrong format of RestPostMediaType: " + restPostMediaType);
        }
    }

//	public void validateJmespath(String jmespath) {
////		TODO: Validator for Jmepath syntax need to be implemented here.
//		throw new SubscriptionValidationException("Wrong format/syntax of Jmepath: " + jmespath);
//	}
}