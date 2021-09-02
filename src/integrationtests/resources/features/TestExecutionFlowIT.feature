#Author: erik.edling@ericsson.com

Feature: Test execution flow integrationtest

  Scenario: Send eiffel events for test execution flow and make sure EI is triggering on
  REST-POST subscriptions using buildWithParameters and JMESPATH extracted correct
  data to add to parameter

    # Setup Eiffel Intelligence
    Given the rules "src/main/resources/rules/TestExecutionObjectRules-Eiffel-Agen-Version.json"
    And the events "src/test/resources/TestExecutionTestEvents.json"
    And the resulting aggregated object "src/test/resources/aggregatedTestActivityObject.json";
    Then the expected aggregated object ID is "e46ef12d-25gb-4d7y-b9fd-8763re66de47"

    # Setup jenkins
    Given jenkins data is prepared
    When job token "test-token-123" is added to jenkins data
    And parameter key "test_key" is added to jenkins data
    And bash script "echo 'test 123'" is added to jenkins data
    Then jenkins is set up with job name "testExecutionTestJobParam"

    # Setup subscription
    Given subscription object of type "REST/POST" with name "ParameterizedTriggerSubscription" is created
    When notification meta "http://${jenkinsHost}:${jenkinsPort}/job/${jenkinsJobName}/buildWithParameters?token='test-token-123'&test_key=activity_triggered_event_id" is set in subscription
    And "BASIC_AUTH_JENKINS_CSRF" authentication with username "admin" and password "admin" is set in subscription
    And rest post body media type is set to "application/x-www-form-urlencoded" is set in subscription
    And condition "activity_triggered_event_id=='e46ef12d-25gb-4d7y-b9fd-8763re66de47'" at requirement index '0' is added in subscription
    Then subscription is uploaded

    # Send Events and Check job triggered
    When the eiffel events are sent
    And jenkins job status data fetched
    Then verify jenkins job data timestamp is after test subscription was created
    And jenkins job status data has key "test_key" with value "e46ef12d-25gb-4d7y-b9fd-8763re66de47"
    And the jenkins job should be deleted

  Scenario: Send eiffel events for test execution flow and make sure EI is triggering on
  REST-POST subscriptions using build and JMESPATH extracted correct
  data to add as a json parameter in the body. This with multiple parameters.

    # Setup Eiffel Intelligence
    Given the rules "src/main/resources/rules/TestExecutionObjectRules-Eiffel-Agen-Version.json"
    And the events "src/test/resources/TestExecutionTestEvents.json"
    And the resulting aggregated object "src/test/resources/aggregatedTestActivityObject.json";
    Then the expected aggregated object ID is "e46ef12d-25gb-4d7y-b9fd-8763re66de47"

    # Setup jenkins
    Given jenkins data is prepared
    When job token "test-token-123" is added to jenkins data
    And parameter key "test_param_1" is added to jenkins data
    And parameter key "test_param_2" is added to jenkins data
    And bash script "echo 'test 123'" is added to jenkins data
    Then jenkins is set up with job name "testExecutionTestJobBodyJson"

    # Setup subscription
    Given subscription object of type "REST/POST" with name "ParameterInBodyTriggerSubscription" is created
    When notification meta "http://${jenkinsHost}:${jenkinsPort}/job/${jenkinsJobName}/build?token='test-token-123'" is set in subscription
    And "BASIC_AUTH_JENKINS_CSRF" authentication with username "admin" and password "admin" is set in subscription
    And rest post body media type is set to "application/x-www-form-urlencoded" is set in subscription
    And parameter form key "json" and form value "{parameter: [{name:'test_param_1', value:'test_value'}, {name:'test_param_2', value:activity_triggered_event_id}]}" is added in subscription
    And condition "activity_triggered_event_id=='e46ef12d-25gb-4d7y-b9fd-8763re66de47'" at requirement index '0' is added in subscription
    Then subscription is uploaded

    # Send Events and Check job triggered
    When the eiffel events are sent
    And jenkins job status data fetched
    Then verify jenkins job data timestamp is after test subscription was created
    And jenkins job status data has key "test_param_1" with value "test_value"
    And jenkins job status data has key "test_param_2" with value "e46ef12d-25gb-4d7y-b9fd-8763re66de47"
    Then the jenkins job should be deleted
