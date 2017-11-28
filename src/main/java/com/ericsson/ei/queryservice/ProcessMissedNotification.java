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

import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents the mechanism to extract the aggregated data on the
 * basis of the SubscriptionName from the Missed Notification Object.
 * 
 */
@Component
public class ProcessMissedNotification {

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDataBaseName;

    static Logger log = (Logger) LoggerFactory.getLogger(ProcessMissedNotification.class);

    @Autowired
    MongoDBHandler handler;

    /**
     * The method is responsible to extract the data on the basis of the
     * subscriptionName from the Missed Notification Object.
     * 
     * @param subscriptionName
     * @return ArrayList
     */
    public ArrayList processQueryMissedNotification(String subscriptionName) {
        ObjectMapper mapper = new ObjectMapper();
        String condition = "{\"subscriptionName\" : \"" + subscriptionName + "\"}";
        log.info("The condition is : " + condition);
        JsonNode jsonCondition = null;
        try {
            jsonCondition = mapper.readTree(condition);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("The Json condition is : " + jsonCondition);
        ArrayList<String> output = handler.find(missedNotificationDataBaseName, missedNotificationCollectionName,
                jsonCondition.toString());
        ArrayList<String> response = new ArrayList<String>();
        Iterator itr = output.iterator();
        while (itr.hasNext()) {
            String sElement = (String) itr.next();
            try {
                JsonNode jElement = mapper.readTree(sElement);
                log.info("The individual element is : " + jElement.toString());
                JsonNode element = jElement.path("AggregatedObject");
                response.add(element.toString());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return response;
    }

    @PostConstruct
    public void init() {
        log.debug("The Aggregated Database is : " + missedNotificationDataBaseName);
        log.debug("The Aggregated Collection is : " + missedNotificationCollectionName);
    }

}
