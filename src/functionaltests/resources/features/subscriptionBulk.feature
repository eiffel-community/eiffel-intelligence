#Author: valentin.tyhonov@ericsson.com
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
Feature: Test Subscription Bulk Operations

  @tag1
  Scenario: Create multiple subscriptions using REST API
    Given file with subscriptions "subscriptions_multiple.json"
    When make a POST request with list of subscriptions to the subscription REST API "/subscriptions"
    Then get response code of 200 and retrieved subscriptions using REST API "/subscriptions" are same as given

  @tag2
  Scenario: Create multiple subscriptions using REST API
    Given file with subscriptions "subscriptions_multiple_updated.json"
    When make a PUT request with list of subscriptions to the subscription REST API "/subscriptions"
    Then get response code of 200 and retrieved subscriptions using REST API "/subscriptions" are same as given