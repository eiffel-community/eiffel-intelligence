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
package com.ericsson.ei.queryservice;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.MongoCondition;
import com.ericsson.ei.handlers.MongoDBHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents the mechanism to extract the aggregated data on the
 * basis of the SubscriptionName from the Missed Notification Object.
 */
@Component
public class ProcessMissedNotification {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessMissedNotification.class);

    @Value("${failed.notification.collection-name}")
    private String failedNotificationCollectionName;

    @Value("${failed.notification.database-name}")
    private String failedNotificationDatabaseName;

    @Autowired
    private MongoDBHandler handler;

    /**
     * The method is responsible to extract the data on the basis of the
     * subscriptionName from the Missed Notification Object.
     *
     * @param subscriptionName
     * @return List
     */
    public List<String> processQueryMissedNotification(String subscriptionName) {
        MongoCondition condition = MongoCondition.subscriptionNameCondition(subscriptionName);
        LOGGER.debug("The Json condition is : {}", condition);
        List<String> output = handler.find(failedNotificationDatabaseName, failedNotificationCollectionName,
               condition);

        ObjectMapper mapper = new ObjectMapper();
        return output.stream().map(a -> {
            try {
                return mapper.readTree(a).toString();
            } catch (Exception e) {
                LOGGER.error("Failed to parse JSON.", e);
            }
            return null;
        }).collect(Collectors.toList());

    }

    /**
     * The method is responsible for the delete the missed notification using subscription name
     *
     * @param subscriptionName
     * @return boolean
     */
    public boolean deleteMissedNotification(String subscriptionName) {
        MongoCondition condition = MongoCondition.subscriptionNameCondition(subscriptionName);
        LOGGER.debug("The JSON condition for delete missed notification is : {}", condition);
        return handler.dropDocument(failedNotificationDatabaseName, failedNotificationCollectionName, condition);
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("FaildNotification Database is : {}" + "\nFailedNotification Collection is : {}",
                failedNotificationDatabaseName, failedNotificationCollectionName);
    }

}
