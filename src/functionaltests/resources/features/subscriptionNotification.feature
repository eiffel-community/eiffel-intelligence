@SubscriptionTrigger
Feature: Test Subscription Trigger

   @SubscriptionNotificationOK
   Scenario: Test subscription triggering
     Given The REST API is up and running
     And Mail server is up
     And Subscriptions are created
     When I send Eiffel events
     And Wait for EI to aggregate objects
     Then Mail subscriptions were triggered
     And Rest subscriptions were triggered
     When I send one previous event again
     And No subscription is retriggered

  @FailedSubscriptionNotification
  Scenario: Test missed notifications are created when notifications fail
    Given The REST API is up and running
    And Subscriptions with bad notification meta are created
    When I send Eiffel events
    When I send one previous event again
    And Wait for EI to aggregate objects
    And Missed notifications should exist in the database
