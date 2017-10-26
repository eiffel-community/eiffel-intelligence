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

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionNotFoundException;

public interface ISubscriptionService {

    
    /**
     * 
     * @param Subscription
     * @return
     */
    boolean addSubscription(Subscription Subscription);
    
    /**
     * 
     * @return
     * @throws SubscriptionNotFoundException 
     */
    List<Subscription> getSubscription() throws SubscriptionNotFoundException;
    
    /**
     * 
     * @param name
     * @return
     * @throws SubscriptionNotFoundException
     */
    Subscription getSubscription(String name) throws SubscriptionNotFoundException;
    
    /**
     * 
     * @param subscription
     * @param subscriptionName
     * @return
     */
    boolean modifySubscription(Subscription subscription, String subscriptionName);
    
    /**
     * 
     * @param name
     * @return 
     * @throws SubscriptionNotFoundException
     */
    boolean deleteSubscription(String name);

    /**
     * doSubscriptionExist method checks the is there any Subscription By Subscription Name
     * @param name
     * @return true when Subscription available with same name. Otherwise returns false.
     */
    boolean doSubscriptionExist(String name);


}
