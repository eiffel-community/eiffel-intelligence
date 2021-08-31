#Author: christoffer.cortes.sjowall@ericsson.com

Feature: Failed Notification Integrationtest

  Scenario: Start an artifact flow aggregation and make sure the email notification fails
  due to incorrect SMTP server port. The port is set with @TestPropertySource
  in the steps file.

    # Setup Eiffel Intelligence
    Given the rules "src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json"
    And the events "src/test/resources/ArtifactFlowTestEvents.json"

    # Setup subscription
    Given subscription object of type "MAIL" with name "MailFailureTestSubscription" is created
    When notification meta "some.cool.email@ericsson.com,some.other.cool.email@ericsson.com" is set in subscription
    And parameter form key "" and form value "to_string(@)" is added in subscription
    And condition "testCaseExecutions[?testCaseTriggeredEventId =='ad3df0e0-404d-46ee-ab4f-3118457148f4']" at requirement index '0' is added in subscription
    Then subscription is uploaded

    # Send Events and Check job triggered
    #When the eiffel events are sent
    Then failed notification of type "MAIL" should exist for subscription "MailFailureTestSubscription"

  Scenario: Start an artifact flow aggregation and make sure the REST notification fails
  due to incorrect URL.

    # Setup Eiffel Intelligence
    Given the rules "src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json"
    And the events "src/test/resources/ArtifactFlowTestEvents.json"

    # Setup subscription
    Given subscription object of type "REST/POST" with name "RestFailureTestSubscription" is created
    When notification meta "http://localhost:9999/some-endpoint" is set in subscription
    And rest post body media type is set to "application/x-www-form-urlencoded" is set in subscription
    And condition "testCaseExecutions[?testCaseTriggeredEventId =='ad3df0e0-404d-46ee-ab4f-3118457148f4']" at requirement index '0' is added in subscription
    Then subscription is uploaded

    # Send Events and Check job triggered
    #When the eiffel events are sent
    Then failed notification of type "REST/POST" should exist for subscription "RestFailureTestSubscription"
