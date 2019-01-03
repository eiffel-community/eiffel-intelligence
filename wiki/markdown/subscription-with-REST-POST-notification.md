# Subscription with REST POST notification

A valid subscription for REST POST notification is provided below with comments
for each field.

_**OBS! Comments are only for documentation purposes and a subscription should
not contain them. The subscription will be rejected at this moment if it
contains comments like below.**_

_**Subscription templates can be found [here](https://github.com/Ericsson/eiffel-intelligence/tree/master/src/main/resources/templates).**_

    {
        "subscriptionName" : "Subscription1",

        // the name of the user who created the subscription
        // defaults to an empty string if LDAP is disabled
        "ldapUserName" : "ABC",

        // instructs whether same subscription should be re-triggered for new additions
        // to the aggregated object. If false only first time the conditions are fulfilled
        // a notification will be triggered. No matter how many times the aggregated object
        // is updated.
        "repeat" : false,
        "notificationMessageKeyValuesAuth" : [],
        "created" : 1542117412833,

        // how to notify when a subscription is triggered
        "notificationType" : "REST_POST",
        "authenticationType" : "BASIC_AUTH",

        // the username and password to insert in headers of the POST request when sending
        // a notification via REST POST
        "userName" : "functionalUser",
        "password" : "functionalUserPassword",

        // which url to use for the HTTP POST request
        "notificationMeta" : "http://eiffel-jenkins1:8080/job/ei-artifact-triggered-job/build",

        // headers for the HTTP request, can be 'application/x-www-form-urlencoded' or 'application/json'
        "restPostBodyMediaType" : "application/json",

        // the data to send with the HTTP POST request
        "notificationMessageKeyValues" : [
            {
                // form value will be run through JMESPATH engine to extract
                // content from aggregated object.

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
                        "jmespath" : "gav.groupId=='com.othercompany.library'"
                    }
                ]
            }

        ]
    }
