@Templates
Feature: Test get templates

  @GetTemplatesScenario
  Scenario: Test Template entry-points
    Given Eiffel Intelligence instance is up and running
    Then List available files
    And Get subscription template file
    And Get rules template file
    And Get event template file
