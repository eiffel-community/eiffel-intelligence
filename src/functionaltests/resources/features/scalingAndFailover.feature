#Author: christoffer.cortes.sjowall@ericsson.com

@ScalingAndFailover
Feature: Test Scaling and Failover

  @ScalingAndFailoverScenario
  Scenario: Scaling and Failover
    Given 3 additional instances of Eiffel Intelligence
    And instances are up and running
    When 1000 event messages are sent
    And additional instances are closed
    Then all event messages are processed