
@tag
Feature: FreeStyleQueryTests

	Scenario: Test FreeStyleQuery
  	Given Aggregated object is created
    And Missed Notification object is created
    Then Perform valid query on newly created Aggregated object
    And Perform an invalid query on same Aggregated object
    And Perform and valid query on Missed Notification object
    And Perform same query one more time
    