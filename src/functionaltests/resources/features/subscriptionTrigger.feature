#Author: your.email@your.domain.com
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
@SubscriptionTriggerFeature
Feature: Subscription trigger test
  
  @SubscriptionTriggerScenario
  Scenario: Test subscription triggering
  	Given The REST API "/subscriptions" is up and running
    And Subscriptions are setup using REST API "/subscriptions"
    When I send Eiffel events
    And Wait for EI to aggregate objects and trigger subscriptions
    Then Mail subscriptions were triggered 
    And Rest subscriptions were triggered
