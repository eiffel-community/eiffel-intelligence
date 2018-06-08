#Author:
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
Feature: Test Subscription Content

  @tag1
  Scenario: Test creating valid subscriptions
  Given No subscriptions exist
  When Create subscription request
  Then The subscription is created successfully
  And Valid subscription exists


  @tag2
  Scenario: Test duplicate subscriptions are rejected
  Given Subscription mySubscription already exists
  When I create a duplicate subscription
  Then The new subscription is rejected
  And mySubscription is not duplicated


  @tag3
  Scenario: Test invalid subscriptions are rejected
  Given I delete mySubscription
  And Subscriptions does not exist
  When I create an invalid subscription
  Then The invalid subscription is rejected
  And The invalid subscription does not exist


