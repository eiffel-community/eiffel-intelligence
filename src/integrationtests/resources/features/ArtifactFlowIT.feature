#Author: erik.edling@ericsson.com

Feature: Artifact flow Integrationtest

  Scenario:
    Send eiffel events for artifact flow and make sure EI is triggering
    on mail subscriptions

    # Setup Eiffel Intelligence
    Given the rules "src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json"
    And the events "src/test/resources/ArtifactFlowTestEvents.json"
    And the resulting aggregated object "src/test/resources/AggregatedDocumentInternalCompositionLatestIT.json";
    And the upstream input "src/test/resources/upStreamInput.json"
    Then the expected aggregated object ID is "aacc3c87-75e0-4b6d-88f5-b1a5d4e62b43"

    # Setup subscription
    Given subscription object of type "MAIL" with name "MailTestSubscription" is created
    When notification meta "some.cool.email@ericsson.com,some.other.cool.email@ericsson.com" is set in subscription
    And parameter form key "" and form value "to_string(@)" is added in subscription
    And condition "id=='aacc3c87-75e0-4b6d-88f5-b1a5d4e62b43'" at requirement index '0' is added in subscription
    Then subscription is uploaded

    # Send Events and Check job triggered
    When the upstream input events are sent
    And the eiffel events are sent
    Then mongodb should contain "2" mails.
