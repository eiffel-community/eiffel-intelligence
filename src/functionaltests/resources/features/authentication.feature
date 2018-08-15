#Author: valentin.tyhonov@ericsson.com, christoffer.cortes.sjowall@ericsson.com
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

  @RESTWithoutCredentials
  Scenario: Call an REST API without credentials
    Given LDAP is activated
    When a POST request is prepared for REST API "/subscriptions"
    And request is sent
    Then response code 401 is received
    And subscription is not created

  @RESTWithCredentials
  Scenario: Call an REST API with credentials
    Given LDAP is activated
    When a POST request is prepared for REST API "/subscriptions"
    And username "gauss" and password "password" is used as credentials
    And request is sent
    Then response code 200 is received
    And subscription is created

  @RESTWithSessionCookie
  Scenario: Call an REST API with session credentials
    Given LDAP is activated
    When a GET request is prepared for REST API "/auth/login"
    And request is sent
    Then response code 401 is received
    When a GET request is prepared for REST API "/auth/login"
    And username "gauss" and password "password" is used as credentials
    And request is sent
    Then response code 200 is received
    When a GET request is prepared for REST API "/auth/login"
    And request is sent
    Then response code 200 is received

  @RESTWithTokenId
  Scenario: Call an REST API with session credentials
    Given LDAP is activated
    When a GET request is prepared for REST API "/auth/login"
    And request is sent
    Then response code 401 is received
    When a GET request is prepared for REST API "/auth/login"
    And username "gauss" and password "password" is used as credentials
    And request is sent
    Then response code 200 is received
    And authentication token is saved
    And client is replaced
    When a GET request is prepared for REST API "/auth/login"
    And authentication token is attached
    And request is sent
    Then response code 200 is received