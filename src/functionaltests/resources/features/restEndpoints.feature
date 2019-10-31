@RestEndpoints
Feature: Test Rest Endpoints

# Note: /query is not tested since it has its own test.
# /rules/rule-check/aggregation is not tested since it performs an aggregation.

  Scenario Outline: Perform GET request on <endpoint> and expect response code <responsecode>
    Given A GET request is prepared
    When Perform request on endpoint "<endpoint>"
    Then Request should get response code <responsecode>

  # Note: 404 responses are when no data in database was found,
  # since this scenario is not about fetching data.
  Examples:
    | responsecode | endpoint                               |
    | 200          | /information                           |
    | 200          | /authentication                        |
    | 200          | /authentication/login                  |
    | 200          | /status                                |
    | 200          | /templates                             |
    | 200          | /templates/events                      |
    | 200          | /templates/rules                       |
    | 200          | /templates/subscriptions               |
    | 404          | /queryMissedNotifications/subs_name    |
    | 404          | /queryAggregatedObject/id              |
    | 200          | /rules                                 |
    | 200          | /rules/rule-check/testRulePageEnabled  |

  @Test_Post_Put_Get_Delete_and_Get_(not_found)_subscription
  Scenario Outline: Perform <type> request on <endpoint> and expect response code <responsecode>
    Given A <type> request is prepared
    Given "<add_sub>" add subscription with name "test_subscription" to the request body
    When Perform request on endpoint "<endpoint>"
    Then Request should get response code <responsecode>

  Examples:
    | responsecode | type   | add_sub | endpoint                         |
    | 200          | POST   | do      | /subscriptions                   |
    | 200          | PUT    | do      | /subscriptions                   |
    | 200          | GET    | do not  | /subscriptions/test_subscription |
    | 200          | DELETE | do not  | /subscriptions/test_subscription |
    | 404          | GET    | do not  | /subscriptions/test_subscription |

  Scenario: Test POST on /rules/rule-check endpoint
    Given A POST request is prepared
    And Event rule json data is added as body
    When Perform request on endpoint "/rules/rule-check"
    Then Request should get response code 200
