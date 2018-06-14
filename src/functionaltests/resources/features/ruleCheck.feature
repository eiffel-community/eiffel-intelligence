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
Feature: Test Check Rule

  @tag1
  Scenario: Execute JMESPath rule on JSON object
    When make a POST request with JMESPath rule "extractionRule.txt" and JSON object "artCEvent.json" to the REST API "/rules/rule-check"
    Then get response code of 200 and content "resultOfExtraction.txt"

  #@tag2
  Scenario: Execute list of JMESPath rules on list of JSON objects
    Given rules checking is enabled
    When make a POST request with list of JMESPath rules and list of JSON objects "inputData.json" to the REST API "/rules/rule-check/aggregation"
    Then get response code of 200 and content "resultList.json"

#  @tag3
#  Scenario: Update subscription using REST API by PUT method and validate updation
#    Given The REST API "/subscriptions" is up and running
#    When I make a PUT request with modified notificationType as "MAIL" to REST API "/subscriptions"
#    Then I get response code of 200 for successful updation
#    And  I can validate modified notificationType "MAIL" with GET request at "/subscriptions/Subscription_Test"
#
#  #@tag4
#  Scenario: Delete subscription using REST API by DELETE method and validate deletion
#    Given The REST API "/subscriptions" is up and running
#    When  I make a DELETE request with subscription name "Subscription_Test" to the  subscription REST API "/subscriptions/"
#    Then  I get response code of 200 for successful delete
#    And   My GET request with subscription name "Subscription_Test" at REST API "/subscriptions/" returns empty String "[]"


