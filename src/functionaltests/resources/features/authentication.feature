#Author: valentin.tyhonov@ericsson.com
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
Feature: Test Authentication

  Scenario: Call an REST API without credentials
    Given LDAP is activated
    When make a POST request to the subscription REST API "/subscriptions" without credentials
    Then get response code of 401 and subscription with name "Subscription_Test" is not created

  Scenario: Call an REST API with credentials
    Given LDAP is activated
    When make a POST request to the subscription REST API "/subscriptions" with username "gauss" and password "password"
    Then get response code of 200 and subscription with name "Subscription_Test" is created
