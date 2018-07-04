#Author:
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
@Name
Feature: TestTTL

  @TestTTL
  Scenario: Test time to live for missed notification and aggregated object
    Given Missed notification is created in database with index "expTime"
    And Aggregated object is created in database with index "expTime"
    Then "aggregated_object" has been deleted from "eiffel_intelligence" database
    And "Notification" has been deleted from "MissedNotification" database

  @TestNotificationRetries
  Scenario: Test notification retries
    Given Subscription is created
    When I fail to inform subscriber
    Then Verify that request has been retried
    And Missed notification is in database
