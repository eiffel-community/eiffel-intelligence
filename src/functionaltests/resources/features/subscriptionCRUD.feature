#Author: vasile.baluta@ericsson.com

@SubscriptionCRUD
Feature: Test Subscription CRUD

  @RESTCreateSubscription
  Scenario: Create subscription with JSON object using REST API by POST method
    Given The REST API "/subscriptions" is up and running
    When  I make a POST request with valid "JSON" to the subscription REST API "/subscriptions"
    Then  I get response code of 200

  @RESTReadSubscription
  Scenario: Read subscription using REST API by GET method
    Given The REST API "/subscriptions" is up and running
    When  I make a GET request with subscription name "Subscription_Test" to the subscription REST API "/subscriptions/"
    Then  I get response code of 200
    And   Subscription name is "Subscription_Test"

  @RESTUpdateSubscription
  Scenario Outline: Update subscription using REST API by PUT method and validate updation
    Given The REST API "/subscriptions" is up and running
    When I make a PUT request with modified "<key>" as "<newValue>" to REST API "/subscriptions"
    Then I get response code of 200
    And  I can validate modified "<key>" "<newValue>" with GET request at "/subscriptions/Subscription_Test"

# Values to be added to the variables given above
  Examples:
    | key              | newValue                         |
    | notificationMeta | new.notification.meta/some/path/ |

  @RESTDeleteSubscription
  Scenario: Delete subscription using REST API by DELETE method and validate deletion
    Given The REST API "/subscriptions" is up and running
    When  I make a DELETE request with subscription name "Subscription_Test" to the subscription REST API "/subscriptions/"
    Then  I get response code of 200
    And   My GET request with subscription name "Subscription_Test" at REST API "/subscriptions/" returns an empty String