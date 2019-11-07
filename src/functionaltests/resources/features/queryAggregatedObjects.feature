@QueryAggregatedObjects
Feature: Test Query Aggregated Objects

  @QueryAggregatedObjectsTestSteps
  Scenario: Test QueryAggregatedObjectsTestSteps
    Given Aggregated object is created
    And Failed Notification object is created
    Then Perform valid query on created Aggregated object
    And Perform an invalid query on same Aggregated object
    And Perform several valid freestyle queries on created Aggregated objects
    And Perform an invalid freestyle query on Aggregated object
    And Perform a query for failed notification
    And Perform a query on created Aggregated object with filter
    And Perform a query and filter with part of path

  @QueryConfidenceLevelModified
  Scenario: Test QueryConfidenceLevelModified
    Given Aggregated object is created
    Then perform query to retrieve and filter out confidence level information

