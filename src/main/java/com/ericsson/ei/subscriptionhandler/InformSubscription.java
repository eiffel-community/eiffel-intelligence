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

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import lombok.Getter;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class represents the REST POST notification mechanism and the alternate
 * way to save the aggregatedObject details in the database when the
 * notification fails.
 *
 * @author xjibbal
 */

@Component
public class InformSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(InformSubscription.class);
    // Regular expression for replacement unexpected character like \"|
    private static final String REGEX = "^\"|\"$";
    private String key = "";
    private String val = "";

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

        MultiValueMap<String, String> mapNotificationMessage = mapNotificationMessage(aggregatedObject, subscriptionJson);

        if (notificationMeta.contains("?")) {
            LOGGER.debug("Unformatted notificationMeta = " + notificationMeta);
            notificationMeta = reformatNotificationMeta(aggregatedObject, notificationMeta);
            LOGGER.debug("Formatted notificationMeta = " + notificationMeta);
        }

        if (notificationType.trim().equals("REST_POST")) {
            LOGGER.debug("Notification through REST_POST");
            int result;
            String headerContentMediaType = subscriptionJson.get("restPostBodyMediaType").toString().replaceAll(REGEX,
                    "");
            LOGGER.debug("headerContentMediaType : " + headerContentMediaType);
            if (!key.isEmpty() && !val.isEmpty()) {
                result = restTemplate.postDataMultiValue(notificationMeta, mapNotificationMessage,
                        headerContentMediaType, key, val);
            } else {
                result = restTemplate.postDataMultiValue(notificationMeta, mapNotificationMessage,
                        headerContentMediaType);
            }

            if (result == HttpStatus.OK.value() || result == HttpStatus.CREATED.value()
                    || result == HttpStatus.NO_CONTENT.value()) {
                LOGGER.debug("The result is : " + result);
            } else {
                for (int i = 0; i < failAttempt; i++) {
                    result = restTemplate.postDataMultiValue(notificationMeta, mapNotificationMessage,
                            headerContentMediaType);
                    LOGGER.debug("After retrying for " + (i + 1) + " times, the result is : " + result);
                    if (result == HttpStatus.OK.value())
                        break;
                }
                if (result != HttpStatus.OK.value() && result != HttpStatus.CREATED.value()
                        && result != HttpStatus.NO_CONTENT.value()) {
                    String input = prepareMissedNotification(aggregatedObject, subscriptionName, notificationMeta);
                    LOGGER.debug("Input missed Notification document : " + input);
                    mongoDBHandler.createTTLIndex(missedNotificationDataBaseName, missedNotificationCollectionName,
                            "Time", ttlValue);
                    boolean output = mongoDBHandler.insertDocument(missedNotificationDataBaseName,
                            missedNotificationCollectionName, input);
                    LOGGER.debug("The output of insertion of missed Notification : " + output);
                    if (!output) {
                        LOGGER.debug("Failed to insert the notification into database");
                    } else
                        LOGGER.debug("Notification saved in the database");
                }
            }
        } else if (notificationType.trim().equals("MAIL")) {
            LOGGER.debug("Notification through EMAIL");
            try {
                sendMail.sendMail(notificationMeta, String.valueOf((mapNotificationMessage.get("")).get(0)));
            } catch (MessagingException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
        }
        //set as empty because we should avoid leaking of authentication details
        key = "";
        val = "";
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
            LOGGER.error("Failed to parse url parameters from '" + notificationMeta + "'.\nException message: " + e.getMessage(), e);
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
     * @return String
     */
    private String prepareMissedNotification(String aggregatedObject, String subscriptionName, String notificationMeta) {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = dateFormat.format(date);
        try {
            date = dateFormat.parse(time);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        BasicDBObject document = new BasicDBObject();
        document.put("subscriptionName", subscriptionName);
        document.put("notificationMeta", notificationMeta);
        document.put("Time", date);
        document.put("AggregatedObject", JSON.parse(aggregatedObject));
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
        String value = subscriptionJson.get(fieldName).toString().replaceAll(REGEX, "");
        LOGGER.debug("Extracted field name and value from subscription json:" + fieldName + " : " + value);
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
                if (objNode.get("formkey").toString().replaceAll(REGEX, "").equals("Authorization")) {
                    key = "Authorization";
                    val = objNode.get("formvalue").toString().replaceAll(REGEX, "");
                } else {
                    mapNotificationMessage.add(objNode.get("formkey").toString().replaceAll(REGEX, ""), jmespath
                            .runRuleOnEvent(objNode.get("formvalue").toString().replaceAll(REGEX, ""), aggregatedObject)
                            .toString().replaceAll(REGEX, ""));
                }
            }
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