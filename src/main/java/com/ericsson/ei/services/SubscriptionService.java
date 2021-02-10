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
import com.ericsson.ei.controller.model.AuthenticationType;
import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.encryption.EncryptionUtils;
import com.ericsson.ei.encryption.Encryptor;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.ericsson.ei.mongo.MongoCondition;
import com.ericsson.ei.mongo.MongoDBHandler;
import com.ericsson.ei.mongo.MongoQuery;
import com.ericsson.ei.mongo.MongoQueryBuilder;
import com.ericsson.ei.repository.ISubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoWriteException;

@Component
public class SubscriptionService implements ISubscriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionService.class);

    @Value("${spring.application.name}")
    private String SpringApplicationName;

    @Value("${spring.data.mongodb.database}")
    private String dataBaseName;

    @Value("${subscriptions.repeat.handler.collection.name}")
    private String repeatFlagHandlerCollection;

    @Value("${ldap.enabled}")
    private boolean ldapEnabled;

    @Autowired
    private ISubscriptionRepository subscriptionRepository;

    @Autowired
    private Encryptor encryptor;

    @Override
    public void addSubscription(Subscription subscription)
            throws JsonProcessingException, MongoWriteException {
        String authType = subscription.getAuthenticationType();
        String username = subscription.getUserName();
        String password = subscription.getPassword();
        if (isEncryptionReady(authType, username, password)) {
            String encryptedPassword = encryptPassword(password);
            subscription.setPassword(encryptedPassword);
        }
        ObjectMapper mapper = new ObjectMapper();
        String stringSubscription;
        stringSubscription = mapper.writeValueAsString(subscription);
        subscriptionRepository.addSubscription(stringSubscription);
    }

    @Override
    public Subscription getSubscription(String subscriptionName)
            throws SubscriptionNotFoundException {
        final MongoQuery query = MongoCondition.subscriptionNameCondition(subscriptionName);

        ArrayList<String> list = subscriptionRepository.getSubscription(query);
        ObjectMapper mapper = new ObjectMapper();
        if (list == null || list.isEmpty()) {
            throw new SubscriptionNotFoundException(
                    "No record found for the Subscription Name: " + subscriptionName);
        }
        for (String input : list) {
            Subscription subscription;
            try {
                subscription = mapper.readValue(input, Subscription.class);
                // Inject aggregationtype
                subscription.setAggregationtype(SpringApplicationName);
                return subscription;
            } catch (IOException e) {
                LOGGER.error("Malformed JSON string", e);
            }
        }
        return null;
    }

    @Override
    public boolean doSubscriptionExist(String subscriptionName) {
        final MongoQuery query = MongoCondition.subscriptionNameCondition(subscriptionName);
        ArrayList<String> list = subscriptionRepository.getSubscription(query);
        return !list.isEmpty();
    }

    /**
     * Finds an existing subscription and replaces with a new updated subscription. If the update
     * was successful, any previous matches on aggregations for the old subscription is cleaned from
     * the database.
     *
     * @param subscription the updated subscription to save in the database
     * @param subscriptionName the name of the existing subscription which will be updated
     * @return boolean result of the update operation
     * */
    @Override
    public boolean modifySubscription(Subscription subscription, String subscriptionName) {
        ObjectMapper mapper = new ObjectMapper();
        Document result = null;
        try {
            String stringSubscription = mapper.writeValueAsString(subscription);

            final MongoCondition subscriptionNameCondition = MongoCondition.subscriptionNameCondition(
                    subscriptionName);
            final MongoCondition ldapUserNameCondition = getLdapUserNameCondition(subscriptionName);
            MongoQuery query = MongoQueryBuilder.buildAnd(subscriptionNameCondition,
                    ldapUserNameCondition);

            result = subscriptionRepository.modifySubscription(query, stringSubscription);
            if (result != null) {
//                final MongoCondition subscriptionIdQuery = MongoCondition.subscriptionCondition(
//                        subscriptionName);
//                if (!cleanSubscriptionRepeatFlagHandlerDb(subscriptionIdQuery)) {
//                    LOGGER.info("Subscription  \"{}"
//                            + "\" matched aggregated objects id from repeat flag handler database could not be cleaned during the update of the subscription,\n"
//                            + "probably due to subscription has never matched any aggregated objects and "
//                            + "no matched aggregated objects id has been stored in database for the specific subscription.",
//                            subscriptionName);
//                }
                cleanSubscriptionRepeatFlagHandlerDb(subscriptionName);
            }

        } catch (JSONException | JsonProcessingException e) {
            LOGGER.error("Failed to modify subscription.", e);
            return false;
        }
        return true;
    }

    /**
     * Removes a given subscription from the database. If the operation was successful, any previous
     * matches on aggregations for the removed subscription is also cleaned from the database.
     *
     * @param subscriptionName the name of the subscription to delete
     * @return boolean result of the delete operation
     * */
    @Override
    public boolean deleteSubscription(String subscriptionName) throws AccessException {
        final MongoCondition subscriptionNameCondition = MongoCondition.subscriptionNameCondition(
                subscriptionName);
        final MongoCondition ldapUserNameCondition = getLdapUserNameCondition(subscriptionName);

        MongoQuery deleteQuery = MongoQueryBuilder.buildAnd(subscriptionNameCondition,
                ldapUserNameCondition);

        boolean deleteResult = subscriptionRepository.deleteSubscription(deleteQuery);
        if (deleteResult) {
//            final MongoCondition subscriptionIdQuery = MongoCondition.subscriptionCondition(
//                    subscriptionName);
//            if (!cleanSubscriptionRepeatFlagHandlerDb(subscriptionIdQuery)) {
//                LOGGER.info("Subscription  \"{}"
//                        + "\" matched aggregated objects id from repeat flag handler database could not be cleaned during the removal of subscription,\n"
//                        + "probably due to subscription has never matched any aggregated objects and "
//                        + "no matched aggregated objects id has been stored in database for the specific subscription.",
//                        subscriptionName);
//            }
            cleanSubscriptionRepeatFlagHandlerDb(subscriptionName);
        } else if (doSubscriptionExist(subscriptionName)) {
            String message = "Failed to delete subscription \"" + subscriptionName
                    + "\" invalid ldapUserName";
            throw new AccessException(message);
        }

        return deleteResult;
    }

    @Override
    public List<Subscription> getSubscriptions() throws SubscriptionNotFoundException {
        final MongoCondition query = MongoCondition.emptyCondition();
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
                LOGGER.error("Failed to get subscription.", e);
            }
        }
        return subscriptions;
    }

    private boolean isEncryptionReady(String authType, String username, String password) {
        boolean jasyptPasswordSet = false;
        boolean authTypeSet = false;
        boolean authDetailsSet = EncryptionUtils.verifyAuthenticationDetails(authType, username,
                password);
        if (authDetailsSet) {
            jasyptPasswordSet = encryptor.isJasyptPasswordSet();
            authTypeSet = !AuthenticationType.valueOf(authType).equals(AuthenticationType.NO_AUTH);
        }
        return authDetailsSet && jasyptPasswordSet && authTypeSet;
    }

    private String encryptPassword(String password) {
        String encryptedPassword = encryptor.encrypt(password);
        return EncryptionUtils.addEncryptionParentheses(encryptedPassword);
    }

    private void cleanSubscriptionRepeatFlagHandlerDb(String subscriptionName) {
        LOGGER.debug("Cleaning up previously matched aggregations for subscription: {}.",
                subscriptionName);
        MongoCondition subscriptionIdQuery = MongoCondition.subscriptionCondition(subscriptionName);
        MongoDBHandler mongoDbHandler = subscriptionRepository.getMongoDbHandler();
        mongoDbHandler.dropDocument(dataBaseName, repeatFlagHandlerCollection,
                subscriptionIdQuery);
    }

    /**
     * This method finds whether a given subscription has an owner
     *
     * @param subscriptionName subscription name
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
            LOGGER.error(e.getMessage(), e);
        }
        return ownerExist;
    }

    /**
     * This method ldapUserName, if exists, otherwise return empty string
     *
     * @param subscriptionName subscription name
     * @return a MongoCondition
     */
    private MongoCondition getLdapUserNameCondition(String subscriptionName) {
        String ldapUserName = (ldapEnabled) ? HttpSessionConfig.getCurrentUser() : "";

        boolean ownerExist = doSubscriptionOwnerExist(subscriptionName);
        if (ownerExist) {
            return MongoCondition.ldapUserNameCondition(ldapUserName);
        } else {
            return MongoCondition.emptyCondition();
        }

    }
}
