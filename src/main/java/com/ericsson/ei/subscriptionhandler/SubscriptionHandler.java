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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;

/**
 * This class is responsible to take a aggregatedObject and match it with all
 * the Subscription Object, to check ALL Conditions/requirement for
 * notification. (AND between conditions in requirements, "OR" between
 * requirements with conditions)
 *
 * @author xjibbal
 */

@Component
public class SubscriptionHandler {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SubscriptionHandler.class);

    @Getter
    @Value("${subscription.collection.name}")
    private String subscriptionCollectionName;

    @Getter
    @Value("${spring.data.mongodb.database}")
    private String subscriptionDataBaseName;

    @Autowired
    private InformSubscription informSubscription;

    @Setter
    @Autowired
    private MongoDBHandler mongoDBHandler;

    @Autowired
    private RunSubscription runSubscription;

    @Autowired
    private JmesPathInterface jmespath;

    /**
     * The method takes a aggregatedObject as argument and fetches all the
     * subscriber from the database in order to match the subscription
     * conditions in a separate thread.
     *
     * @param aggregatedObject
     */
    public void checkSubscriptionForObject(final String aggregatedObject, final String id) {
        Thread subscriptionThread = new Thread(() -> {
            List<String> subscriptions = mongoDBHandler.getAllDocuments(subscriptionDataBaseName,
                    subscriptionCollectionName);
            subscriptions.forEach(subscription -> extractConditions(aggregatedObject, subscription, id));
        });
        subscriptionThread.setName("SubscriptionHandler");
        subscriptionThread.start();
    }

    /**
     * This method takes both aggregatedObject and a Subscription object as
     * arguments and fetches the subscription conditions from the subscription
     * object and matches these conditions with the aggregatedObject.
     *
     * @param aggregatedObject
     * @param subscriptionData
     * @param id
     */
    private void extractConditions(String aggregatedObject, String subscriptionData, String id) {
        JsonNode subscriptionJson = null;
        try {
            subscriptionJson = new ObjectMapper().readTree(subscriptionData);
            LOGGER.debug("SubscriptionJson : " + subscriptionJson.toString());
            JsonNode aggregatedJson = new ObjectMapper().readTree(aggregatedObject);
            LOGGER.debug("AggregatedJson : " + aggregatedJson.toString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        ArrayNode requirementNode = (ArrayNode) subscriptionJson.get("requirements");
        LOGGER.debug("Requirements : " + requirementNode.toString());
        Iterator<JsonNode> requirementIterator = requirementNode.elements();
        if (runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator, subscriptionJson, id)) {
            LOGGER.debug("The subscription conditions match for the aggregatedObject");
            informSubscription.informSubscriber(aggregatedObject, subscriptionJson);
        }
    }

    /**
     * This method is responsible for displaying configurable application
     * parameters like Subscription database name and collection name, etc.
     */
    @PostConstruct
    public void print() {
        LOGGER.debug("SubscriptionDataBaseName : " + subscriptionDataBaseName);
        LOGGER.debug("SubscriptionCollectionName : " + subscriptionCollectionName);
        LOGGER.debug("MongoDBHandler object : " + mongoDBHandler);
        LOGGER.debug("JmesPathInterface : " + jmespath);

    }
}