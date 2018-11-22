# Email Notification

A valid subscription for email notification is provided below with comments for each field. 

_**OBS! Comments are only for documentation purposes and a subscription should not contain them. The subscription will be rejected at this moment if it contains comments.**_

_**Subscription templates can be found [here](https://github.com/Ericsson/eiffel-intelligence/tree/master/src/main/resources/templates).**_ 

```javascript
{
  // the name of the subscription to make it easy to search for it
  // Only numbers,letters and underscore allowed
  "subscriptionName": "Subscription_1",
  // the name of the user creating or updating the subscription
  // this will be overriden by signum if ldap authorization is enabled
  "userName" : "DEF",
  // instructs whether same subscription should be re-triggered for new additions
  // to the aggregated object. If false only first time the conditions are fulfilled 
  // a notification will be triggered. No matter how many times the aggregated object
  // is updated.
  "repeat": false,
  // creation time in system time. added by Eiffel Intelligence
  "created": 1523473956269,
  // how to notify the user
  "notificationType": "MAIL",
  // email adress
  "notificationMeta": "example@ericsson.com",
  // used only for REST POST notifications
  "restPostBodyMediaType": "application/json",
  // content of the email message. 
  "notificationMessageKeyValues": [
    {
      // form key should always be an empty string for email messages.
      "formkey": "",
     // form value will be run through jmespath engine to extract 
     // content from aggregated object. Here the whole aggregated object
     // is passed.
      "formvalue": "@"
    }
  ],
  // an array of requirements. At least one requirement should be fulfilled to
  // trigger this subscription. 
  "requirements": [
    {
      // just informative. subscription is not dependent on this to work
      "type": "ARTIFACT_1",
      // Array of conditions. Here we use JMESPATH condition based on content in
      // aggregated object. All conditions needs to be fulfilled in order for
      // a requirement to be fulfilled.
      "conditions": [
        {
          "jmespath": "gav.groupId=='com.mycompany.myproduct'"
        },
        {
          "jmespath": "testCaseExecutions[?testCase.conclusion == 'SUCCESSFUL' && testCase.id=='TC5']"
        }
      ]
    },
    {
      // see above
      "type": "ARTIFACT_1",
      // see above
      "conditions": [
        {
          "jmespath": "gav.groupId=='com.mycompany.myproduct'"
        },
        {
          "jmespath": "testCaseExecutions[?testCaseStartedEventId == '13af4a14-f951-4346-a1ba-624c79f10e98']"
        }
      ]
    }
  ]
}
```
