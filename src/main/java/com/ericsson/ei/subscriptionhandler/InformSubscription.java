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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;

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

    @Value("${notification.failAttempt}")
    private int failAttempt;

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDataBaseName;

    @Autowired
    SpringRestTemplate restTemplate;

    @Autowired
    MongoDBHandler mongoHandler;

    @Autowired
    SendMail sendMail;

    static Logger log = (Logger) LoggerFactory.getLogger(InformSubscription.class);
    static boolean saved = false;

    /**
     * This method extracts the mode of notification through which the
     * subscriber should be notified, from the subscription Object. And if the
     * notification fails, then it saved in the database.
     * 
     * @param aggregatedObject
     * @param subscriptionJson
     */
    public void informSubscriber(String aggregatedObject, JsonNode subscriptionJson) {
        String notificationType = subscriptionJson.get("notificationType").toString().replaceAll("^\"|\"$", "");
        log.info("NotificationType : " + notificationType);
        String notificationMeta = subscriptionJson.get("notificationMeta").toString().replaceAll("^\"|\"$", "");
        log.info("NotificationMeta : " + notificationMeta);
        if (notificationType.trim().equals("REST_POST")) {
            log.info("Notification through REST_POST");
            int result = restTemplate.postData(aggregatedObject, notificationMeta);
            if (result == HttpStatus.OK.value()) {
                log.info("The result is : " + result);
            } else {
                for (int i = 0; i < failAttempt; i++) {
                    try {
                        log.info("Waiting for 2 seconds");
                        Thread.currentThread().sleep(2000);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                    result = restTemplate.postData(aggregatedObject, notificationMeta);
                    log.info("After trying for " + (i + 1) + " times, the result is : " + result);
                    if (result == HttpStatus.OK.value())
                        break;
                }
                if (result != HttpStatus.OK.value() && saved == false) {
                    mongoHandler.insertDocument(missedNotificationDataBaseName, missedNotificationCollectionName,
                            aggregatedObject);
                    saved = true;
                    log.info("Notification saved in the database");
                }
            }
        }
        if (notificationType.equals("EMAIL")) {
            log.info("Notification through EMAIL");
            sendMail.sendMail(notificationMeta, aggregatedObject);

        }
    }

    @PostConstruct
    public void display() {
        log.debug("missedNotificationCollectionName : " + missedNotificationCollectionName);
        log.debug("missedNotificationDataBaseName : " + missedNotificationDataBaseName);
        log.debug("notification.failAttempt : " + failAttempt);
    }

}
