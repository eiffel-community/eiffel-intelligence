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
Feature: Source change flow integrationtest
   Scenario: Send eiffel events for source change flow and make sure EI is triggering on REST-POST subscriptions
    Given the rules "src/test/resources/SourceChangeObjectRules.json"
    And the events "src/test/resources/TestSourceChangeObject.json"
    And the upstream input "src/test/resources/UpstreamEventsForSourceChange.json"
    And the resulting aggregated object "src/test/resources/aggregatedSourceChangeObject.json";
    And the expected aggregated object ID is "sb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47"
    And jenkins is set up with a job "source-change-job"
    And that "REST/POST" subscription with jmespath "id=='sb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47'" is uploaded
    When the upstream input events are sent
    And the eiffel events are sent
    Then the jenkins job should have been triggered.
