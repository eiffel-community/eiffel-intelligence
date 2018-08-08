#Author: andriy.nedvyha@ericsson.com
#Keywords Summary :
#Feature: List of scenarios.
#Scenario: Business rule through list of steps with arguments.
#Given: Some precondition step
#When: Some key actions
#Then: To observe outcomes or validation
#And,But: To enumerate more Given,When,Then steps
#Scenario Outline: List of steps for data-driven as an Examples and <placeholder>
#Examples: Container for s table
#Background: List of steps run before each of the scenarios
#""" (Doc Strings)
#| (Data Tables)
#@ (Tags/Labels):To group Scenarios
#<> (placeholder)
#""
## (Comments)
#Sample Feature Definition Template
@tag
Feature: Test Subscription Repeat Handler

  @SubscriptionRepeatHandler
  Scenario: Subscription match the Aggragated Object one time
    Given Publish events on Message Bus
    When  In MongoDb RepeatFlagHandler collection the subscription has matched the AggrObjectId
    Then  I make a DELETE request with subscription name "Subscription_Test" to the subscription REST API "/subscriptions/"
    And   Check in MongoDB RepeatFlagHandler collection that the subscription has been removed

  @SubscriptionRepeatHandler
  Scenario: Subscription match the Aggragated Object at least two time
    Given Publish events on Message Bus
    When  In MongoDb RepeatFlagHandler collection the subscription has matched the AggrObjectId at least two times
    Then  I make a DELETE request with subscription name "Subscription_Test" to the subscription REST API "/subscriptions/"
    And   Check in MongoDB RepeatFlagHandler collection that the subscription has been removed