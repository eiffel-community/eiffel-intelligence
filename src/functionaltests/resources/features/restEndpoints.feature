@RestEndpoints
Feature: Test Rest Endpoints

  @RestEndpointsTest
  Scenario: Test get information
    Given A GET request is prepared
    When Perform request on endpoint "/information"
    Then Request should get response code 200

  Scenario: Test get subscriptions
    Given A GET request is prepared
    When Perform request on endpoint "/subscriptions"
    Then Request should get response code 200