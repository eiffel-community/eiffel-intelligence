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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.Getter;

/**
 * This class represents the mechanism to fetch the rule conditions from the
 * Subscription Object and match it with the aggregatedObject to check if it is
 * true.
 *
 * @author xjibbal
 */

@Component
public class RunSubscription {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunSubscription.class);

    @Autowired
    private JmesPathInterface jmespath;

    @Autowired
    private SubscriptionRepeatDbHandler subscriptionRepeatDbHandler;
    
    @Getter
    @Value("${spring.data.mongodb.database}")
    public String dataBaseName;

    /**
     * This method matches every condition specified in the subscription Object and
     * if all conditions are matched then only the aggregatedObject is eligible for
     * notification via e-mail or REST POST.
     *
     * (AND between conditions in requirements, "OR" between requirements with
     * conditions)
     *
     * @param aggregatedObject
     * @param requirementIterator
     * @return boolean
     */
    public boolean runSubscriptionOnObject(String aggregatedObject, Iterator<JsonNode> requirementIterator,
            JsonNode subscriptionJson, String id) {
        boolean conditionFulfilled = false;
        int count_condition_fulfillment = 0;
        int count_conditions = 0;
        int requirementIndex = 0;
        String subscriptionName = "";

        while (requirementIterator.hasNext()) {

            subscriptionName = subscriptionJson.get("subscriptionName").asText();
            String subscriptionRepeatFlag = subscriptionJson.get("repeat").asText();

            if (id == null) {
                LOGGER.debug(
                        "ID has not been passed for given aggregated object. The subscription will be triggered again.");
            }

            if (subscriptionRepeatFlag.equals("false") && id != null && subscriptionRepeatDbHandler
                    .checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(subscriptionName, requirementIndex, id, false)) {
                LOGGER.debug(
                        "Subscription has already matched with AggregatedObject Id: {}\n"
                                + "SubscriptionName: {}\nand has Subscription Repeat flag set to: {}",
                        id, subscriptionName, subscriptionRepeatFlag);
                break;
            }

            JsonNode requirement = requirementIterator.next();

            LOGGER.info("The fulfilled requirement which condition will check is : {}", requirement.toString());
            ArrayNode conditions = (ArrayNode) requirement.get("conditions");

            count_condition_fulfillment = 0;
            count_conditions = conditions.size();

            Iterator<JsonNode> conditionIterator = conditions.elements();
            while (conditionIterator.hasNext()) {
                String condition = conditionIterator.next().get("jmespath").toString().replaceAll("^\"|\"$", "");
                JsonNode result = jmespath.runRuleOnEvent(condition, aggregatedObject);
                String resultString = result.toString();
                resultString = destringify(resultString);
                boolean resultNotEqualsToNull = !resultString.equals("null");
                boolean resultNotEqualsToFalse = !resultString.equals("false");
                boolean resultNotEmpty = !resultString.equals("");
                boolean isFulfilled = resultNotEqualsToNull && resultNotEqualsToFalse && resultNotEmpty;
                String fulfilledStatement = String.format("Condition was %sfulfilled.", isFulfilled ? "" : "not ");
                LOGGER.debug("Condition: {}\nJMESPath evaluation result: {}\n{}", condition, result.toString(),
                        fulfilledStatement);
                if (resultNotEqualsToNull && resultNotEqualsToFalse && resultNotEmpty) {
                    count_condition_fulfillment++;
                }
            }

            if (count_conditions != 0 && count_condition_fulfillment == count_conditions) {
                conditionFulfilled = true;
                if (subscriptionRepeatFlag.equals("false") && id != null) {
                    final String synchronizationString = new String(subscriptionName);
                    // the keyword 'synchronized' ensures that this part of the code run
                    // synchronously. Thus avoids race condition.
                    synchronized (synchronizationString) {
                        if (!subscriptionRepeatDbHandler.checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(
                                subscriptionName, requirementIndex, id, false)) {
                            LOGGER.debug("Adding matched aggregated object to database:" + dataBaseName);
                            subscriptionRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionName,
                                    requirementIndex, id);
                        } else {
                            conditionFulfilled = false;
                        }
                    }
                }
            }

            requirementIndex++;
        }

        LOGGER.info("The final value of conditionFulfilled is : {} for aggregation object id: {} and Subscription: {}",
                conditionFulfilled, id);

        return conditionFulfilled;
    }

    public static String destringify(String str) {
        str = str.replaceAll("\"", "");
        str = str.replaceAll("\\{", "");
        str = str.replaceAll("\\}", "");
        str = str.replaceAll("\\]", "");
        str = str.replaceAll("\\[", "");
        return str;
    }
}