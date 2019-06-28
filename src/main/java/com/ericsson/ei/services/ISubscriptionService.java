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

import java.util.List;

import org.springframework.expression.AccessException;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface ISubscriptionService {

    /**
     *
     * @param subscription
     * @throws JsonProcessingException
     */
    void addSubscription(Subscription subscription) throws JsonProcessingException;

    /**
     *
     * @return
     * @throws SubscriptionNotFoundException
     */
    List<Subscription> getSubscriptions() throws SubscriptionNotFoundException;

    /**
     *
     * @param subscriptionName
     * @return
     * @throws SubscriptionNotFoundException
     */
    Subscription getSubscription(String subscriptionName) throws SubscriptionNotFoundException;

    /**
     *
     * @param subscription
     * @param subscriptionName
     * @return
     */
    boolean modifySubscription(Subscription subscription, String subscriptionName);

    /**
     *
     * @param subscriptionName
     * @return
     * @throws AccessException
     */
    boolean deleteSubscription(String subscriptionName) throws AccessException;

    /**
     * doSubscriptionExist method checks the is there any Subscription By
     * Subscription Name
     *
     * @param subscriptionName
     * @return true when Subscription available with same name. Otherwise
     *         returns false.
     */
    boolean doSubscriptionExist(String subscriptionName);

}
