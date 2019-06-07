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

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.ericsson.ei.controller.model.NotificationMessageKeyValue;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionValidationException;
import com.ericsson.eiffelcommons.utils.RegExProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ListReportProvider;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonSchemaFactoryBuilder;

public class SubscriptionValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionValidator.class);
    private static final String SCHEMA_FILE_PATH = "/schemas/subscription_schema.json";

    /**
     * The private constructor forces all implementations to use the functions as
     * static methods.
     */
    private SubscriptionValidator() {
    }

    /**
     * Validation of parameters values in subscriptions objects. Throws
     * SubscriptionValidationException if validation of a parameter fails due to
     * wrong format of parameter.
     *
     * @param subscription
     */
    public static void validateSubscription(Subscription subscription) throws SubscriptionValidationException {
        LOGGER.debug("Validation of subscription " + subscription.getSubscriptionName() + " Started.");
        validateSubscriptionName(subscription.getSubscriptionName());
        validateNotificationMessageKeyValues(subscription.getNotificationMessageKeyValues(),
                subscription.getRestPostBodyMediaType());
        validateNotificationType(subscription.getNotificationType());
        validateNotificationMeta(subscription.getNotificationMeta(), subscription.getNotificationType());
        if (subscription.getNotificationType().equals("REST_POST")) {
            RestPostMediaType(subscription.getRestPostBodyMediaType());
        }
        validateWithSchema(subscription);
        LOGGER.debug("Validating of subscription " + subscription.getSubscriptionName() + " finished successfully.");
    }

    /**
     * Validation of subscriptionName parameter Throws
     * SubscriptionValidationException if validation of the parameter fails due to
     * wrong format of parameter.
     *
     * @param subscriptionName
     */
    private static void validateSubscriptionName(String subscriptionName) throws SubscriptionValidationException {
        // Here the regEx used is :/(\W)/ that matches anything that is not
        // [A-Z,a-z,0-8] and _.
        String invalidSubscriptionNameRegex = null;
        try {
            invalidSubscriptionNameRegex = RegExProvider.getRegExs().getString("invalidName");
        } catch (JSONException | IOException e) {
            LOGGER.error("Error message: " + e.getMessage(), e);
        }

        if (subscriptionName == null) {
            throw new SubscriptionValidationException("Required field SubscriptionName has not been set");
        } else if (invalidSubscriptionNameRegex == null || invalidSubscriptionNameRegex.isEmpty()) {
            throw new SubscriptionValidationException(
                    "A valid regular expression for validating subscription name is not provided.");
        } else if (Pattern.matches(invalidSubscriptionNameRegex, subscriptionName)) {
            throw new SubscriptionValidationException("Wrong format of SubscriptionName: " + subscriptionName);
        }
    }

    /**
     * Validation of NotificationMessageKeyValues parameters (key/values) Throws
     * SubscriptionValidationException if validation of the parameter fails due to
     * wrong format of parameter.
     *
     * @param notificationMessage
     * @param restPostBodyMediaType
     */

    private static void validateNotificationMessageKeyValues(List<NotificationMessageKeyValue> notificationMessage,
            String restPostBodyMediaType) throws SubscriptionValidationException {
        for (NotificationMessageKeyValue item : notificationMessage) {
            String testKey = item.getFormkey();
            String testValue = item.getFormvalue();
            if (restPostBodyMediaType != null
                    && restPostBodyMediaType.equals(MediaType.APPLICATION_FORM_URLENCODED.toString())) { // FORM/POST
                // PARAMETERS
                if (StringUtils.isBlank(testKey) || StringUtils.isBlank(testValue)) {
                    throw new SubscriptionValidationException(
                            "Value & Key  in notificationMessage must have a values: " + notificationMessage);
                }
            } else {
                if (notificationMessage.size() != 1) {
                    throw new SubscriptionValidationException(
                            "Only one array is allowed for notificationMessage when NOT using key/value pairs: "
                                    + notificationMessage);
                } else if (testKey != null && !testKey.isEmpty()) {
                    throw new SubscriptionValidationException(
                            "Key in notificationMessage must be empty when NOT using key/value pairs: "
                                    + notificationMessage);
                } else if (StringUtils.isBlank(testValue)) {
                    throw new SubscriptionValidationException(
                            "Value in notificationMessage must have a value when NOT using key/value pairs: "
                                    + notificationMessage);
                }
            }
        }
    }

    /**
     * Validation of notificationMeta parameter Throws
     * SubscriptionValidationException if validation of the parameter fails due to
     * wrong format of parameter.
     *
     * @param notificationMeta
     * @param notificationType
     */
    private static void validateNotificationMeta(String notificationMeta, String notificationType)
            throws SubscriptionValidationException {
        String regexMail = "[\\s]*MAIL[\\\\s]*";
        if (notificationMeta == null || notificationMeta.isEmpty()) {
            throw new SubscriptionValidationException("Required field NotificationMeta has not been set");
        }

        if (Pattern.matches(regexMail, notificationType)) {
            String[] addresses = notificationMeta.split(",");
            for (String address : addresses) {
                validateEmail(address.trim());
            }
        }
    }

    /**
     * Validation of notificationType parameter Throws
     * SubscriptionValidationException if validation of the parameter fails due to
     * wrong format of parameter.
     *
     * @param notificationType
     */
    private static void validateNotificationType(String notificationType) throws SubscriptionValidationException {
        String regexMail = "[\\s]*MAIL[\\\\s]*";
        String regexRestPost = "[\\s]*REST_POST[\\\\s]*";
        if (notificationType == null) {
            throw new SubscriptionValidationException("Required field NotificationType has not been set");
        } else if (!(Pattern.matches(regexMail, notificationType)
                || Pattern.matches(regexRestPost, notificationType))) {
            throw new SubscriptionValidationException("Wrong format of NotificationType: " + notificationType);
        }
    }

    private static void RestPostMediaType(String restPostMediaType) throws SubscriptionValidationException {
        String regexApplication_JSON = "[\\s]*application/json[\\\\s]*";
        String regexApplicationFormUrlEncoded = "[\\s]*application/x-www-form-urlencoded[\\\\s]*";
        if (restPostMediaType == null) {
            throw new SubscriptionValidationException("Required field RestPostMediaType has not been set");
        } else if (!(Pattern.matches(regexApplication_JSON, restPostMediaType)
                || Pattern.matches(regexApplicationFormUrlEncoded, restPostMediaType))) {
            throw new SubscriptionValidationException("Wrong format of RestPostMediaType: " + restPostMediaType);
        }
    }

    /**
     * Validation of email address Throws SubscriptionValidationException if
     * validation of the parameter fails due to wrong format of parameter.
     *
     * @param email
     */
    public static void validateEmail(String email) throws SubscriptionValidationException {
        String validEmailRegEx = null;
        try {
            validEmailRegEx = RegExProvider.getRegExs().getString("validEmail");
        } catch (JSONException | IOException e) {
            LOGGER.error("Error message: " + e.getMessage(), e);
        }

        if (validEmailRegEx == null || validEmailRegEx.isEmpty()) {
            throw new SubscriptionValidationException(
                    "A valid regular expression for subscription email validation is not provided");
        }
        final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile(validEmailRegEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        if (!(matcher.matches())) {
            throw new SubscriptionValidationException(
                    "Notification type is set to [MAIL] but the given notificatioMeta contains an invalid e-mail ["
                            + email + "]");
        }
    }

    public static void validateWithSchema(Subscription subscription) throws SubscriptionValidationException {
        LOGGER.debug("Validation of subscription " + subscription.getSubscriptionName() + " Started.");
        try {
            JsonNode subscriptionJson = objectToJson(subscription);
            JsonNode schemaObj = JsonLoader.fromResource(SCHEMA_FILE_PATH);
            JsonSchemaFactoryBuilder schemaFactoryBuilder = JsonSchemaFactory.newBuilder();
            schemaFactoryBuilder.setReportProvider(new ListReportProvider(LogLevel.INFO, LogLevel.ERROR));
            final JsonSchemaFactory factory = schemaFactoryBuilder.freeze();
            final JsonSchema schema = factory.getJsonSchema(schemaObj);
            ProcessingReport report = schema.validate(subscriptionJson);
            boolean waitreport = true;
        } catch (Exception e) {
            throw new SubscriptionValidationException("Schema validation fails" + e.getMessage());
        }
    }

    public static JsonNode objectToJson(Subscription subObject) throws SubscriptionValidationException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonSubscriptionObj1;
        try {
            jsonSubscriptionObj1 = mapper.valueToTree(subObject);
        } catch (Exception e) {
            LOGGER.error("Failed to create object to json" + "\nError message: " + e.getMessage(), e);
            throw new SubscriptionValidationException(
                    "Failed to create object to json" + "\nError message: " + e.getMessage());
        }
        return jsonSubscriptionObj1;
    }
}