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

import java.util.Iterator;

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

    static Logger log = (Logger) LoggerFactory.getLogger(RunSubscription.class);

    /**
     * This method matches every condition specified in the subscription Object
     * and if all conditions are matched then only the aggregatedObject is
     * eligible for notification via e-mail or REST POST.
     * 
     * @param aggregatedObject
     * @param requirement
     * @param subscriptionJson
     * @return boolean
     */
    public boolean runSubscriptionOnObject(String aggregatedObject, ArrayNode fulfilledRequirements,
            JsonNode subscriptionJson) {
        Iterator<JsonNode> requirementIterator = fulfilledRequirements.elements();
        boolean conditionFulfilled = false;
        while (requirementIterator.hasNext()) {
            JsonNode requirement = requirementIterator.next();
            log.info("The fulfilled requirement which will condition checked is : " + requirement.toString());
            ArrayNode conditions = (ArrayNode) requirement.get("conditions");
            log.info("Conditions of the subscription : " + conditions.toString());
            Iterator<JsonNode> conditionIterator = conditions.elements();
            // boolean conditionFulfilled = false;
            while (conditionIterator.hasNext()) {
                String rule = conditionIterator.next().get("jmespath").toString().replaceAll("^\"|\"$", "");
                String new_Rule = rule.replace("'", "\"");
                log.info("Rule : " + rule);
                log.info("New Rule after replacing single quote : " + new_Rule);
                JsonNode result = jmespath.runRuleOnEvent(rule, aggregatedObject);
                log.info("Result : " + result.toString());
                if (result.toString() != null) {
                    conditionFulfilled = true;
                }
            }
        }
        log.info("The final value of conditionFulfilled is : " + conditionFulfilled);
        return conditionFulfilled;

    }

    /**
     * This method check if the subscription requirement type and the
     * aggregatedObject TemplateName are same.
     * 
     * @param requirementIterator
     * @param aggregatedObject
     * @return JsonNode
     */

    public ArrayNode checkRequirementType(Iterator<JsonNode> requirementIterator, String aggregatedObject) {
        ArrayNode fulfilledRequirements = new ObjectMapper().createArrayNode();
        ;
        JsonNode requirement = null;
        JsonNode aggregatedJson = null;
        boolean condition = false;
        try {
            aggregatedJson = new ObjectMapper().readTree(aggregatedObject);
            log.info("AggregatedJson : " + aggregatedJson.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }



        while (requirementIterator.hasNext()) {
            requirement = requirementIterator.next();
            log.info("Requirements : " + requirement.toString());
            fulfilledRequirements.add(requirement);
        }

        return fulfilledRequirements;



    }

}
