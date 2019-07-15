@SubscriptionContent
Feature: Test Subscription Content

  @ValidSubscription
  Scenario: Test creating valid subscriptions
  Given No subscriptions exist
  When I create subscription request with "src/functionaltests/resources/ValidSubscription.json"
  Then The subscription is created successfully
  And Valid subscription "mySubscription" exists


  @DuplicateSubscription
  Scenario: Test duplicate subscriptions are rejected
  Given Subscription "mySubscription" already exists
  When I create a duplicate subscription with "src/functionaltests/resources/ValidSubscription.json"
  Then Duplicate subscription is rejected
  And "mySubscription" is not duplicated


  @InvalidSubscription
  Scenario: Test invalid subscriptions are rejected
  Given I delete "mySubscription"
  And Subscriptions does not exist
  When I create an invalid subscription with "src/functionaltests/resources/InvalidSubscription.json"
  Then The invalid subscription is rejected
  And The invalid subscription does not exist

  @SubscriptionWithMissingFileds
  Scenario: Test creating subscription with missing required fields in Subscription Schema
  Given No subscriptions exist
  When I try to create subscription request with missing required fields"src/functionaltests/resources/missingFieldsSubscription.json"
  Then The subscription with missing field is rejected


