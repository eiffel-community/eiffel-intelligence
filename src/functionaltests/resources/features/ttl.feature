#Author: emelie.pettersson@ericsson.com

@TtlAndNotifications
Feature: TestTTL

  @TestNotificationRetries
  Scenario: Test notification retries
    Given Subscription is created
    When I want to inform subscriber
    Then Verify that request has been retried
    And Check missed notification is in database
    
  @TestTTLforAutomaticSubscriptionTrigger
  Scenario: Test time to live for missed notification and aggregated object with an automatic subscription trigger flow setup
    Given A subscription is created  at the end point "/subscriptions" with non-existent notification meta 
    And I send an Eiffel event and consequently aggregated object and thereafter missed notification is created
    Then the Notification document should be deleted from the database according to ttl value
    And the Aggregated Object document should be deleted from the database according to ttl value 
