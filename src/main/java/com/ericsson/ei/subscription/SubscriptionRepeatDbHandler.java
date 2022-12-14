/*
   Copyright 2018 Ericsson AB.
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

import com.ericsson.ei.cache.SubscriptionCacheHandler;
import com.ericsson.ei.mongo.MongoCondition;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class SubscriptionRepeatDbHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionRepeatDbHandler.class);

    @Autowired
    public MongoDBHandler mongoDbHandler;

    private ObjectMapper mapper = new ObjectMapper();

    @Getter
    @Setter
    @Value("${spring.data.mongodb.database}")
    public String dataBaseName;
    @Getter
    @Setter
    @Value("${subscriptions.repeat.handler.collection.name}")
    public String collectionName;

    /**
     * Function that stores the matched aggregatedObjectId to the database.
     *
     * @param subscriptionId
     * @param requirementId
     * @param aggrObjId
     */
    public void addMatchedAggrObjToSubscriptionId(String subscriptionId,
            int requirementId, String aggrObjId) {
        LOGGER.debug(
                "Adding/Updating matched AggrObjId: {} to SubscriptionsId: {} aggrId matched list",
                aggrObjId, subscriptionId);

        if (checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(subscriptionId, requirementId,
                aggrObjId, false)) {
            
            LOGGER.debug(
                    "Subscription: {} and AggrObjId, {} has already been matched."
                            + "No need to register the subscription match.",
                    subscriptionId, aggrObjId);
            return;
        }

        if (!updateExistingMatchedSubscriptionWithAggrObjId(subscriptionId,
                requirementId, aggrObjId)) {
            LOGGER.debug(
                    "New Subscription AggrId has not matched, inserting new SubscriptionId"
                            + "and AggrObjId to matched list.");

            insertNewMatchedAggregationToDatabase(subscriptionId, requirementId, aggrObjId);
        }
    }

    public boolean checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(
            String subscriptionId, int requirementId, String aggrObjId, boolean useCache) {
        List<String> objArray = null;
        if (useCache && SubscriptionCacheHandler.subscriptionsCache.containsKey(subscriptionId)) {
            objArray = SubscriptionCacheHandler.subscriptionsCache.get(subscriptionId);
        } else {
            LOGGER.debug(
                    "Checking if AggrObjId: {} exist in SubscriptionId: {} AggrId matched list.",
                    aggrObjId, subscriptionId);
            final MongoCondition subscriptionQuery = MongoCondition.subscriptionCondition(subscriptionId);
            objArray = mongoDbHandler.find(dataBaseName, collectionName,
                    subscriptionQuery);

            SubscriptionCacheHandler.subscriptionsCache.put(subscriptionId, objArray);
        }

        if (objArray != null && !objArray.isEmpty()) {

            LOGGER.debug("Making AggrObjId checks on SubscriptionId document: {}", objArray.get(0));
            try {
                JsonNode jNode = mapper.readTree(objArray.get(0));
                boolean aggrObjIdExist = jNode.get("subscriptionId")
                                              .asText()
                                              .trim()
                                              .equals(subscriptionId);
                if (aggrObjIdExist) {
                    LOGGER.debug(
                            "SubscriptionId \"{}\" , exist in document. "
                                    + "Checking if AggrObjId has matched earlier.",
                            subscriptionId);

                    LOGGER.debug(
                            "Subscription requirementId: {} and Requirements content:\n{}",
                            jNode.get("requirements")
                                 .get(new Integer(requirementId).toString()),
                            requirementId);

                    boolean triggered = checkRequirementIdTriggered(jNode, requirementId,
                            aggrObjId);

                    if (!triggered) {
                        LOGGER.debug(
                                "RequirementId: {} and SubscriptionId: {} "
                                        + "has not matched any AggregatedObject.",
                                requirementId, subscriptionId);
                    }
                    return triggered;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to check if requirement has triggered.",
                        e);
            }
        }
        LOGGER.debug(
                "AggrObjId: {} not found for SubscriptionId: {} in SubscriptionRepeatFlagHandlerDb.",
                aggrObjId, subscriptionId);
        return false;
    }

    private boolean updateExistingMatchedSubscriptionWithAggrObjId(
            String subscriptionId, int requirementId, String aggrObjId) {

        final MongoCondition subscriptionQuery = MongoCondition.subscriptionCondition(subscriptionId);
        JsonNode updateDocJsonNode = prepareQueryToUpdateAggregation(subscriptionId, requirementId,
                aggrObjId);

        Document document = null;

        document = mongoDbHandler.findAndModify(dataBaseName, collectionName,
                subscriptionQuery, updateDocJsonNode.toString());
        if (document != null && !document.isEmpty()) {
            LOGGER.debug(
                    "Successfully updated Matched Subscription Aggregated Object list:"
                            + "\nfor subscriptionId: {}"
                            + "\nwith Aggregated Object Id: {}"
                            + "\nwith matched Requirement Id: {}",
                    subscriptionId, aggrObjId, requirementId);
            return true;
        }

        LOGGER.warn(
                "Failed to update existing matched SubscriptionId with new AggrId."
                        + "SubscriptionId: {} "
                        + "New matched AggrObjId: {} "
                        + "RequirementId that have matched: {}",
                subscriptionId, aggrObjId, requirementId);
        return false;
    }

    private void insertNewMatchedAggregationToDatabase(String subscriptionId,
            int requirementId, String aggrObjId) {
        try {
            BasicDBObject document = new BasicDBObject();
            document.put("subscriptionId", subscriptionId);

            ArrayList<String> aggrObjIdsList = new ArrayList<String>();
            aggrObjIdsList.add(aggrObjId);

            BasicDBObject reqDocument = new BasicDBObject();
            reqDocument.put(String.valueOf(requirementId), aggrObjIdsList);

            document.put("requirements", reqDocument);

            LOGGER.debug("New matched aggregated object id update"
                    + "on subscription to be inserted to Db: {}",
                    document);
            mongoDbHandler.insertDocument(dataBaseName, collectionName, document.toString());
        } catch (MongoWriteException e) {
            LOGGER.error("Failed to insert the document into database.", e);
        }
    }

    private JsonNode prepareQueryToUpdateAggregation(String subscriptionId,
            int requirementId, String aggrObjId) {
        try {
            String json = "{\"$push\": { \"requirements." + requirementId + "\": \"" + aggrObjId
                    + "\"}}";
            JsonNode updateDocJsonNode = mapper.readValue(json, JsonNode.class);
            LOGGER.debug(
                    "SubscriptionId \"{}\" document will be updated with "
                            + "following requirement update object: {}",
                    subscriptionId, updateDocJsonNode.toString());
            return updateDocJsonNode;
        } catch (Exception e) {
            LOGGER.error("Failed to create query for updating aggregated object.", e);
        }
        return null;
    }

    private boolean checkRequirementIdTriggered(JsonNode jNode,
            int requirementId, String aggrObjId) throws Exception {
        ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {
        });
        JsonNode ids = jNode.get("requirements").get("" + requirementId);
        if (ids == null) {
            return false;
        }
        List<String> listAggrObjIds = reader.readValue(ids);

        if (requirementId > (listAggrObjIds.size() - 1)) {
            return false;
        }

        if (listAggrObjIds.contains(aggrObjId)) {
            LOGGER.info("Subscription has already matched for aggregated object id: {}", aggrObjId);
            return true;
        }
        return false;
    }
}
