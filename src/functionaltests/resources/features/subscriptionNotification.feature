@SubscriptionTrigger
Feature: Test Subscription Trigger

  @SubscriptionNotificationOK
  Scenario: Test subscription triggering
    Given The REST API "/subscriptions" is up and running
    And mail server is up
    And Subscriptions are setup using REST API "/subscriptions"
    When I send Eiffel events
    And Wait for EI to aggregate objects
    Then Mail subscriptions were triggered
    And Rest subscriptions were triggered
    When I send one previous event again
    And No subscription is retriggered

  @FailedSubscriptionNotification
  Scenario: Test missed notifications are created when notifications fail
    Given The REST API "/subscriptions" is up and running
    And Bad subscriptions are created using "/subscriptions"
    When I send Eiffel events
    When I send one previous event again
    And Wait for EI to aggregate objects
    And Missed notifications should exist in the database
