@Encryption
Feature: Test Encryption on subscription passwords

  @EncryptionScenario
  Scenario: Encryption and decryption of subscription password
    When a subscription is created
    Then the password should be encrypted in the database
    When eiffel events are sent
    Then the subscription should trigger
    And the notification should be sent with a decrypted password in base64