
@tag
Feature: FreestyleQueryAggregatedObjectsTestSteps

  Scenario: Test FreestyleQueryAggregatedObjectsTestSteps
    Given Aggregated object2 is created
    Then Perform valid freestyle query on newly created Aggregated object
    And Perform an invalid freesyle query on Aggregated object
    