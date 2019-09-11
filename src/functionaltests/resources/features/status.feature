
@Status
Feature: Test Status

  @TestStatusIsOk
  Scenario: Test Status Is AVAILABLE
    Given "None" services is unavailable
    When I fetches status
    Then Verify "eiffelIntelligenceStatus" status is "AVAILABLE"
    And Verify "rabbitMQStatus" status is "AVAILABLE"
    And Verify "mongoDBStatus" status is "AVAILABLE"
    
  @TestRMQStatusIsUnavailable
  Scenario: Test RMQ Status Is UNAVAILABLE
    Given "rabbitMQStatus" services is unavailable
    When I fetches status
    Then Verify "eiffelIntelligenceStatus" status is "UNAVAILABLE"
    And Verify "rabbitMQStatus" status is "UNAVAILABLE"
    And Verify "mongoDBStatus" status is "AVAILABLE"
