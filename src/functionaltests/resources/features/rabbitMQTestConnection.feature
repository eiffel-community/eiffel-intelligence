
Feature: RabbitMQConnection

  Scenario: Testing Connection to Message Bus
    Given We are connected to message bus
    When Message bus goes down
    And Message bus is restarted
    Then I send some events
    And Events are in waitlist
