# Author: valentin.tyhonov@ericsson.com, christoffer.cortes.sjowall@ericsson.com

@Authentication
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
    When a GET request is prepared for REST API "/auth/logout"  
    And request is sent
    When a GET request is prepared for REST API "/auth/login"
    And request is sent
    Then response code 401 is received
    When a GET request is prepared for REST API "/auth/login"
    And username "gauss" and password "password" is used as credentials
    And request is sent
    Then response code 200 is received
    And authentication token is saved    
    When a GET request is prepared for REST API "/auth/login"
    And client is replaced
    And authentication token is attached
    And request is sent
    Then response code 200 is received