#Author: vasile.baluta@ericsson.com
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
Feature: Test Subscription CRUD  

  @tag1
  Scenario: Create subscription with JSON object using REST API by POST method
    Given The REST API "/subscriptions" is up and running
    When  I make a POST request with valid "JSON" to the  subscription REST API "/subscriptions"
    Then  I get response code of 200

  #@tag2
  Scenario: Read subscription using REST API by GET method
    Given The REST API "/subscriptions" is up and running
    When  I make a GET request with subscription name "Subscription_Test" to the  subscription REST API "/subscriptions/"
    Then  I get response code of 200
    And   Subscription name is "Subscription_Test"
    
  #@tag3
  Scenario: Update subscription using REST API by PUT method and validate updation
    Given The REST API "/subscriptions" is up and running
    When I make a PUT request with modified notificationType as "MAIL" to REST API "/subscriptions"
    Then I get response code of 200
    And  I can validate modified notificationType "MAIL" with GET request at "/subscriptions/Subscription_Test"
  
  #@tag4 
  Scenario: Delete subscription using REST API by DELETE method and validate deletion
    Given The REST API "/subscriptions" is up and running
    When  I make a DELETE request with subscription name "Subscription_Test" to the  subscription REST API "/subscriptions/"
    Then  I get response code of 200
    And   My GET request with subscription name "Subscription_Test" at REST API "/subscriptions/" returns an empty String


