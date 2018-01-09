package com.ericsson.ei.subscriptionhandler.test;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;

import com.ericsson.ei.exception.SubscriptionHashMapException;
import com.ericsson.ei.subscriptionhandler.SubscriptionRepeatController;

public class SubscriptionRepeatControllerTest {
		
	@Before
	public void tearUp() {
		SubscriptionRepeatController.getSubscriptionsHashMap().clear();
	}
		
	@Test
	public void addSubscriptionAndRepeatValueToHashMapTest() {
		
		String subscriptionName = "SubA";
		String repeatValue = "false";
		
		boolean result = true;
		
		try {
			SubscriptionRepeatController.addSubscriptionToHashMap(subscriptionName, repeatValue);
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		assertEquals(true, result);;
	}
	
	@Test
	public void checkSubscriptionRepeatValueInHashMapTest() {
				
		String subscriptionName = "SubA";
		String repeatValue = "false";
		
		boolean result=true;
		
		try {
			SubscriptionRepeatController.addSubscriptionToHashMap(subscriptionName, repeatValue);
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
		
		
		try {
			result = SubscriptionRepeatController.subscriptionExistsInHashMap(subscriptionName);
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		
		assertEquals(true, result);;
	}
	
	@Test
	public void checkSubscriptionRepeatValueInHashMapFailTest() {
				
		String subscriptionName = "SubA";
		String subscriptionNameNotExist = "SubB";
		String repeatValue = "false";
		
		boolean result=true;
		
		try {
			SubscriptionRepeatController.addSubscriptionToHashMap(subscriptionName, repeatValue);
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
		
		
		try {
			result = SubscriptionRepeatController.subscriptionExistsInHashMap(subscriptionNameNotExist);
		} catch (SubscriptionHashMapException e) {
			result = false;
		}
		
		assertEquals(false, result);;
	}
	
	@Test
	public void addSeveralSubscriptionAndRepeatValueToHashMapTest() {
		
		String subscriptionName = "SubA";
		String repeatValue = "false";
		
		boolean result=true;
		
		try {
			SubscriptionRepeatController.addSubscriptionToHashMap(subscriptionName, repeatValue);
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		assertEquals(true, result);


		subscriptionName = "SubB";
		repeatValue = "false";
		
		result=true;
		
		try {
			SubscriptionRepeatController.addSubscriptionToHashMap(subscriptionName, repeatValue);
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		assertEquals(true, result);
		
		
		subscriptionName = "SubC";
		repeatValue = "false";
		
		result=true;
		
		try {
			SubscriptionRepeatController.addSubscriptionToHashMap(subscriptionName, repeatValue);
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		assertEquals(true, result);
		
		
		subscriptionName = "SubD";
		repeatValue = "true";
		
		result=true;
		
		try {
			SubscriptionRepeatController.addSubscriptionToHashMap(subscriptionName, repeatValue);
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		assertEquals(true, result);
		
		result = true;
		try {
			SubscriptionRepeatController.subscriptionExistsInHashMap("SubA");
			SubscriptionRepeatController.subscriptionExistsInHashMap("SubB");
			SubscriptionRepeatController.subscriptionExistsInHashMap("SubC");
			SubscriptionRepeatController.subscriptionExistsInHashMap("SubD");
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		assertEquals(true, result);

		try {
			result = SubscriptionRepeatController.subscriptionExistsInHashMap("SubE");
		} catch (SubscriptionHashMapException e) {
			result = false;
		}
		assertEquals(false, result);
	}

	
	@Test
	public void testRemoveSubscriptionAndRepeatValueToHashMapTest() {
		
		boolean result=true;
		
		boolean resultA=true;
		boolean resultB=true;
		boolean resultC=true;
		boolean resultD=true;

		
		try {
			SubscriptionRepeatController.addSubscriptionToHashMap("SubA", "false");
			SubscriptionRepeatController.addSubscriptionToHashMap("SubB", "false");
			SubscriptionRepeatController.addSubscriptionToHashMap("SubC", "false");
			SubscriptionRepeatController.addSubscriptionToHashMap("SubD", "false");
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		assertEquals(true, result);
		
		try {
			SubscriptionRepeatController.addSubscriptionToHashMap("SubA", "false");
			SubscriptionRepeatController.addSubscriptionToHashMap("SubB", "false");
			SubscriptionRepeatController.addSubscriptionToHashMap("SubC", "false");
			SubscriptionRepeatController.addSubscriptionToHashMap("SubD", "false");
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		
		result = true;
		try {
			SubscriptionRepeatController.removeSubscriptionFromHashMap("SubB");
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		assertEquals(true, result);
		
		result = true;
		try {
			resultB = SubscriptionRepeatController.subscriptionExistsInHashMap("SubB");
		} catch (SubscriptionHashMapException e) {
			result = false;
		}
		assertEquals(false, resultB);
		assertEquals(true, result);
		
		result = true;
		try {
			resultA = SubscriptionRepeatController.subscriptionExistsInHashMap("SubA");
			resultC = SubscriptionRepeatController.subscriptionExistsInHashMap("SubC");
			resultD = SubscriptionRepeatController.subscriptionExistsInHashMap("SubD");
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			result = false;
		}
		assertEquals(true, resultA);
		assertEquals(false, resultB);
		assertEquals(true, resultC);
		assertEquals(true, resultD);
		assertEquals(true, result);
	}
	
	@Test
	public void removeAllSubscriptionsFromHashMapTest() {
		boolean result=true;
		
		try {
			SubscriptionRepeatController.addSubscriptionToHashMap("SubA", "false");
			SubscriptionRepeatController.addSubscriptionToHashMap("SubB", "false");
			SubscriptionRepeatController.addSubscriptionToHashMap("SubC", "false");
			SubscriptionRepeatController.addSubscriptionToHashMap("SubD", "false");
		} catch (SubscriptionHashMapException e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		assertEquals(true, result);
		
		SubscriptionRepeatController.removeAllSubscriptionsFromHashMap();
		
		try {
			result = SubscriptionRepeatController.subscriptionExistsInHashMap("SubA");
		} catch (SubscriptionHashMapException e) {
			result = false;
		}
		assertEquals(false, result);
		
		try {
			result = SubscriptionRepeatController.subscriptionExistsInHashMap("SubB");
		} catch (SubscriptionHashMapException e) {
			result = false;
		}
		assertEquals(false, result);
		
		try {
			result = SubscriptionRepeatController.subscriptionExistsInHashMap("SubC");
		} catch (SubscriptionHashMapException e) {
			result = false;
		}
		assertEquals(false, result);
		
		try {
			result = SubscriptionRepeatController.subscriptionExistsInHashMap("SubD");
		} catch (SubscriptionHashMapException e) {
			result = false;
		}
		assertEquals(false, result);
	}
	
	@Test
	public void getSubscriptionsRepeatValueHashMapTest() {
		
		SubscriptionRepeatController.removeAllSubscriptionsFromHashMap();
		assertEquals(true, SubscriptionRepeatController.getSubscriptionsHashMap().isEmpty());
	}
}
