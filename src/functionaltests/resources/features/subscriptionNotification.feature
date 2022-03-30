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
    And Failed notification db should contain 0 objects

  @FailedSubscriptionNotification
  Scenario: Test failed notifications are created
    Given The REST API is up and running
    And Subscriptions with bad notification meta are created
    When I send Eiffel events
    When I send one previous event again
    And Wait for EI to aggregate objects
    And Failed notification db should contain 1 objects

  @AddedDefaultValuesSenderAndSubject
  Scenario: Test default values mail notification
    Given The REST API is up and running
    And Mail server is up
    And Subscriptions are created
    When I send Eiffel events
    And Wait for EI to aggregate objects
    Then Notification email contains 'noreply@domain.com' and 'Email Subscription Notification' values
    Then Mail subscriptions were triggered
