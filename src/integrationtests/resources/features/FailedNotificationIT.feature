#Author: christoffer.cortes.sjowall@ericsson.com

Feature: Failed Notification Integrationtest

  Scenario:
    Send eiffel events for artifact flow and make sure email notification fails
    due to incorrect SMTP server port

    # Setup Eiffel Intelligence
    Given the rules "src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json"
    And the events "src/test/resources/ArtifactFlowTestEvents.json"

    # Setup subscription
    Given subscription object for "MAIL" with name "MailFailureTestSubscription" is created
    When notification meta "some.cool.email@ericsson.com,some.other.cool.email@ericsson.com" is set in subscription
    And parameter form key "" and form value "to_string(@)" is added in subscription
    And condition "testCaseExecutions[?testCaseTriggeredEventId =='ad3df0e0-404d-46ee-ab4f-3118457148f4']" at requirement index '0' is added in subscription
    Then subscription is uploaded

    # Send Events and Check job triggered
    Given all previous steps passed
    And the eiffel events are sent
    Then failed notification for "email" should exist for subscription "MailFailureTestSubscription"
