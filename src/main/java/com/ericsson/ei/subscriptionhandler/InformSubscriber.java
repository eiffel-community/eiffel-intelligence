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

import java.net.URI;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.ei.handlers.DateUtils;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.BasicDBObject;

import lombok.Getter;

/**
 * This class represents the REST POST notification mechanism and the alternate
 * way to save the aggregatedObject details in the database when the
 * notification fails.
 *
 * @author xjibbal
 */

@Component
public class InformSubscriber {

    private static final Logger LOGGER = LoggerFactory.getLogger(InformSubscriber.class);
    // Regular expression for replacement unexpected character like \"|
    private static final String REGEX = "^\"|\"$";

    @Getter
    @Value("${notification.failAttempt}")
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
    private SpringRestTemplate restTemplate;

    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private SendMail sendMail;

    /**
     * This method extracts the mode of notification through which the
     * subscriber should be notified, from the subscription Object. And if the
     * notification fails, then it saved in the database.
     *
     * @param aggregatedObject
     * @param subscriptionJson
     */
    public void informSubscriber(String aggregatedObject, JsonNode subscriptionJson) {
        String subscriptionName = getSubscriptionField("subscriptionName", subscriptionJson);
        String notificationType = getSubscriptionField("notificationType", subscriptionJson);
        String notificationMeta = getSubscriptionField("notificationMeta", subscriptionJson);

        String subject = getSubscriptionField("emailSubject", subscriptionJson);

        MultiValueMap<String, String> mapNotificationMessage = mapNotificationMessage(aggregatedObject,
                subscriptionJson);

        if (notificationType.trim().equals("REST_POST")) {
            LOGGER.debug("Notification through REST_POST");
            boolean success = false;

            if (notificationMeta.contains("?")) {
                LOGGER.debug("Unformatted notificationMeta = " + notificationMeta);
                notificationMeta = reformatNotificationMeta(aggregatedObject, notificationMeta);
                LOGGER.debug("Formatted notificationMeta = " + notificationMeta);
            }

            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers = prepareHeaders(headers, subscriptionJson);

            success = restTemplate.postDataMultiValue(notificationMeta, mapNotificationMessage, headers);

            if (!success && failAttempt > 0) {
                for (int i = 0; i < failAttempt; i++) {
                    success = restTemplate.postDataMultiValue(notificationMeta, mapNotificationMessage, headers);
                    LOGGER.debug("After retrying for " + (i + 1) + " times, the result is : " + success);
                    if (success) {
                        break;
                    }
                }
            }

            if (!success) {
                saveMissedNotificationToDB(aggregatedObject, subscriptionName, notificationMeta);
            }
            return;
        }

        if (notificationType.trim().equals("MAIL")) {
            LOGGER.debug("Notification through EMAIL");
            try {
                sendMail.sendMail(notificationMeta, String.valueOf((mapNotificationMessage.get("")).get(0)), subject);
            } catch (MessagingException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
            return;
        }

    }

    /**
     * This method prepares headers to be used when making a post rest call
     *
     * @param headers
     * @param subscriptionJson
     * @return
     */
    private HttpHeaders prepareHeaders(HttpHeaders headers, JsonNode subscriptionJson) {
        // Setting Content Type
        String headerContentMediaType = getSubscriptionField("restPostBodyMediaType", subscriptionJson);
        headers.setContentType(MediaType.valueOf(headerContentMediaType));

        // Adding authentication if any
        String authType = getSubscriptionField("authenticationType", subscriptionJson);
        if (authType.equals("BASIC_AUTH")) {
            String username = getSubscriptionField("userName", subscriptionJson);
            String password = getSubscriptionField("password", subscriptionJson);

            if (!username.equals("") && !password.equals("")) {
                String encoding = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
                headers.add("Authorization", "Basic " + encoding);
            } else {
                LOGGER.error(
                        "userName/password field in subscription is missing. Make sure both are provided for BASIC_AUTH.");
            }

            String jenkinsCrumb = fetchJenkinsCrumbIfAny();
            if (jenkinsCrumb != null) {
                headers.add("Authorization", jenkinsCrumb);
            }

        }

        return headers;
    }

    private String fetchJenkinsCrumbIfAny() {
        String jenkinsCrumb = null;
        // String jenkinsCrumb = restTemplate.fetchJenkinsCrumbIfAny();
        return jenkinsCrumb;
    }

    /**
     * This method reformats notificationMeta and fetches values if applicable
     * for the requested parameters.
     *
     * @param aggregatedObject
     * @param notificationMeta
     * @return String
     */
    private String reformatNotificationMeta(String aggregatedObject, String notificationMeta) {
        String URL = notificationMeta.split("\\?")[0];

        List<NameValuePair> params = null;
        try {
            params = URLEncodedUtils.parse(new URI(notificationMeta), Charset.forName("UTF-8"));
        } catch (Exception e) {
            LOGGER.error("Failed to parse url parameters from '" + notificationMeta + "'.\nException message: "
                    + e.getMessage(), e);
        }

        List<NameValuePair> processedParams = new ArrayList<>();
        for (NameValuePair param : params) {
            String name = param.getName(), value = param.getValue();
            LOGGER.debug("Input parameter key and value: " + name + " : " + value);
            value = jmespath.runRuleOnEvent(value.replaceAll(REGEX, ""), aggregatedObject).toString().replaceAll(REGEX,
                    "");

            processedParams.add(new BasicNameValuePair(name, value));
            LOGGER.debug("Formatted parameter key and value: " + name + " : " + value);
        }
        String encodedQuery = URLEncodedUtils.format(processedParams, "UTF8");

        notificationMeta = URL + "?" + encodedQuery;
        return notificationMeta;
    }

    /**
     * This method saves the missed Notification into a single document along
     * with Subscription name, notification meta and time period.
     *
     * @param aggregatedObject
     * @param subscriptionName
     * @param notificationMeta
     */
    private void saveMissedNotificationToDB(String aggregatedObject, String subscriptionName, String notificationMeta) {
        String input = prepareMissedNotification(aggregatedObject, subscriptionName, notificationMeta);
        LOGGER.debug("Input missed Notification document : " + input);
        mongoDBHandler.createTTLIndex(missedNotificationDataBaseName, missedNotificationCollectionName, "Time",
                ttlValue);
        boolean output = mongoDBHandler.insertDocument(missedNotificationDataBaseName, missedNotificationCollectionName,
                input);

        if (!output) {
            LOGGER.debug("Failed to insert the notification into database");
        } else
            LOGGER.debug("Notification saved in the database");
    }

    /**
     * This method prepares the document to be saved in missed notification DB.
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
            LOGGER.error(e.getMessage(), e);
        }
        document.put("AggregatedObject", BasicDBObject.parse(aggregatedObject));
        return document.toString();
    }

    /**
     * This method, given the field name, returns its value
     *
     * @param subscriptionJson
     * @param fieldName
     * @return field value
     */
    private String getSubscriptionField(String fieldName, JsonNode subscriptionJson) {
        String value;
        if (subscriptionJson.get(fieldName) != null) {
            value = subscriptionJson.get(fieldName).toString().replaceAll(REGEX, "");
            LOGGER.debug("Extracted field name and value from subscription json:" + fieldName + " : " + value);
        } else {
            value = "";
        }
        return value;
    }

    /**
     * This method extracting key and value from subscription
     *
     * @param aggregatedObject
     * @param subscriptionJson
     * @return
     */
    private MultiValueMap<String, String> mapNotificationMessage(String aggregatedObject, JsonNode subscriptionJson) {
        MultiValueMap<String, String> mapNotificationMessage = new LinkedMultiValueMap<>();
        ArrayNode arrNode = (ArrayNode) subscriptionJson.get("notificationMessageKeyValues");

        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                mapNotificationMessage.add(objNode.get("formkey").toString().replaceAll(REGEX, ""), jmespath
                        .runRuleOnEvent(objNode.get("formvalue").toString().replaceAll(REGEX, ""), aggregatedObject)
                        .toString().replaceAll(REGEX, ""));
            }
            ;
        }
        return mapNotificationMessage;
    }

    /**
     * This method is responsible to display the configurable application
     * properties and to create TTL index on the missed Notification collection.
     */
    @PostConstruct
    public void init() {
        LOGGER.debug("missedNotificationCollectionName : " + missedNotificationCollectionName);
        LOGGER.debug("missedNotificationDataBaseName : " + missedNotificationDataBaseName);
        LOGGER.debug("notification.failAttempt : " + failAttempt);
        LOGGER.debug("Missed Notification TTL value : " + ttlValue);
    }
}