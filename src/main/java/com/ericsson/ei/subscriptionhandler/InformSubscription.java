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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * This class represents the REST POST notification mechanism and the alternate
 * way to save the aggregatedObject details in the database when the
 * notification fails.
 * 
 * @author xjibbal
 * 
 */

@Component
public class InformSubscription {

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
    SpringRestTemplate restTemplate;

    @Autowired
    MongoDBHandler mongoDBHandler;

    @Autowired
    SendMail sendMail;

    static Logger log = (Logger) LoggerFactory.getLogger(InformSubscription.class);

    /**
     * This method extracts the mode of notification through which the subscriber
     * should be notified, from the subscription Object. And if the notification
     * fails, then it saved in the database.
     * 
     * @param aggregatedObject
     * @param subscriptionJson
     */
    public void informSubscriber(String aggregatedObject, JsonNode subscriptionJson) {
        String subscriptionName = subscriptionJson.get("subscriptionName").toString().replaceAll("^\"|\"$", "");
        log.info("SubscriptionName : " + subscriptionName);
        String notificationType = subscriptionJson.get("notificationType").toString().replaceAll("^\"|\"$", "");
        log.info("NotificationType : " + notificationType);
        String notificationMeta = subscriptionJson.get("notificationMeta").toString().replaceAll("^\"|\"$", "");
        log.info("NotificationMeta : " + notificationMeta);
        MultiValueMap<String, String> mapNotificationMessage = new LinkedMultiValueMap<String, String>();
        ArrayNode arrNode = (ArrayNode) subscriptionJson.get("notificationMessageKeyValues");
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                mapNotificationMessage.add(objNode.get("formkey").toString().replaceAll("^\"|\"$", ""), jmespath
                        .runRuleOnEvent(objNode.get("formvalue").toString().replaceAll("^\"|\"$", ""), aggregatedObject)
                        .toString().toString().replaceAll("^\"|\"$", ""));
            }
        }
        if (notificationType.trim().equals("REST_POST")) {
            log.info("Notification through REST_POST");
            int result = -1;
            String headerContentMediaType = subscriptionJson.get("restPostBodyMediaType").toString()
                    .replaceAll("^\"|\"$", "");
            log.info("headerContentMediaType : " + headerContentMediaType);
            result = restTemplate.postDataMultiValue(notificationMeta, mapNotificationMessage, headerContentMediaType);
            if (result == HttpStatus.OK.value() || result == HttpStatus.CREATED.value()
                    || result == HttpStatus.NO_CONTENT.value()) {
                log.info("The result is : " + result);
            } else {
                for (int i = 0; i < failAttempt; i++) {
                    result = restTemplate.postDataMultiValue(notificationMeta, mapNotificationMessage,
                            headerContentMediaType);
                    log.info("After trying for " + (i + 1) + " times, the result is : " + result);
                    if (result == HttpStatus.OK.value())
                        break;
                }
                if (result != HttpStatus.OK.value() && result != HttpStatus.CREATED.value()
                        && result != HttpStatus.NO_CONTENT.value()) {
                    String input = prepareMissedNotification(aggregatedObject, subscriptionName, notificationMeta);
                    log.info("Input missed Notification document : " + input);
                    mongoDBHandler.createTTLIndex(missedNotificationDataBaseName, missedNotificationCollectionName,
                            "Time", ttlValue);
                    boolean output = mongoDBHandler.insertDocument(missedNotificationDataBaseName,
                            missedNotificationCollectionName, input);
                    log.info("The output of insertion of missed Notification : " + output);
                    if (output == false) {
                        log.info("failed to insert the notification into database");
                    } else
                        log.info("Notification saved in the database");
                }
            }
        } else if (notificationType.trim().equals("MAIL")) {
            log.info("Notification through EMAIL");
            sendMail.sendMail(notificationMeta, String.valueOf(((List<String>) mapNotificationMessage.get("")).get(0)));
        }
    }

    /**
     * This method saves the missed Notification into a single document along with
     * Subscription name, notification meta and time period.
     * 
     * @param aggregatedObject
     * @param subscriptionName
     * @param notificationMeta
     * 
     * @return String
     */
    public String prepareMissedNotification(String aggregatedObject, String subscriptionName, String notificationMeta) {
        String time = null;
        Date date = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = new Date();
        time = dateFormat.format(date);
        try {
            date = dateFormat.parse(time);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        BasicDBObject document = new BasicDBObject();
        document.put("subscriptionName", subscriptionName);
        document.put("notificationMeta", notificationMeta);
        document.put("Time", date);
        document.put("AggregatedObject", JSON.parse(aggregatedObject));
        return document.toString();
    }

    /**
     * This method is responsible to display the configurable application properties
     * and to create TTL index on the missed Notification collection.
     */
    @PostConstruct
    public void init() {
        log.debug("missedNotificationCollectionName : " + missedNotificationCollectionName);
        log.debug("missedNotificationDataBaseName : " + missedNotificationDataBaseName);
        log.debug("notification.failAttempt : " + failAttempt);
        log.debug("Missed Notification TTL value : " + ttlValue);
    }

}
