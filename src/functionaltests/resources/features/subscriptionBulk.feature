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
Feature: Test Subscription Bulk Operations

  Scenario: Create multiple subscriptions using REST API
    Given file with subscriptions "/subscriptions_multiple.json"
    When make a POST request with list of subscriptions to the subscription REST API "/subscriptions"
    Then get response code of 200
    And number of retrieved subscriptions using REST API "/subscriptions" is 3
    And retrieved subscriptions are same as given

  Scenario: Fetch multiple subscriptions using REST API, one subscription does not exist
    When make a GET request with list of subscriptions names "Subscription_Test_1,Subscription_Test_Not_Found,Subscription_Test_2" to the subscription REST API "/subscriptions"
    Then get response code of 200
    And get in response content 2 found subscriptions and not found subscription name "Subscription_Test_Not_Found"

   Scenario: Create multiple subscriptions using REST API, one subscription already exists
     Given file with subscriptions "/subscriptions_multiple_wrong.json"
     When make a POST request with list of subscriptions to the subscription REST API "/subscriptions"
     Then get response code of 400
     And get in response content subscription "Subscription_Test_2"
     And number of retrieved subscriptions using REST API "/subscriptions" is 5

  Scenario: Delete multiple subscriptions using REST API
    When make a DELETE request with list of subscriptions names "Subscription_Test_4,Subscription_Test_5" to the subscription REST API "/subscriptions"
    Then get response code of 200
    And number of retrieved subscriptions using REST API "/subscriptions" is 3

  Scenario: Update multiple subscriptions using REST API
    Given file with subscriptions "/subscriptions_multiple_updated.json"
    When make a PUT request with list of subscriptions to the subscription REST API "/subscriptions"
    Then get response code of 200
    And number of retrieved subscriptions using REST API "/subscriptions" is 3
    And retrieved subscriptions are same as given

  Scenario: Update multiple subscriptions using REST API, one subscription does not exist
    Given file with subscriptions "/subscriptions_multiple_wrong_updated.json"
    When make a PUT request with list of subscriptions to the subscription REST API "/subscriptions"
    Then get response code of 400
    And get in response content subscription "Subscription_Test_Not_Found"
    And number of retrieved subscriptions using REST API "/subscriptions" is 3

  Scenario: Delete multiple subscriptions using REST API, one subscription does not exist
    When make a DELETE request with list of subscriptions names "Subscription_Test_1,Subscription_Test_2,Subscription_Test_Not_Found,Subscription_Test_3" to the subscription REST API "/subscriptions"
    Then get response code of 400
    And get in response content subscription "Subscription_Test_Not_Found"
    And number of retrieved subscriptions using REST API "/subscriptions" is 0
