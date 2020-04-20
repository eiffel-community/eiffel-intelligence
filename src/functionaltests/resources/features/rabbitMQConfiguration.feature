@RabbitMQConfiguration
Feature: Test Rabbit MQ Configuration

  @RabbitMQConfigurationMultipleRoutingKeysScenario
  Scenario: Test that aggregations are done when events are sent with different routing keys
    Given We are connected to message bus
    When events are published using different routing keys
    Then an aggregated object should be created