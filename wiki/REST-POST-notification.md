# REST POST Notification 

A valid subscription for REST POST notification is provided below with comments for each field. 

_**OBS! Comments are only for documentation purposes and a subscription should not contain them. The subscription will be rejected at this moment if it contains comments.**_

_**Subscription templates can be found [here](https://github.com/Ericsson/eiffel-intelligence/tree/master/src/main/resources/templates).**_ 

```javascript
{
    "subscriptionName": "Subscription_Rest_Params_in_Url",
    "userName": "DEF",
    "repeat": false,
    "created": "data-time",
    "notificationType": "REST_POST",
    "notificationMeta": "http://${rest.host}:${rest.port}${rest.endpoint.params}?parameter1=testCaseExecutions[0].outcome.id&parameter2=testCaseExecutions[0].outcome.conclusion",
    "restPostBodyMediaType": "application/x-www-form-urlencoded",
    "requirements": [
      {
        "type": "ARTIFACT_1",
        "conditions": [
          {"jmespath": "gav.groupId=='com.mycompany.myproduct'"},
          {"jmespath": "testCaseExecutions[?outcome.conclusion == 'SUCCESSFUL' && outcome.id=='TC5']"}
        ]
      },
      {
        "type": "ARTIFACT_1",
        "conditions": [
          {"jmespath": "gav.groupId=='com.mycompany.myproduct'"},
          {"jmespath": "testCaseExecutions[?testCaseStartedEventId == 'cb9d64b0-a6e9-4419-8b5d-a650c27c59ca']"}
        ]
      }
    ]
  }
```
