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
Feature: Artifact flow Integrationtest
   Scenario: Send eiffel events for artifact flow and make sure EI is triggering on mail subscriptions
    Given that "mail" subscription with jmespath "id=='aacc3c87-75e0-4b6d-88f5-b1a5d4e62b43'" is uploaded
    And the rules "src/test/resources/ArtifactRules.json"
    And the events "src/test/resources/ArtifactFlowTestEvents.json"
    And the resulting aggregated object "src/test/resources/AggregatedDocumentInternalCompositionLatestIT.json";
    And the expected aggregated object ID is "aacc3c87-75e0-4b6d-88f5-b1a5d4e62b43"
    And the upstream input "src/test/resources/upStreamInput.json"
    When the upstream input events are sent
    And the eiffel events are sent
    Then mongodb should contain mail.


