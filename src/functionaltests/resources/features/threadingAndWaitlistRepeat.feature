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

  @ThreadingAndWaitlistRepeatScenario
  Scenario: Test multithreaded events processing, waitlist resend and waitlist TTL.
  	Given that eiffel events are sent  	
	Then waitlist should not be empty
	And no event is aggregated
	And the waitlist will try to resend the events at given time interval
	And correct amount of threads should be spawned
	And after the time to live has ended, the waitlist should be empty
