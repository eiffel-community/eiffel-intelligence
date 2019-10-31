# Author: christoffer.cortes.sjowall@ericsson.com

@AuthenticationMultiLDAP
Feature: Test Authentication with multiple LDAP servers

  @RESTWithUniqueUsersInDifferentLDAPServers
  Scenario: Login using unique users from two different LDAP servers
    Given LDAP is activated
    When a GET request is prepared for REST API "/authentication/login"
    And username "gauss" and password "password" is used as credentials
    And request is sent
    Then response code 200 is received
    When a GET request is prepared for REST API "/authentication/logout"
    And request is sent
    When a GET request is prepared for REST API "/authentication/login"
    And username "einstein" and password "e=mc2" is used as credentials
    And request is sent
    Then response code 200 is received

  @RESTWithIdenticalUsernamesInDifferentLDAPServers
  Scenario: Login using identical usernames with different passwords from two different LDAP servers
    Given LDAP is activated
    When a GET request is prepared for REST API "/authentication/login"
    And username "newton" and password "password" is used as credentials
    And request is sent
    Then response code 200 is received
    When a GET request is prepared for REST API "/authentication/logout"
    And request is sent
    When a GET request is prepared for REST API "/authentication/login"
    And username "newton" and password "password2" is used as credentials
    And request is sent
    Then response code 200 is received