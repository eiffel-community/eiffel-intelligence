#Author: valentin.tyhonov@ericsson.com

@SubscriptionBulk
Feature: Test Subscription Bulk Operations

  @CreateSubscriptions
  Scenario: Create multiple subscriptions using REST API
    Given file with subscriptions "/subscriptions_multiple.json"
    When make a POST request with list of subscriptions to the subscription REST API "/subscriptions"
    Then get response code of 200
    And number of retrieved subscriptions using REST API "/subscriptions" is 3
    And retrieved subscriptions are same as given

  @GetSubscriptions
  Scenario: Fetch multiple subscriptions using REST API, one subscription does not exist
    When make a GET request with list of subscriptions names "Subscription_Test_1,Subscription_Test_Not_Found,Subscription_Test_2" to the subscription REST API "/subscriptions"
    Then get response code of 200
    And get in response content 2 found subscriptions and not found subscription name "Subscription_Test_Not_Found"

  @CreateWithDuplicate
   Scenario: Create multiple subscriptions using REST API, one subscription already exists
     Given file with subscriptions "/subscriptions_multiple_wrong.json"
     When make a POST request with list of subscriptions to the subscription REST API "/subscriptions"
     Then get response code of 400
     And get in response content subscription "Subscription_Test_2"
     And number of retrieved subscriptions using REST API "/subscriptions" is 5

  @DeleteSubscriptions
  Scenario: Delete multiple subscriptions using REST API
    When make a DELETE request with list of subscriptions names "Subscription_Test_4,Subscription_Test_5" to the subscription REST API "/subscriptions"
    Then get response code of 200
    And number of retrieved subscriptions using REST API "/subscriptions" is 3

  @UpdateSubscriptions
  Scenario: Update multiple subscriptions using REST API
    Given file with subscriptions "/subscriptions_multiple_updated.json"
    When make a PUT request with list of subscriptions to the subscription REST API "/subscriptions"
    Then get response code of 200
    And number of retrieved subscriptions using REST API "/subscriptions" is 3
    And retrieved subscriptions are same as given

  @UpdateSubscriptionsNonExisting
  Scenario: Update multiple subscriptions using REST API, one subscription does not exist
    Given file with subscriptions "/subscriptions_multiple_wrong_updated.json"
    When make a PUT request with list of subscriptions to the subscription REST API "/subscriptions"
    Then get response code of 400
    And get in response content subscription "Subscription_Test_Not_Found"
    And number of retrieved subscriptions using REST API "/subscriptions" is 3

  @DeleteSubscriptionsNonExisting
  Scenario: Delete multiple subscriptions using REST API, one subscription does not exist
    When make a DELETE request with list of subscriptions names "Subscription_Test_1,Subscription_Test_2,Subscription_Test_Not_Found,Subscription_Test_3" to the subscription REST API "/subscriptions"
    Then get response code of 400
    And get in response content subscription "Subscription_Test_Not_Found"
    And number of retrieved subscriptions using REST API "/subscriptions" is 0
