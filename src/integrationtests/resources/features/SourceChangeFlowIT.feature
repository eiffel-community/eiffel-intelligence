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

  Scenario: 
    Send eiffel events for source change flow and make sure EI is triggering on 
    REST-POST subscriptions using buildWithParameters and JMESPATH extracted correct 
    data to add to parameter

    Given the rules "src/main/resources/rules/SourceChangeObjectRules-Eiffel-Agen-Version.json"
    And the events "src/test/resources/TestSourceChangeObjectEvents.json"
    And the upstream input "src/test/resources/UpstreamEventsForSourceChange.json"
    And the resulting aggregated object "src/test/resources/aggregatedSourceChangeObject.json";
    And the expected aggregated object ID is "sb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47"
    # Setup jenkins
    Given jenkins data is prepared
    When job token "test-token-123" is added to jenkins data
    And parameter key "test_key" is added to jenkins data
    And bash script "echo 'param 1 ' $test_key" is added to jenkins data
    Then jenkins is set up with job name "sourceChangeTestJobParam"
    # Setup subscription
    Given subscription object for "REST/POST" with name "ParameterizedTriggerSubscription" is created
    When notification meta "http://${jenkinsHost}:${jenkinsPort}/job/${jenkinsJobName}/buildWithParameters?token='test-token-123'&test_key=id" is set in subscription
    And basic_auth authentication with username "admin" and password "admin" is set in subscription
    And rest post body media type is set to "application/x-www-form-urlencoded" is set in subscription
    And condition "id=='sb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47'" at requirement index '0' is added in subscription
    Then subscription is uploaded
    # Send Events and Check job triggered
    Given all previous steps passed
    When the upstream input events are sent
    When the eiffel events are sent
    And jenkins job status data fetched
    Then verify jenkins job data timestamp is after test subscription was creted
    And jenkins job status data has key "test_key" with value "sb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47"
    And the jenkins job should be deleted

  Scenario: 
    Send eiffel events for source change flow and make sure EI is triggering on 
    REST-POST subscriptions using buildWithParameters and JMESPATH extracted correct 
    data to add to parameter

    Given the rules "src/main/resources/rules/SourceChangeObjectRules-Eiffel-Agen-Version.json"
    And the events "src/test/resources/TestSourceChangeObjectEvents.json"
    And the upstream input "src/test/resources/UpstreamEventsForSourceChange.json"
    And the resulting aggregated object "src/test/resources/aggregatedSourceChangeObject.json";
    And the expected aggregated object ID is "sb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47"
    # Setup jenkins
    Given jenkins data is prepared
    When job token "test-token-123" is added to jenkins data
    And parameter key "test_param_1" is added to jenkins data
    And parameter key "test_param_2" is added to jenkins data
    And bash script "echo 'param 1 ' $test_param_1" is added to jenkins data
    And bash script "echo 'param 2 ' $test_param_2" is added to jenkins data
    Then jenkins is set up with job name "sourceChangeTestJobBodyJson"
    # Setup subscription
    Given subscription object for "REST/POST" with name "ParameterInBodyTriggerSubscription" is created
    When notification meta "http://${jenkinsHost}:${jenkinsPort}/job/${jenkinsJobName}/build?token='test-token-123'" is set in subscription
    And basic_auth authentication with username "admin" and password "admin" is set in subscription
    And rest post body media type is set to "application/x-www-form-urlencoded" is set in subscription
    And paremeter form key "json" and form value "{parameter: [{name:'test_param_1', value:'Test Input Value'}, {name:'test_param_2', value:id}]}" is added in subscription
    And condition "id=='sb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47'" at requirement index '0' is added in subscription
    Then subscription is uploaded
    # Send Events and Check job triggered
    Given all previous steps passed
    When the upstream input events are sent
    When the eiffel events are sent
    And jenkins job status data fetched
    Then verify jenkins job data timestamp is after test subscription was creted
    And jenkins job status data has key "test_param_1" with value "Test Input Value"
    And jenkins job status data has key "test_param_2" with value "sb6efi4n-25fb-4d77-b9fd-5f2xrrefe66de47"
    Then the jenkins job should be deleted
