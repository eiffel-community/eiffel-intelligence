# Subscription with REST POST notification

Valid subscriptions for REST POST notifications are provided below with 
each field explained.

**Subscription templates can be found [here](../src/main/resources/templates).**

## subscriptionName
The name of the subscription to make it easy to search for. Only numbers, letters and underscore allowed.

## ldapUserName
The name of the logged in user creating or updating the subscription. Added 
by Eiffel Intelligence if LDAP is enabled. Not required. Defaults to an empty string.

## repeat
Instructs whether the same subscription should be re-triggered for new 
additions to the aggregated object. If this is set to false, only the first 
time the conditions are fulfilled, a notification will be triggered. No 
matter how many times the aggregated object is updated.

## created
Creation time in system time, added by Eiffel Intelligence.

## notificationType
How Eiffel Intelligence should notify when a subscription is fulfilled.

## notificationMeta
Which url to use for the HTTP POST request. This field requires a schema 
to work. That means the 'http://' part of a url needs to be included.

## restPostBodyMediaType
Which headers to use for the HTTP request, they can be 
'application/x-www-form-urlencoded' or 'application/json'.

## notificationMessageKeyValues
The data to send in the HTTP POST request body. When using 'application/json' 
the form key should be left empty. 

With 'application/x-www-form-urlencoded' for REST_POST it is possible to have 
multiple key/value pairs in the notification message. The form key should 
match the external API to which Eiffel Intelligence will send the notification 
of a fulfilled subscription.

The form value will be run through JMESPath engine so it is possible to use 
JMESPath expressions to extract content from the aggregated object. The 
form value can only be one JSON object.

## authenticationType
If any authentication is needed by Eiffel Intelligence to send the notification HTTP request.

## userName & password
The username and password Eiffel Intelligence will use in headers of the 
HTTP request Eiffel Intelligence sends a notification via HTTP POST.

## requirements
An array of one or several requirements. At least one requirement should be 
fulfilled to trigger this subscription. A requirement can have several conditions.

## conditions
Array of conditions. The key in the condition object must be "jmespath". 
The value can be any JMESPath expression to extract data from the aggregated object. 
All conditions needs to be fulfilled in order for a requirement to be fulfilled.

Notice the single quotes surrounding the value to be checked. This tells 
JMESPath that this is a constant and not an object from the aggregated object.

    {
        "subscriptionName" : "Subscription1",
        "ldapUserName" : "ABC",
        "repeat" : false,
        "created" : 1542117412833,

        "notificationType" : "REST_POST",
        "notificationMeta" : "http://myAwesomeService:8080/api/send",

        "restPostBodyMediaType" : "application/json",
        "notificationMessageKeyValues" : [
            {
                "formkey" : "",
                "formvalue" : "{parameter: [{ name: 'jsonparams', value : to_string(@) }]}"
            }
        ],
        
        "authenticationType" : "BASIC_AUTH",
        "userName" : "functionalUser",
        "password" : "functionalUserPassword",
        
        "requirements" : [
            {
                "conditions" : [
                    {
                        "jmespath" : "identity=='pkg:maven/com.othercompany.library/artifact-name@1.0.0'"
                    },
                    {
                        "jmespath" : "flowContexts[?product == 'myProduct' && track == 'myTrack']"
                    }
                ]
            }

        ]
    }

Additional examples of how to write subscription requirements and conditions:

The below subscription contains 2 requirements. To trigger a notification an 
aggregation should contain a successful test case execution OR an artifact 
with a given identity AND a specific flowContext should be found in the aggregation.


    {
        "subscriptionName": "Subscription1",
        "repeat": false,

        "notificationMeta": "http://127.0.0.1:3000/ei/test_subscription_rest",
        "notificationType": "REST_POST",
        "restPostBodyMediaType": "application/x-www-form-urlencoded",
        "notificationMessageKeyValues": [
            {
                "formkey": "external-api-parameter-name",
                "formvalue": "{parameters: [{ name: 'full_aggregation', value : to_string(@) }, { name: 'artifact_identity', value : '@.identity' }]}"
            },
            {
                "formkey": "another-parameter",
                "formvalue": "@.flowContexts[?product == 'myAwesomeProduct']"
            }
        ],
        "requirements": [
            {
                "conditions": [
                    {
                        "jmespath": "testCaseExecutions[?testCase.conclusion == 'SUCCESSFUL' && testCase.id=='TC5']"
                    }
                ]
            },
            {
                "conditions": [
                    {
                        "jmespath": "identity=='pkg:maven/com.mycompany.myproduct/artifact-name@1.0.0'"
                    },
                    {
                        "jmespath": "flowContexts[?product == 'my/product/flow']"
                    }
                ]
            }
        ]
    }

The below subscription will perform a REST POST notification when an artifact 
for issue JIRA-1234 has passed testcase TC5 successfully. The method 
'incomplete_path_contains()' used in the condition is predefined.

    {
        "subscriptionName": "artifactRequirementSubscription",
        "notificationMeta": "http://127.0.0.1:3000/ei/test_subscription_rest",
        "notificationType": "REST_POST",
        "restPostBodyMediaType": "application/json",
        "notificationMessageKeyValues": [
            {
                "formkey": "",
                "formvalue": "@"
            }
        ],
        "repeat": false,
        "requirements": [
            {
                "conditions": [
                    {
                        "jmespath": "incomplete_path_contains(@, 'issues.id','JIRA-1234')"
                    },
                    {
                        "jmespath": "(length(testCaseExecutions[?(outcome.id == 'TC5' && outcome.conclusion == 'SUCCESSFUL')]) > `0`)"
                    }
                ]
            }
        ]
    }

## Requirements and conditions

Read more on how Eiffel Intelligence groups [requirements and conditions in subscriptions](subscriptions.md#writing-requirements-and-conditions).
