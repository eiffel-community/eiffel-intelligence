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

  Scenario: 
    Send eiffel events for artifact flow and make sure EI is triggering 
    on mail subscriptions

    # Setup Eiffel Intelligence
    Given the rules "src/main/resources/ArtifactRules.json"
    And the events "src/test/resources/ArtifactFlowTestEvents.json"
    And the resulting aggregated object "src/test/resources/AggregatedDocumentInternalCompositionLatestIT.json";
    And the upstream input "src/test/resources/upStreamInput.json"
    Then the expected aggregated object ID is "aacc3c87-75e0-4b6d-88f5-b1a5d4e62b43"
    # Setup subscription
    Given subscription object for "MAIL" with name "MailTestSubscription" is created
    When notification meta "some.cool.email@ericsson.com" is set in subscription
    And rest post body media type is set to "crazy-pinguin" is set in subscription
    And paremeter form key "" and form value "to_string(@)" is added in subscription
    And condition "id=='aacc3c87-75e0-4b6d-88f5-b1a5d4e62b43'" at requirement index '0' is added in subscription
    Then subscription is uploaded
    # Send Events and Check job triggered
    Given all previous steps passed
    When the upstream input events are sent
    And the eiffel events are sent
    Then mongodb should contain mail.
