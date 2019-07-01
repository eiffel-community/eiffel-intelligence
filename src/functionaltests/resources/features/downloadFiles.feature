@DownloadFiles
Feature: Test Download Files

  @DownloadFilesScenario
  Scenario: Test DownloadFiles Entrypoints
    Given Eiffel Intelligence instance is up and running
    Then List available files
    And Get subscription template file
    And Get rules template file
    And Get event template file
