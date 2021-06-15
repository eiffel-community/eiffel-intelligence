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
package com.ericsson.ei.notifications;

import java.text.ParseException;

import javax.mail.internet.MimeMessage;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.exception.NotificationFailureException;
import com.ericsson.ei.handlers.DateUtils;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.notifications.HttpRequest.HttpRequestFactory;
import com.ericsson.ei.utils.SubscriptionField;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the notification mechanism and the alternate way to save the aggregatedObject details
 * in the database when the notification fails.
 *
 * @author xjibbal
 */

@Component
public class InformSubscriber {

    private static final Logger LOGGER = LoggerFactory.getLogger(InformSubscriber.class);
    private static final String REGEX = "^\"|\"$";

    @Setter
    @Getter
    @Value("${notification.failAttempt:#{0}}")
    private int failAttempt;

    @Getter
    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Getter
    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDataBaseName;

    @Getter
    @Value("${notification.ttl.value}")
    private int ttlValue;

    @Autowired
    private JmesPathInterface jmespath;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private HttpRequestFactory httpRequestFactory;

    /**
     * Extracts the mode of notification through which the subscriber should be notified, from the
     * subscription Object. And if the notification fails, then it saved in the database.
     *
     * @param aggregatedObject
     * @param subscriptionJson
     * @throws AuthenticationException
     */
    public void informSubscriber(String aggregatedObject, JsonNode subscriptionJson)
            throws AuthenticationException, MongoDBConnectionException {
        SubscriptionField subscriptionField = new SubscriptionField(subscriptionJson);
        String notificationType = subscriptionField.get("notificationType");
        String notificationMeta = subscriptionField.get("notificationMeta");

        MultiValueMap<String, String> mapNotificationMessage = mapNotificationMessage(
                aggregatedObject, subscriptionJson);

        try {
            if (notificationType.trim().equals("REST_POST")) {
                LOGGER.debug("Notification through REST_POST");

                HttpRequest request = httpRequestFactory.createHttpRequest();
                request.setAggregatedObject(aggregatedObject)
                       .setMapNotificationMessage(mapNotificationMessage)
                       .setSubscriptionJson(subscriptionJson)
                       .setUrl(notificationMeta)
                       .build();
                makeHTTPRequests(request);
            }

            if (notificationType.trim().equals("MAIL")) {
                LOGGER.debug("Notification through EMAIL");
                String subject = subscriptionField.get("emailSubject");
                String emailBody = String.valueOf((mapNotificationMessage.get("")).get(0));
                MimeMessage message = emailSender.prepareEmailMessage(notificationMeta, emailBody,
                        subject);
                emailSender.sendEmail(message);
            }
        } catch (NotificationFailureException | AuthenticationException | EncryptionOperationNotPossibleException e) {
            String subscriptionName = subscriptionField.get("subscriptionName");
            String missedNotification = prepareMissedNotification(aggregatedObject,
                    subscriptionName, notificationMeta);
            LOGGER.debug(
                    "Failed to inform subscriber '{}'\nPrepared 'missed notification' document : {}",
                    e.getMessage(), missedNotification);
            mongoDBHandler.createTTLIndex(missedNotificationDataBaseName,
                    missedNotificationCollectionName, "Time", ttlValue);
            saveMissedNotificationToDB(missedNotification);
        }
    }

    /**
     * Attempts to make HTTP POST requests. If the request fails, it is retried until the maximum
     * number of failAttempts have been reached.
     *
     * @param request
     * @throws AuthenticationException, NotificationFailureException
     */
    private void makeHTTPRequests(HttpRequest request)
            throws AuthenticationException, NotificationFailureException {
        boolean success = false;
        int requestTries = 0;

        do {
            requestTries++;
            success = request.perform();
            LOGGER.debug("After trying for {} time(s), the result is : {}", requestTries, success);
        } while (!success && requestTries <= failAttempt);

        if (!success) {
            throw new NotificationFailureException("Failed to send HTTP notification!");
        }
    }

    /**
     * Saves the missed Notification into a single document in the database.
     */
    private void saveMissedNotificationToDB(String missedNotification) {
        try {
            mongoDBHandler.insertDocument(missedNotificationDataBaseName,
                    missedNotificationCollectionName, missedNotification);
            LOGGER.debug("Missed notification saved in database: {} ", missedNotificationCollectionName);
        } catch (MongoWriteException e) {
            LOGGER.debug("Failed to insert the missed notification into database.", e);
        }
    }

    /**
     * Prepares the document to be saved in the missed notification database.
     *
     * @param aggregatedObject
     * @param subscriptionName
     * @param notificationMeta
     * @return String
     */
    private String prepareMissedNotification(String aggregatedObject, String subscriptionName,
            String notificationMeta) {
        BasicDBObject document = new BasicDBObject();
        document.put("subscriptionName", subscriptionName);
        document.put("notificationMeta", notificationMeta);
        try {
            document.put("Time", DateUtils.getDate());
        } catch (ParseException e) {
            LOGGER.error("Failed to get date object.", e);
        }
        document.put("AggregatedObject", BasicDBObject.parse(aggregatedObject));
        return document.toString();
    }

    /**
     * Extracts key and value from notification message in a given subscription.
     *
     * @param aggregatedObject
     * @param subscriptionJson
     * @return
     */
    private MultiValueMap<String, String> mapNotificationMessage(String aggregatedObject,
            JsonNode subscriptionJson) {
        MultiValueMap<String, String> mapNotificationMessage = new LinkedMultiValueMap<>();
        ArrayNode arrNode = (ArrayNode) subscriptionJson.get("notificationMessageKeyValues");

        if (arrNode.isArray()) {
            LOGGER.debug("Running JMESPath extraction on form values.");

            for (final JsonNode objNode : arrNode) {
                String formKey = objNode.get("formkey").asText();
                String preJMESPathExtractionFormValue = objNode.get("formvalue").asText();

                JsonNode extractedJsonNode = jmespath.runRuleOnEvent(preJMESPathExtractionFormValue,
                        aggregatedObject);
                String postJMESPathExtractionFormValue = extractedJsonNode.toString()
                                                                          .replaceAll(
                                                                                  REGEX, "");

                LOGGER.debug("formValue after running the extraction: [{}] for formKey: [{}]",
                        postJMESPathExtractionFormValue, formKey);

                mapNotificationMessage.add(formKey, postJMESPathExtractionFormValue);
            }
        }
        return mapNotificationMessage;
    }

}