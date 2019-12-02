# Subscription with REST POST notification

Valid subscriptions for REST POST notification are provided below with 
comments for each field.

_**OBS! Comments are only for documentation purposes and a subscription should
not contain them. The subscription will be rejected at this moment if it
contains comments like below.**_

**Subscription templates can be found [here](https://github.com/eiffel-community/eiffel-intelligence/tree/master/src/main/resources/templates).**

    {
        // The name of the subscription to make it easy to search for.
        // Only numbers, letters and underscore allowed.
        "subscriptionName" : "Subscription1",

        // The name of the logged in user creating or updating the subscription.
        // Added by Eiffel Intelligence if LDAP is enabled. Not required. 
        // Defaults to an empty string.
        "ldapUserName" : "ABC",

        // Instructs whether the same subscription should be re-triggered
        // for new additions to the aggregated object. If this is set to
        // false, only the first time the conditions are fulfilled, a
        // notification will be triggered. No matter how many times the
        // aggregated object is updated.
        "repeat" : false,

        // Creation time in system time, added by Eiffel Intelligence.
        "created" : 1542117412833,

        // How Eiffel Intelligence should notify when a subscription is fulfilled.
        "notificationType" : "REST_POST",

        // Which url to use for the HTTP POST request.
        // This field requires a schema to work. That means the 'http://' part needs to be included.
        "notificationMeta" : "http://myAwesomeService:8080/api/send",

        // If any authentication is needed by Eiffel Intelligence to send 
        // the notification HTTP request.
        "authenticationType" : "BASIC_AUTH",

        // The username and password Eiffel Intelligence will use in headers 
        // of the HTTP request when sending a notification via HTTP POST.
        "userName" : "functionalUser",
        "password" : "functionalUserPassword",
        
        // Headers for the HTTP request, can be 'application/x-www-form-urlencoded' or 'application/json'.
        "restPostBodyMediaType" : "application/json",

        // The data to send in the HTTP POST request body.
        "notificationMessageKeyValues" : [
            {
                // When using 'application/json' the form key should be   
                // left empty.
                // The form value will be run through JMESPath engine so
                // it is possible to use JMESPath expressions to extract
                // content from the aggregated object. The form value can
                // only be one JSON object.

                "formkey" : "",
                "formvalue" : "{parameter: [{ name: 'jsonparams', value : to_string(@) }]}"
            }
        ],

        // An array of one or several requirements. At least one requirement 
        // should be fulfilled to trigger this subscription. A requirement 
        // can have several conditions.
        "requirements" : [
            {
                // Array of conditions. The key in the condition object must 
                // be "jmespath". The value can be any JMESPath expression to 
                // extract data from the aggregated object. 
                // All conditions needs to be fulfilled in order for
                // a requirement to be fulfilled.

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


    {
        "subscriptionName": "Subscription1",
        "repeat": false,
        "notificationMeta": "http://127.0.0.1:3000/ei/test_subscription_rest",
        "notificationType": "REST_POST",
        "restPostBodyMediaType": "application/x-www-form-urlencoded",
        "notificationMessageKeyValues": [
            // With 'application/x-www-form-urlencoded' for REST_POST it is
            // possible to have multiple key/value pairs in the notification message.
            {
                // The form key should match the external API to which 
                // Eiffel Intelligence will send the notification of a 
                // fulfilled subscription.
                // The form value will be run through JMESPath engine so
                // it is possible to use JMESPath expressions to extract
                // content from the aggregated object. The form value can
                // only be one JSON object.

                "formkey": "external-api-parameter-name",
                "formvalue": "{parameters: [{ name: 'full_aggregation', value : to_string(@) }, { name: 'artifact_identity', value : '@.identity' }]}"
            },
            {
                "formkey": "another-parameter",
                "formvalue": "@.flowContexts[?product == 'myAwesomeProduct']"
            }
        ],
        "requirements": [
        
            // This subscription contains 2 requirements. To trigger a notification
            // an aggregation should contain a successful test case execution OR 
            // an artifact with a given identity AND a specific flowContext 
            // should be found in the aggregation.
        
            {
                "conditions": [
                    {
                        // Array of conditions. The key in the condition object must 
                        // be "jmespath". The value can be any JMESPath expression to 
                        // extract data from the aggregated object. 
                        // All conditions needs to be fulfilled in order for
                        // a requirement to be fulfilled.
                
                        // Notice the single quotes surrounding the value to be checked.
                        // This tells jmespath that this is a constant and not
                        // an object from the aggregated object.
                
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

The below subscription will trigger a REST POST CALL when an artifact for 
issue JIRA-1234 has passed testcase TC5 successfully.


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
                // This subscription will notify when an artifact for given 
                // issue id, has passed a certain test successfully.
                // The method 'incomplete_path_contains()' is predefined.
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

Read more on how Eiffel Intelligence groups [requirements and conditions in subscriptions](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/subscriptions.md#writing-requirements-and-conditions).

