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
package com.ericsson.ei.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.AccessException;
import org.springframework.stereotype.Component;

import com.ericsson.ei.config.HttpSessionConfig;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.repository.ISubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SubscriptionService implements ISubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionService.class);

    private static final String SUBSCRIPTION_NAME = "{'subscriptionName':'%s'}";
    private static final String SUBSCRIPTION_ID = "{'subscriptionId':'%s'}";
    private static final String USER_NAME = "{'ldapUserName':'%s'}";

    private static final String AND = "{$and:[%s]}";

    @Value("${spring.application.name}")
    private String SpringApplicationName;

    @Value("${spring.data.mongodb.database}")
    private String dataBaseName;

    @Value("${subscription.collection.repeatFlagHandlerName}")
    private String repeatFlagHandlerCollection;

    @Value("${ldap.enabled}")
    private boolean ldapEnabled;

    @Autowired
    private ISubscriptionRepository subscriptionRepository;

    @Override
    public void addSubscription(Subscription subscription) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String stringSubscription;
        stringSubscription = mapper.writeValueAsString(subscription);
        subscriptionRepository.addSubscription(stringSubscription);
    }

    @Override
    public Subscription getSubscription(String subscriptionName) throws SubscriptionNotFoundException {
        // empty ldapUserName means that result of query should not depend from
        // userName
        String query = generateQuery(subscriptionName, "");

        ArrayList<String> list = subscriptionRepository.getSubscription(query);
        ObjectMapper mapper = new ObjectMapper();
        if (list == null || list.isEmpty()) {
            throw new SubscriptionNotFoundException("No record found for the Subscription Name:" + subscriptionName);
        }
        for (String input : list) {
            Subscription subscription;
            try {
                subscription = mapper.readValue(input, Subscription.class);
                // Inject aggregationtype
                subscription.setAggregationtype(SpringApplicationName);
                return subscription;
            } catch (IOException e) {
                LOG.error("Malformed JSON string");
            }
        }
        return null;
    }

    @Override
    public boolean doSubscriptionExist(String subscriptionName) {
        // empty userName means that result of query should not depend from
        // userName
        String query = generateQuery(subscriptionName, "");
        ArrayList<String> list = subscriptionRepository.getSubscription(query);
        return !list.isEmpty();
    }

    @Override
    public boolean modifySubscription(Subscription subscription, String subscriptionName) {
        ObjectMapper mapper = new ObjectMapper();
        Document result = null;
        String query;
        try {
            String stringSubscription = mapper.writeValueAsString(subscription);
            String ldapUserName = getLdapUserName(subscriptionName);
            query = generateQuery(subscriptionName, ldapUserName);
            result = subscriptionRepository.modifySubscription(query, stringSubscription);
            if (result != null) {
                String subscriptionIdQuery = String.format(SUBSCRIPTION_ID, subscriptionName);
                if (!cleanSubscriptionRepeatFlagHandlerDb(subscriptionIdQuery)) {
                    LOG.info("Subscription  \"" + subscriptionName
                            + "\" matched aggregated objects id from repeat flag handler database could not be cleaned during the update of the subscription,\n"
                            + "probably due to subscription has never matched any aggregated objects and "
                            + "no matched aggregated objects id has been stored in database for the specific subscription.");
                }
            }

        } catch (JSONException | JsonProcessingException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteSubscription(String subscriptionName) throws AccessException {
        String ldapUserName = getLdapUserName(subscriptionName);
        String deleteQuery = generateQuery(subscriptionName, ldapUserName);
        boolean deleteResult = subscriptionRepository.deleteSubscription(deleteQuery);
        if (deleteResult) {
            String subscriptionIdQuery = String.format(SUBSCRIPTION_ID, subscriptionName);
            if (!cleanSubscriptionRepeatFlagHandlerDb(subscriptionIdQuery)) {
                LOG.info("Subscription  \"" + subscriptionName
                        + "\" matched aggregated objects id from repeat flag handler database could not be cleaned during the removal of subscription,\n"
                        + "probably due to subscription has never matched any aggregated objects and "
                        + "no matched aggregated objects id has been stored in database for the specific subscription.");
            }
        } else if (doSubscriptionExist(subscriptionName)) {
            String message = "Failed to delete subscription \"" + subscriptionName + "\" invalid ldapUserName";
            throw new AccessException(message);
        }

        return deleteResult;
    }

    @Override
    public List<Subscription> getSubscriptions() throws SubscriptionNotFoundException {
        String query = "{}";
        ArrayList<String> list = subscriptionRepository.getSubscription(query);
        List<Subscription> subscriptions = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        if (list == null || list.isEmpty()) {
            throw new SubscriptionNotFoundException("No Subscriptions found");
        }
        for (String input : list) {
            Subscription subscription;
            try {
                subscription = mapper.readValue(input, Subscription.class);
                // Inject aggregationtype
                subscription.setAggregationtype(SpringApplicationName);
                subscriptions.add(subscription);
            } catch (IOException e) {
                LOG.error("malformed json string");
            }
        }
        return subscriptions;
    }

    private boolean cleanSubscriptionRepeatFlagHandlerDb(String subscriptionNameQuery) {
        LOG.debug("Cleaning and removing matched subscriptions AggrObjIds in ReapeatHandlerFlag database with query: "
                + subscriptionNameQuery);
        MongoDBHandler mongoDbHandler = subscriptionRepository.getMongoDbHandler();
        return mongoDbHandler.dropDocument(dataBaseName, repeatFlagHandlerCollection, subscriptionNameQuery);
    }

    /**
     * This method generate query for mongoDB
     *
     * @param subscriptionName-
     *            subscription name
     * @param ldapUserName-
     *            name of the current user
     * @return a String object
     */
    private String generateQuery(String subscriptionName, String ldapUserName) {
        String query = String.format(SUBSCRIPTION_NAME, subscriptionName);
        if (ldapUserName != null && !ldapUserName.isEmpty()) {
            String queryUser = String.format(USER_NAME, ldapUserName);
            String queryTemp = query + "," + queryUser;
            query = String.format(AND, queryTemp);
        }
        return query;
    }

    /**
     * This method finds whether a given subscription has an owner
     *
     * @param subscriptionName-
     *            subscription name
     * @return a boolean
     * @throws SubscriptionNotFoundException
     */
    private boolean doSubscriptionOwnerExist(String subscriptionName) {
        boolean ownerExist = false;
        try {
            Subscription subscription = getSubscription(subscriptionName);
            String getLdapUserName = subscription.getLdapUserName();
            if (getLdapUserName != null && !getLdapUserName.isEmpty()) {
                ownerExist = true;
            }
        } catch (SubscriptionNotFoundException e) {
            LOG.error(e.getMessage());
        }
        return ownerExist;
    }

    /**
     * This method ldapUserName, if exists, otherwise return empty string
     *
     * @param subscriptionName-
     *            subscription name
     * @return a string
     */
    private String getLdapUserName(String subscriptionName) {
        String ldapUserName = (ldapEnabled) ? HttpSessionConfig.getCurrentUser() : "";
        boolean ownerExist = doSubscriptionOwnerExist(subscriptionName);
        if (!ownerExist) {
            ldapUserName = "";
        }

        return ldapUserName;
    }
}
