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
