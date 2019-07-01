#Author: andriy.nedvyha@ericsson.com

@SubscriptionRepeatHandler
Feature: Test Subscription Repeat Handler

  @SubscriptionRepeatFalse
  Scenario: Subscription match the Aggragated Object one time
    Given Publish events on Message Bus
    When  In MongoDb RepeatFlagHandler collection the subscription has matched the AggrObjectId
    Then  I make a DELETE request with subscription name "Subscription_Test" to the subscription REST API "/subscriptions/"
    And   Check in MongoDB RepeatFlagHandler collection that the subscription has been removed

  @SubscriptionRepeatTrue
  Scenario: Subscription match the Aggragated Object at least two time
    Given Publish events on Message Bus
    When  In MongoDb RepeatFlagHandler collection the subscription has matched the AggrObjectId at least two times
    Then  I make a DELETE request with subscription name "Subscription_Test" to the subscription REST API "/subscriptions/"
    And   Check in MongoDB RepeatFlagHandler collection that the subscription has been removed