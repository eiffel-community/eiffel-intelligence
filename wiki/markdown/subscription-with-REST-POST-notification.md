# Subscription with REST POST notification

A valid subscription for REST POST notification is provided below with comments
for each field.

_**OBS! Comments are only for documentation purposes and a subscription should
not contain them. The subscription will be rejected at this moment if it
contains comments like below.**_

_**Subscription templates can be found [here](https://github.com/Ericsson/eiffel-intelligence/tree/master/src/main/resources/templates).**_

    {
        // The name of the subscription to make it easy to search for it.
        // Only numbers,letters and underscore allowed.
        "subscriptionName" : "Subscription1",

        // The name of the logged in user creating or updating the subscription
        // added by Eiffel Intelligence if LDAP is enabled. Defaults to an empty string.
        "ldapUserName" : "ABC",

        // Instructs whether the same subscription should be re-triggered 
        // for new additions to the aggregated object. If this is set to 
        // false, only the first time the conditions are fulfilled, a 
        // notification will be triggered. No matter how many times the 
        // aggregated object is updated.
        "repeat" : false,
        
        // Creation time in system time, added by Eiffel Intelligence.
        "created" : 1542117412833,

        "authenticationType" : "BASIC_AUTH",

        // The username and password to insert in headers of the POST request when sending
        // a notification via REST POST.
        "userName" : "functionalUser",
        "password" : "functionalUserPassword",

        // How to notify when a subscription is triggered.
        "notificationType" : "REST_POST",

        // Which url to use for the HTTP POST request.
        "notificationMeta" : "http://eiffel-jenkins1:8080/job/ei-artifact-triggered-job/build",

        // Headers for the HTTP request, can be 'application/x-www-form-urlencoded' or 'application/json'.
        "restPostBodyMediaType" : "application/json",

        // The data to send with the HTTP POST request.
        "notificationMessageKeyValues" : [
            {
                // The form value will be run through JMESPATH engine to extract
                // content from the aggregated object.

                "formkey" : "json",
                "formvalue" : "{parameter: [{ name: 'jsonparams', value : to_string(@) }]}"
            }
        ],

        // An array of requirements. At least one requirement should be fulfilled to
        // trigger this subscription.
        "requirements" : [
            {
                // Array of conditions. Here we use JMESPATH condition based on content in
                // aggregated object. All conditions needs to be fulfilled in order for
                // a requirement to be fulfilled.

                "conditions" : [
                    {
                        "jmespath" : "identity=='pkg:maven/com.othercompany.library/artifact-name@1.0.0'"
                    }
                ]
            }

        ]
    }


## Requirements and conditions

Read more on how Eiffel Intelligence groups [requirements and conditions in subscriptions](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/subscriptions.md#writing-requirements-and-conditions).

