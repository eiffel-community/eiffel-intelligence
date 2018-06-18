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
Feature: Test Rules Checker

  @tag1
  Scenario: Execute JMESPath rule on JSON object
    When make a POST request with JMESPath rule "extractionRule.txt" and JSON object "artCEvent.json" to the REST API "/rules/rule-check"
    Then get response code of 200 and content "resultOfExtraction.json"

  @tag2
  Scenario: Execute list of JMESPath rules on list of JSON objects
    Given rules checking is enabled
    When make a POST request with list of JMESPath rules "rules.json" and list of JSON objects "events.json" to the REST API "/rules/rule-check/aggregation"
    Then get response code of 200 and content "resultAggregatedObject.json"

  @tag3
  Scenario: Execute incorrect list of JMESPath rules on list of JSON objects
    Given rules checking is enabled
    When make a POST request with list of JMESPath rules "rules.json" and list of JSON objects "emptyArray.json" to the REST API "/rules/rule-check/aggregation"
    Then get response code of 400 and content "badRequestResponse.json"

  @tag4
  Scenario: Execute list of JMESPath rules on list of JSON objects, when rules checking is not enabled
    Given rules checking is not enabled
    When make a POST request with list of JMESPath rules "rules.json" and list of JSON objects "events.json" to the REST API "/rules/rule-check/aggregation"
    Then get response code of 503 and content "environmentDisabledResponse.json"