package com.ericsson.ei.subscriptionhandler;

//import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.exception.SubscriptionValidationException;
//import com.ericsson.ei.controller.model.Requirement;

public class SubscriptionValidator {
	
	private static final Logger LOG = LoggerFactory.getLogger(SubscriptionValidator.class);
	
	/*
	 * Validation of parameters values in subscriptions objects.
	 */
	public void validateSubscription(Subscription subscription) throws SubscriptionValidationException {
		
		LOG.info("Validation of subscription " + subscription.getSubscriptionName() + " Started.");
		
		this.validateSubscriptionName(subscription.getSubscriptionName());
		this.validateNotificationMessage(subscription.getNotificationMessage());
		this.validateNotificationMeta(subscription.getNotificationMeta());
		this.validateNotificationType(subscription.getNotificationType());
		this.validateRepeat(subscription.getRepeat());
//		List<Requirement> reqList = subscription.getRequirements();
//		for (int i=0; i < reqList.size(); i++) {
//			this.validateJmespath(reqList.get(i).getConditions().get(0).getJmespath());
//		}
		LOG.info("Validating of subscription " + subscription.getSubscriptionName() + " finished successfully.");
	}
	
	/*
	 * Validation of subscriptionName parameter
	 */
	public void validateSubscriptionName(String subscriptionName) throws SubscriptionValidationException {
		
		String regex = "^[A-Za-z0-9_]+$";
		
		if (!Pattern.matches(regex, subscriptionName)) {
			throw new SubscriptionValidationException("Wrong format of SubscriptionName: " + subscriptionName);
		}	
	}
	
	/*
	 * Validation of notificationMessage parameter
	 */
	public void validateNotificationMessage(String notificationMessage) throws SubscriptionValidationException {
		
		String regex = "^[A-Za-z0-9@.]+$";
		
		if (!Pattern.matches(regex, notificationMessage)) {
			throw new SubscriptionValidationException("Wrong format of NotificationMessage: " + notificationMessage);
		}	
	}
	
	/*
	 * Validation of notificationMeta parameter
	 */
	public void validateNotificationMeta(String notificationMeta) throws SubscriptionValidationException {
		String regex = ".*[\\s].*";
		
		if (Pattern.matches(regex, notificationMeta)) {
			throw new SubscriptionValidationException("Wrong format of NotificationMeta: " + notificationMeta);
		}
	}
	
	/*
	 * Validation of notificationType parameter
	 */
	public void validateNotificationType(String notificationType) throws SubscriptionValidationException {
		String regexMail = "[\\s]*MAIL[\\\\s]*";
		String regexRestPost = "[\\s]*REST_POST[\\\\s]*";
		
		if (!(Pattern.matches(regexMail, notificationType) || Pattern.matches(regexRestPost, notificationType))) {
			throw new SubscriptionValidationException("Wrong format of NotificationType: " + notificationType);
		}
	}
	
	/*
	 * Validation of repeat parameter
	 */
	public void validateRepeat(Boolean repeat) throws SubscriptionValidationException {
		if (!(repeat instanceof Boolean)) {
			throw new SubscriptionValidationException("Wrong format of Repeat: " + repeat.toString());
		}
		
	}
	
//	public void validateJmespath(String jmespath) {
////		TODO: Validator for Jmepath syntax need to be implemented here.
//		throw new SubscriptionValidationException("Wrong format/syntax of Jmepath: " + jmespath);
//	}
}
