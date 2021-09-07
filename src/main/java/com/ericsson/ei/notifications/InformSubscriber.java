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

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;

import com.ericsson.ei.mongo.MongoConstants;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.ei.exception.AbortExecutionException;
import com.ericsson.ei.exception.AuthenticationException;
import com.ericsson.ei.exception.MongoDBConnectionException;
import com.ericsson.ei.exception.NotificationFailureException;
import com.ericsson.ei.handlers.DateUtils;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongo.MongoDBHandler;
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
    @Value("${notification.retry:#{0}}")
    private int notificationRetry;

    @Getter
    @Value("${failed.notifications.collection.name}")
    private String failedNotificationCollectionName;

    @Getter
    @Value("${spring.data.mongodb.database}")
    private String database;

    @Getter
    @Value("${failed.notifications.collection.ttl}")
    private int failedNotificationsTtl;

    @Autowired
    private JmesPathInterface jmespath;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private HttpRequestFactory httpRequestFactory;
    
    @PostConstruct
    public void init() throws AbortExecutionException {
        try {
            if (failedNotificationsTtl > 0) {
                mongoDBHandler.createTTLIndex(database, failedNotificationCollectionName, MongoConstants.TIME,
                        failedNotificationsTtl);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create an index for {} due to: {}", failedNotificationCollectionName, e);
        }
    }
    /**
     * Extracts the mode of notification through which the subscriber should be notified, from the
     * subscription Object. And if the notification fails, then it saved in the database.
     *
     * @param aggregatedObject
     * @param subscriptionJson
     * @throws AuthenticationException, MongoDBConnectionException
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
            String failedNotification = prepareFailedNotification(aggregatedObject,
                    subscriptionName, notificationMeta, e.getMessage());
            LOGGER.debug(
                    "Failed to inform subscriber '{}'\nPrepared 'failed notification' document : {}",
                    e.getMessage(), failedNotification);
            saveFailedNotificationToDB(failedNotification);
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
        int requestTries = 0;
        Exception exception = null;
        do {
            requestTries++;
            try {
                request.perform();
                exception = null;
            } catch (AuthenticationException e) {
                exception = e;
                break;
            } catch (Exception e) {
                exception = e;
            }
            LOGGER.debug("After trying for {} time(s), the result is : {}", requestTries,
                    exception != null);
        } while (exception != null && requestTries <= notificationRetry);

        if (exception != null) {
            String errorMessage = "Failed to send REST/POST notification!";
            throw new NotificationFailureException(
                    errorMessage + "\nMessage: " + exception.getMessage());
        }
    }

    /**
     * Saves the failed Notification into a single document in the database.
     */
    private void saveFailedNotificationToDB(String failedNotification) {
        try {
            mongoDBHandler.insertDocument(database,
                    failedNotificationCollectionName, failedNotification);
            LOGGER.warn("Failed notification saved. Database: {} , Collection: {} ", database,
                    failedNotificationCollectionName);
        } catch (MongoWriteException e) {
            LOGGER.warn("Failed to insert the failed notification into database.", e);
        }
    }

    /**
     * Prepares the document to be saved in the failed notification database.
     *
     * @param aggregatedObject
     * @param subscriptionName
     * @param notificationMeta
     * @return String
     */
    private String prepareFailedNotification(String aggregatedObject, String subscriptionName,
            String notificationMeta, String errorMessage) {
        BasicDBObject document = new BasicDBObject();
        document.put("subscriptionName", subscriptionName);
        document.put("notificationMeta", notificationMeta);
        try {
            document.put(MongoConstants.TIME, DateUtils.getDate());
        } catch (ParseException e) {
            LOGGER.error("Failed to get date object.", e);
        }
        document.put("aggregatedObject", BasicDBObject.parse(aggregatedObject));
        document.put("message", errorMessage);
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