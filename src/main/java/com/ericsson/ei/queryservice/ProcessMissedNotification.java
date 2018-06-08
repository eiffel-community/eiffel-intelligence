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

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents the mechanism to extract the aggregated data on the
 * basis of the SubscriptionName from the Missed Notification Object.
 */
@Component
public class ProcessMissedNotification {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ProcessMissedNotification.class);

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDatabaseName;

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
        ObjectMapper mapper = new ObjectMapper();
        String condition = "{\"subscriptionName\" : \"" + subscriptionName + "\"}";
        LOGGER.debug("The condition is : " + condition);
        JsonNode jsonCondition = null;
        try {
            jsonCondition = mapper.readTree(condition);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("The Json condition is : " + jsonCondition);
        List<String> output = handler.find(missedNotificationDatabaseName, missedNotificationCollectionName,
                jsonCondition.toString());
        return output.stream().map(a -> {
            try {
                return mapper.readTree(a).path("AggregatedObject").toString();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
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
        String condition = "{\"subscriptionName\" : \"" + subscriptionName + "\"}";
        LOGGER.debug("The JSON condition for delete missed notification is : " + condition);
        return handler.dropDocument(missedNotificationDatabaseName, missedNotificationCollectionName, condition);
    }

    @PostConstruct
    public void init() {
        LOGGER.debug("MissedNotification Database is : " + missedNotificationDatabaseName
            + "\nMissedNotification Collection is : " + missedNotificationCollectionName);
    }

}
