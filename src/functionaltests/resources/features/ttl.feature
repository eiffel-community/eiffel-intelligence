#Author: emelie.pettersson@ericsson.com

@TtlAndNotifications
Feature: TestTTL

  @TestTTL
  Scenario: Test time to live for missed notification and aggregated object
    Given Missed notification is created in database with index "expTime"
    And Aggregated object is created in database with index "expTime"
    Then "aggregated_object" document has been deleted from "eiffel_intelligence" database
    And "Notification" document has been deleted from "MissedNotification" database

  @TestNotificationRetries
  Scenario: Test notification retries
    Given Subscription is created
    When I want to inform subscriber
    Then Verify that request has been retried
    And Check missed notification is in database
