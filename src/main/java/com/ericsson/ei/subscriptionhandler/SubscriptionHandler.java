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

import java.util.ArrayList;
import java.util.Iterator;
import javax.annotation.PostConstruct;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * This class is responsible to take a aggregatedObject and match it with all
 * the Subscription Object, to check ALL Conditions/requirement for
 * notification.  (AND between conditions in requirements, "OR" between requirements with conditions)
 * 
 * @author xjibbal
 *
 */

@Component
public class SubscriptionHandler {

    @Getter
    @Value("${subscription.collection.name}")
    private String subscriptionCollectionName;

    @Getter
    @Value("${database.name}")
    private String subscriptionDataBaseName;

    @Autowired
    InformSubscription informSubscription;

    @Autowired
    private MongoDBHandler handler;

    @Autowired
    private RunSubscription runSubscription;

    @Autowired
    private JmesPathInterface jmespath;

    static Logger log = (Logger) LoggerFactory.getLogger(SubscriptionHandler.class);

    /**
     * The method takes a aggregatedObject as argument and fetches all the
     * subscriber from the database in order to match the subscription
     * conditions in a separate thread.
     * 
     * @param aggregatedObject
     */
    public void checkSubscriptionForObject(final String aggregatedObject) {
        Thread subscriptionThread = new Thread(new Runnable() {
            public void run() {
                ArrayList<String> subscriptions = handler.getAllDocuments(subscriptionDataBaseName,
                        subscriptionCollectionName);
                Iterator itr = subscriptions.iterator();
                while (itr.hasNext()) {
                    String subscriptionData = (String) itr.next();
                    extractConditions(aggregatedObject, subscriptionData);
                }

            }
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
     */
    public void extractConditions(String aggregatedObject, String subscriptionData) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode subscriptionJson = null;
        JsonNode aggregatedJson = null;
        try {
            subscriptionJson = mapper.readTree(subscriptionData);
            log.info("SubscriptionJson : " + subscriptionJson.toString());
            aggregatedJson = mapper.readTree(aggregatedObject);
            log.info("AggregatedJson : " + aggregatedJson.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        ArrayNode requirementNode = (ArrayNode) subscriptionJson.get("requirements");
        log.info("RequirementNode : " + requirementNode.toString());
        Iterator<JsonNode> requirementIterator = requirementNode.elements();

            if (runSubscription.runSubscriptionOnObject(aggregatedObject, requirementIterator, subscriptionJson)) {
                log.info("The subscription conditions match for the aggregatedObject");
                informSubscription.informSubscriber(aggregatedObject, subscriptionJson);
            }

    }

    /**
     * This method is responsible for displaying configurable application
     * parameters like Subscription database name and collection name, etc.
     */
    @PostConstruct
    public void print() {
        log.debug("SubscriptionDataBaseName : " + subscriptionDataBaseName);
        log.debug("SubscriptionCollectionName : " + subscriptionCollectionName);
        log.debug("MongoDBHandler object : " + handler);
        log.debug("JmesPathInterface : " + jmespath);

    }

}
