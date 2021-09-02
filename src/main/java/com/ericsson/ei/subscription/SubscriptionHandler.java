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
package com.ericsson.ei.subscription;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.notifications.InformSubscriber;
import com.ericsson.ei.utils.SubscriptionField;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.Getter;
import lombok.Setter;

/**
 * This class is responsible to take a aggregatedObject and match it with all
 * the subscription object, to check ALL Conditions/requirement for
 * notification. (AND between conditions in requirements, "OR" between
 * requirements with conditions)
 *
 * @author xjibbal
 */

@Component
public class SubscriptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        SubscriptionHandler.class);

    @Getter
    @Value("${subscriptions.collection.name}")
    private String subscriptionCollectionName;

    @Getter
    @Value("${spring.data.mongodb.database}")
    private String database;

    @Autowired
    private InformSubscriber informSubscriber;

    @Setter
    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private RunSubscription runSubscription;

    /**
     * The method takes a aggregatedObject as argument and fetches all the
     * subscriber from the database in order to match the subscription
     * conditions in a separate thread.
     *
     * @param aggregatedObject
     * @param id
     */
    public void checkSubscriptionForObject(final String aggregatedObject,
                                           final String id) {
        List<String> subscriptions = mongoDBHandler.getAllDocuments(
                database, subscriptionCollectionName);
        subscriptions.forEach(
                subscription -> extractConditions(aggregatedObject,
                        subscription, id));
    }

    /**
     * This method takes both aggregatedObject and a Subscription object as
     * arguments and fetches the subscription conditions from the
     * subscription object and matches these conditions with
     * the aggregatedObject.
     *
     * @param aggregatedObject
     * @param subscriptionData
     * @param id
     */
    private void extractConditions(String aggregatedObject,
                                   String subscriptionData, String id) {
        try {
            JsonNode subscriptionJson = new ObjectMapper().readTree(
                subscriptionData);
            LOGGER.debug("SubscriptionJson : " + subscriptionJson.toString());
            LOGGER.debug("Aggregated Object : " + aggregatedObject  + " for the event id: " + id);
            ArrayNode requirementNode = (ArrayNode) subscriptionJson.get(
                "requirements");
            LOGGER.debug("Requirements : " + requirementNode.toString());
            Iterator<JsonNode> requirementIterator = requirementNode.elements();
            SubscriptionField subscriptionField = new SubscriptionField(subscriptionJson);
            String subscriptionName = subscriptionField.get("subscriptionName");
            if (runSubscription.runSubscriptionOnObject(aggregatedObject,
                requirementIterator, subscriptionJson, id)) {
                LOGGER.debug(
                    "The subscription conditions match for the aggregatedObject");
                informSubscriber.informSubscriber(aggregatedObject,
                    subscriptionJson);
                LOGGER.info("Subscription with name ** {} ** has been processed for the event id: {}", subscriptionName,
                        id);
            }
        } catch (Exception e) {
            LOGGER.error("Subscription: {}, failed for aggregated object: {}",
                subscriptionData, aggregatedObject, e);
        }
    }
}