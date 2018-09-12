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
    Given file with JMESPath rules "/ExtractionRule.txt" and file with events "/EiffelArtifactCreatedEvent.json"
    When make a POST request to the REST API "/rules/rule-check" with a single rule
    Then get response code of 200
    And get content "/ExtractedContent.json"

  #@tag2
  Scenario: Execute list of JMESPath rules on list of JSON objects
    Given rules checking is enabled
    And file with JMESPath rules "/AggregateListRules.json" and file with events "/AggregateListEvents.json"
    When make a POST request to the REST API "/rules/rule-check/aggregation"
    Then get response code of 200
    And get content "/AggregateResultObject.json"

  #@tag3
  Scenario: Execute incorrect list of JMESPath rules on list of JSON objects
    Given rules checking is enabled
    And file with JMESPath rules "/AggregateListRules.json" and file with events "/subscription_single.json"
    When make a POST request to the REST API "/rules/rule-check/aggregation"
    Then get response code of 400

  #@tag4
  Scenario: Execute list of JMESPath rules on list of JSON objects, when rules checking is not enabled
    Given rules checking is not enabled
    And file with JMESPath rules "/AggregateListRules.json" and file with events "/AggregateListEvents.json"
    When make a POST request to the REST API "/rules/rule-check/aggregation"
    Then get response code of 503

  #@tag5
  Scenario: Check status of test rule page using REST API
    When rules checking is enabled
    Then get request from REST API "/rules/rule-check/testRulePageEnabled" return response code of 200 and status as "true"
    When rules checking is not enabled
    Then get request from REST API "/rules/rule-check/testRulePageEnabled" return response code of 200 and status as "false"
