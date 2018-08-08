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
@ScalingAndFailoverFeature
Feature: Scaling and Failover test

  @ScalingAndFailoverScenario
  Scenario: Scaling and Failover
    Given 3 additional instances of Eiffel Intelligence
    And instances are up and running
    When 1000 event messages are sent
    And additional instances are closed
    Then all event messages are processed