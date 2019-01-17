#Author: erik.edling@ericsson.com
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
Feature: Test execution flow integrationtest
  Scenario: Send eiffel events for test execution flow and make sure EI is triggering on REST-POST subscriptions
    Given the rules "src/test/resources/TestExecutionObjectRules.json"
    And the events "src/test/resources/TestExecutionTestEvents.json"
    And the resulting aggregated object "src/test/resources/aggregatedTestActivityObject.json";
    And the expected aggregated object ID is "e46ef12d-25gb-4d7y-b9fd-8763re66de47"
    And jenkins is set up with a job "test-execution-job"
    And that "REST/POST" subscription with jmespath "id=='e46ef12d-25gb-4d7y-b9fd-8763re66de47'" is uploaded
    When the eiffel events are sent
    Then the jenkins job should have been triggered.
