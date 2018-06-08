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

  @tag1
  Scenario: Subscription should only match Aggregated Object only one time
    Given Subscription that will match this Aggregated Object
    When  Publish events on MessageBus
    Then  Subscription should only match Aggregated Object only one time

  #@tag2
  Scenario: Remove Subscription via RestApi
    Given Add subscription to MongoDB
    When  I make a DELETE request with subscription name "Subscription_Test" to the subscription REST API "/subscriptions/"
    Then  Check in MongoDB that subscription has been removed
    And   Check in MongoDB RepeatFlagHandler collection that the subscription has been removed

  #@tag3
  Scenario: Subscription should match Aggregated Object at least two times
    Given Subscription that will match this Aggregated Object
    When  Publish events on MessageBus
    Then  Subscription should match Aggregated Object at least two times