package com.ericsson.ei.repository;

import java.util.ArrayList;

public interface ISubscriptionRepository {
    
    ArrayList<String> getSubscription(String name);
    
    boolean modifySubscription(String stringSubscription, String subscriptionName);
    
    boolean deleteSubscription(String name);
    
    boolean addSubscription(String subscription);
    
}
