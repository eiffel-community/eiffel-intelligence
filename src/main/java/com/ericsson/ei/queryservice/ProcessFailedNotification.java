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
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongo.MongoCondition;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents the mechanism to extract information regarding a failed notification by
 * using the subscriptionName key.
 */
@Component
public class ProcessFailedNotification {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessFailedNotification.class);

    @Value("${failed.notification.collection-name}")
    private String failedNotificationCollectionName;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Autowired
    private MongoDBHandler handler;

    /**
     * Get a failed notification object by using the subscriptionName key.
     *
     * @param subscriptionName
     * @return
     * @throws NoSuchElementException
     */
    public List<String> processQueryFailedNotification(String subscriptionName)
            throws NoSuchElementException {
        final MongoCondition condition = MongoCondition.subscriptionNameCondition(subscriptionName);
        LOGGER.debug("The Json condition is : {}", condition);
        List<String> output = handler.find(database, failedNotificationCollectionName, condition);
        if (output == null || output.isEmpty()) {
            throw new NoSuchElementException(
                    "No failed notifications found for subscription " + subscriptionName);
        }
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

    @PostConstruct
    public void init() {
        LOGGER.debug(
                "FaildNotification Database is : {}" + "\nFailedNotification Collection is : {}",
                database, failedNotificationCollectionName);
    }

}
