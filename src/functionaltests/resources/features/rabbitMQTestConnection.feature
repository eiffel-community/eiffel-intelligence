
Feature: RabbitMQConnection

  Scenario: Testing to automatically reconnect to Message Bus
    Given We are connected to message bus
    When Message bus goes down
    And Message bus is restarted
    Then I can send events which are put in the waitlist
