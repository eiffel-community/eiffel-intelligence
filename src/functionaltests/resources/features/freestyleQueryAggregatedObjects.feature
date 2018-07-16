
@tag
Feature: FreestyleQueryAggregatedObjectsTestSteps

  Scenario: Test FreestyleQueryAggregatedObjectsTestSteps
    Given Aggregated object2 is created
    And Missed Notification2 object is created
    Then Perform valid freestyle query on newly created Aggregated object
    And Perform an invalid freesyle query on same Aggregated object
    And Perform a query2 for missed notification
    And Check missed2 notification has been returned
    