@SubscriptionTrigger
Feature: Test Subscription Trigger
  
  @SubscriptionTriggerScenario
  Scenario: Test subscription triggering
  	Given The REST API "/subscriptions" is up and running
    And Subscriptions are setup using REST API "/subscriptions"
    When I send Eiffel events
    And Wait for EI to aggregate objects and trigger subscriptions
    Then Mail subscriptions were triggered 
    And Rest subscriptions were triggered
    And I send one previous event again
    And No subscription is retriggered
