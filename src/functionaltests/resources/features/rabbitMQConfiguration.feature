@RabbitMQConfiguration
Feature: Test Rabbit MQ Configuration

  @RabbitMQConfigurationMultipleRoutingKeysScenario
  Scenario: Test that aggregations are done when events are sent with different routing keys
    Given We are connected to message bus
    When events are published using different routing keys
    Then an aggregated object should be created

  @RabbitMQConfigurationDeleteBindingKeysScenario
  Scenario: Test that old binding keys are deleted from rabbitMQ and mongoDB
    Given We are connected to message bus
    Then get the binding documents from mongoDB
    Then compare the binding keys and remove the old binding keys from rabbitMQ and mongoDB
    Then insert the new binding keys into mongoDB document