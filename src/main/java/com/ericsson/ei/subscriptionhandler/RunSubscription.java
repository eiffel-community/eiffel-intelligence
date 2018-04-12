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

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * This class represents the mechanism to fetch the rule conditions from the
 * Subscription Object and match it with the aggregatedObject to check if it is
 * true.
 * 
 * @author xjibbal
 *
 */

@Component
public class RunSubscription {

    @Autowired
    private JmesPathInterface jmespath;

    final static Logger LOGGER = (Logger) LoggerFactory.getLogger(RunSubscription.class);
    
    @Autowired
	private SubscriptionRepeatDbHandler subscriptionRepeatDbHandler;
	
	/**
     * This method matches every condition specified in the subscription Object
     * and if all conditions are matched then only the aggregatedObject is
     * eligible for notification via e-mail or REST POST.
     *
     * (AND between conditions in requirements, "OR" between requirements with conditions)
     * 
     * @param aggregatedObject
     * @param requirement
     * @param subscriptionJson
     * @return boolean
     */

        public boolean runSubscriptionOnObject(String aggregatedObject, Iterator<JsonNode> requirementIterator,
                JsonNode subscriptionJson) {
        boolean conditionFulfilled = false;
        int count_condition_fulfillment = 0;
        int count_conditions = 0;

        int requirementIndex = 0;
        while (requirementIterator.hasNext()) {
        	
            JsonNode aggrObjJsonNode = null;
            ObjectMapper objectMapper = new ObjectMapper();
            try {
            	aggrObjJsonNode = objectMapper.readValue(aggregatedObject, JsonNode.class);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            
            
            String aggrObjId = aggrObjJsonNode.get("id").asText();
            String subscriptionName = subscriptionJson.get("subscriptionName").asText();
            String subscriptionRepeatFlag = subscriptionJson.get("repeat").asText();
            
            if (subscriptionRepeatFlag == "false" && subscriptionRepeatDbHandler.checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(subscriptionName, requirementIndex, aggrObjId)){
            	LOGGER.info("Subscription has already matched with AggregatedObject Id: " + aggrObjId +
            			"\nSubscriptionName: " + subscriptionName +
            			"\nand has Subsctrion Repeat flag set to: " + subscriptionRepeatFlag);
            	break;
            }
            
            JsonNode requirement = requirementIterator.next();
            LOGGER.info("The fulfilled requirement which will condition checked is : " + requirement.toString());
            ArrayNode conditions = (ArrayNode) requirement.get("conditions");

            count_condition_fulfillment = 0;
            count_conditions = conditions.size();

            LOGGER.info("Conditions of the subscription : " + conditions.toString());
            Iterator<JsonNode> conditionIterator = conditions.elements();
            while (conditionIterator.hasNext()) {
                String rule = conditionIterator.next().get("jmespath").toString().replaceAll("^\"|\"$", "");
                String new_Rule = rule.replace("'", "\"");
                LOGGER.info("Rule : " + rule);
                LOGGER.info("New Rule after replacing single quote : " + new_Rule);
                JsonNode result = jmespath.runRuleOnEvent(rule, aggregatedObject);
                LOGGER.info("Result : " + result.toString());
                if (result.toString() != null && result.toString() != "false" && !result.toString().equals("[]")){
                    count_condition_fulfillment++;
                }
            }

            if(count_conditions != 0 && count_condition_fulfillment == count_conditions){
                conditionFulfilled = true;
                if (subscriptionJson.get("repeat").toString() == "false") {
                	LOGGER.info("Adding matched AggrObj id to SubscriptionRepeatFlagHandlerDb.");
                	try {
						subscriptionRepeatDbHandler.addMatchedAggrObjToSubscriptionId(subscriptionName, requirementIndex, aggrObjId);
					} catch (Exception e) {
						e.printStackTrace();
					}
                }
            }
            
            requirementIndex++;
        }

        LOGGER.info("The final value of conditionFulfilled is : " + conditionFulfilled);

        return conditionFulfilled;

    }

}
