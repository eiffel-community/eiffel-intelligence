#Author: valentin.tyhonov@ericsson.com

@RuleCheck
Feature: Test Rules Checker

  @ExecuteRuleSingle
  Scenario: Execute JMESPath rule on JSON object
    Given file with JMESPath rules "/ExtractionRule.txt" and file with events "/EiffelArtifactCreatedEvent.json"
    When make a POST request to the REST API "/rules/rule-check" with a single rule
    Then get response code of 200
    And get content "/ExtractedContent.json"

  @ExecuteRuleMultiple
  Scenario: Execute list of JMESPath rules on list of JSON objects
    Given rules checking is enabled
    And file with JMESPath rules "/AggregateListRules.json" and file with events "/AggregateListEvents.json"
    When make a POST request to the REST API "/rules/rule-check/aggregation"
    Then get response code of 200
    And get content "/AggregateResultObject.json"

  @ExecuteIncorrectRule
  Scenario: Execute incorrect list of JMESPath rules on list of JSON objects
    Given rules checking is enabled
    And file with JMESPath rules "/AggregateListRules.json" and file with events "/subscription_single.json"
    When make a POST request to the REST API "/rules/rule-check/aggregation"
    Then get response code of 400

  @ExecuteRuleDisabled
  Scenario: Execute list of JMESPath rules on list of JSON objects, when rules checking is not enabled
    Given rules checking is not enabled
    And file with JMESPath rules "/AggregateListRules.json" and file with events "/AggregateListEvents.json"
    When make a POST request to the REST API "/rules/rule-check/aggregation"
    Then get response code of 503

  @RuleCheckStatus
  Scenario: Check status of test rule page using REST API
    When rules checking is enabled
    Then get request from REST API "/rules/rule-check/testRulePageEnabled" return response code of 200 and status as "true"
    When rules checking is not enabled
    Then get request from REST API "/rules/rule-check/testRulePageEnabled" return response code of 200 and status as "false"
