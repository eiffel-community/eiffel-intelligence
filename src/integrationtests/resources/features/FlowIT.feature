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
Feature: Integrationtest flows

   Scenario: Send eiffel events for artifact flow and make sure EI is triggering on mail subscriptions
    Given that "mail" subscription with jmespath "id=='6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43'" is uploaded
    And the rules "src/test/resources/ArtifactRules_new.json"
    And the events "src/integrationtests/resources/ArtifactFlowTestEvents.json"
    And the resulting aggregated object "src/test/resources/AggregatedDocumentInternalCompositionLatest.json";
    And the expected aggregated object ID is "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43"
    And the upstream input "src/test/resources/upStreamInput.json"
    When the upstream input events are sent
    And the eiffel events are sent
    Then mongodb should contain mail.

  Scenario: Send eiffel events for test execution flow and make sure EI is triggering on REST-POST subscriptions
    Given that "REST/POST" subscription with jmespath "activity_triggered_event_id=='b46ef12d-25gb-4d7y-b9fd-8763re66de47'" is uploaded
    And the rules "src/test/resources/TestExecutionObjectRules.json"
    And the events "src/test/resources/TestExecutionTestEvents.json"
    And the resulting aggregated object "src/test/resources/aggregatedTestActivityObject.json";
    And the expected aggregated object ID is "b46ef12d-25gb-4d7y-b9fd-8763re66de47"
    And jenkins is set up with a job
    When the eiffel events are sent
    Then the jenkins job should have been triggered.

   Scenario: Send eiffel events for source change flow and make sure EI is triggering on REST-POST subscriptions
    Given that "REST/POST" subscription with jmespath "id=='fb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47'" is uploaded
    And the rules "src/test/resources/TestSourceChangeObjectRules.json"
    And the events "src/test/resources/TestSourceChangeObject.json"
    #And the upstream input "src/test/resources/UpstreamEventsForSourceChange.json"
    And the resulting aggregated object "src/test/resources/aggregatedSourceChangeObject.json";
    And the expected aggregated object ID is "fb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47"
    And jenkins is set up with a job
    #When the upstream input events are sent
    When the eiffel events are sent
    Then the jenkins job should have been triggered.
