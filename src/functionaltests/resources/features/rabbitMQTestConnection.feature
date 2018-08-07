
Feature: RabbitMQConnection

  Scenario: Testing Connection to Message Bus
    Given Eiffel Intelligence is up and running
    When Message bus goes down
    And Message bus is restarted
    Then I send some events
    And Events are received
