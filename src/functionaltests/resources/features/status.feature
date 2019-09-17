@Status
Feature: Test Status

  @TestStatusIsOk
  Scenario: Test Status Is AVAILABLE
    Given "None" service is unavailable
    When I get status
    Then Verify "eiffelIntelligenceStatus" status is "AVAILABLE"
    And Verify "rabbitMQStatus" status is "AVAILABLE"
    And Verify "mongoDBStatus" status is "AVAILABLE"
    
  @TestRabbitMQStatusIsUnavailable
  Scenario: Test RabbitMQ Status Is UNAVAILABLE
    Given "rabbitMQStatus" service is unavailable
    When I get status
    Then Verify "eiffelIntelligenceStatus" status is "UNAVAILABLE"
    And Verify "rabbitMQStatus" status is "UNAVAILABLE"
    And Verify "mongoDBStatus" status is "AVAILABLE"
