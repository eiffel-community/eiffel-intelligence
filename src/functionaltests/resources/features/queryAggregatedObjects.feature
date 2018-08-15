
@tag
Feature: QueryAggregatedObjectsTestSteps

  Scenario: Test QueryAggregatedObjectsTestSteps
    Given Aggregated object is created
    And Missed Notification object is created
    Then Perform valid query on created Aggregated object
    And Perform an invalid query on same Aggregated object
    And Perform several valid freestyle queries on created Aggregated objects
    And Perform an invalid freesyle query on Aggregated object
    And Perform a query for missed notification
    And Check missed notification has been returned
