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

  #@TestAggregatedObject
  #Scenario: Test time to live for aggregated object
  #  Given "src/functionaltests/resources/AggregatedObject.json" is prepared with index "expTime"
  #  And "aggregated_object" is created in database "eiffel_intelligence" with index "expTime"
  #  When I sleep for "10" seconds
  #  Then "aggregated_objects" has been deleted from "eiffel_intelligence" database

  #@TestMissedNotification
  #Scenario: Test time to live for missed notifications
  #  Given "src/functionaltests/resources/MissedNotification.json" is prepared with index "expTime"
  #  And "Notification" is created in database "MissedNotification" with index "expTime"
  #  When I sleep for "60" seconds
  #  Then "Notification" has been deleted from "MissedNotification" database
  #  And Verify that request has been made several times

  @TestSubscription
  Scenario: Test with subscription
    Given Subscription is created
    #And "Notification" is created in database "MissedNotification" with index "expTime"
    #When I sleep for "60" seconds
    #And Notification is triggered
    #Then "Notification" has been deleted from "MissedNotification" database
    Then Verify that request has been made several times
