#Author: emelie.pettersson@ericsson.com

@TtlAndNotifications
Feature: Test TTL

  @TestNotificationRetries
  Scenario: Test notification retries
    Given Subscription is created
    When I want to inform subscriber
    Then Verify that request has been retried
    And Check missed notification is in database
    
  @TestTTLforAutomaticSubscriptionTrigger
  Scenario: Test time to live for missed notification and aggregated object with an automatic subscription trigger flow setup
    Given A subscription is created using "/subscriptions" with non-working notification meta 
    And I send an Eiffel event
    When Aggregated object is created
    And Missed notification is created
    Then Notification document should be deleted from the database
    And Aggregated Object document should be deleted from the database
