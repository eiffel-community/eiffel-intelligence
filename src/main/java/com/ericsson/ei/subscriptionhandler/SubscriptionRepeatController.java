package com.ericsson.ei.subscriptionhandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ericsson.ei.exception.SubscriptionHashMapException;

public class SubscriptionRepeatController {
/*
 * Class that controls which Subscription has got a AggregatedObject match and which Subscription that has not matched.
 * 
 * {
 * 	subscriptionA =  false,
 * 	subscriptionB = false,
 *  subscriptionC = true
 * }
 */
	private static ConcurrentMap<String, String> subscriptionRepeatHashMap = new ConcurrentHashMap<String, String>();
	
	/*
	 * Check if Subscription exist in hashmap.
	 * 
	 * @param subscriptionName
	 * @throws SubscriptionHashMapException
	 * @return boolean
	 */
	public static boolean subscriptionExistsInHashMap(String subscriptionName) throws SubscriptionHashMapException {
		try {
			if(subscriptionRepeatHashMap.get(subscriptionName) == null) {
				return false;
			}
		}
		catch (Exception e) {
			throw new SubscriptionHashMapException("Fail to get Subscription in hashmap. Either SubscriptionName exist or something else failed. ERROR: " + e.toString());
		}
		return true;
	}
	
	/*
	 * Add subscription to hashmap.
	 * If subscription exist in hashMap, the Repeat Flag value will be overwritten with the new value.
	 * 
	 * @param subscriptionName
	 * @param repeatValue
	 * @throws SubscriptionHashMapException
	 */
	public static void addSubscriptionToHashMap(String subscriptionName, String repeatValue) throws SubscriptionHashMapException {
		try {
			subscriptionRepeatHashMap.put(subscriptionName, repeatValue);
		}
		catch (Exception e) {
			throw new SubscriptionHashMapException("Failed to add Subscription: " + subscriptionName +
					" Reapet Value: " + repeatValue +
					" ERROR: " + e.toString());
		}
	}
	
	/*
	 * Get subscription Reapet boolean Value.
	 * 
	 * @param subscriptionName
	 */
	public static boolean getSubscriptionRepeatBooleanValue(String subscriptionName) {
		return Boolean.valueOf(subscriptionRepeatHashMap.get(subscriptionName));
	}
	
	/*
	 * Get HashMap object.
	 * 
	 */
	public static ConcurrentMap<String, String> getSubscriptionsHashMap() {
		return subscriptionRepeatHashMap;
	}
	
	/*
	 * Remove subscription in hashmap.
	 * 
	 * @param subscriptionName
	 * @throws SubscriptionHashMapException
	 */
	public static void removeSubscriptionFromHashMap(String subscriptionName) throws SubscriptionHashMapException {
		if (subscriptionRepeatHashMap.remove(subscriptionName) == null) {
			throw new SubscriptionHashMapException(
					"SubscriptionName " + subscriptionName + " don't exists in hashmap. Can't be removed");
		}
	}
	
	/*
	 * Remove all subscriptions in hashmap.
	 * (Clears current HashMap)
	 */
	public static void removeAllSubscriptionsFromHashMap() {
		subscriptionRepeatHashMap.clear();
	}
}
