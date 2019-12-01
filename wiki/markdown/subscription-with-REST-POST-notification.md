# Subscription with REST POST notification

A valid subscription for REST POST notification is provided below with comments
for each field.

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
                // The form key should match the external API to which 
                // Eiffel Intelligence will send the notification of a 
                // fulfilled subscription.
                // The form value will be run through JMESPATH engine so
                // it is possible to use JMESPATH expressions to extract
                // content from the aggregated object.

                "formkey" : "json",
                "formvalue" : "{parameter: [{ name: 'jsonparams', value : to_string(@) }]}"
            }
        ],

        // An array of one or several requirements. At least one requirement 
        // should be fulfilled to trigger this subscription. A requirement 
        // can have several conditions.
        "requirements" : [
            {
                // Array of conditions. The key in the condition object must 
                // be "jmespath". The value can be any JMESPATH expression to 
                // extract data from the aggregated object. 
                // All conditions needs to be fulfilled in order for
                // a requirement to be fulfilled.

                "conditions" : [
                    {
                        "jmespath" : "identity=='pkg:maven/com.othercompany.library/artifact-name@1.0.0'"
                    },
                    {
                        "jmespath" : "confidenceLevels[?name=='my_confidence_level']"
                    }
                ]
            }

        ]
    }


## Requirements and conditions

Read more on how Eiffel Intelligence groups [requirements and conditions in subscriptions](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/subscriptions.md#writing-requirements-and-conditions).

