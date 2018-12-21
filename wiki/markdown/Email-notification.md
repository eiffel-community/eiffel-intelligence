# Subscription with EMAIL notification

A valid subscription for email notification is provided below with comments for
each field.

_**OBS! Comments are only for documentation purposes and a subscription should
not contain them. The subscription will be rejected at this moment if it
contains comments.**_

_**Subscription templates can be found [here](https://github.com/Ericsson/eiffel-intelligence/tree/master/src/main/resources/templates).**_

    {
        // the name of the subscription to make it easy to search for it
        // Only numbers,letters and underscore allowed
        "subscriptionName" : "Subscription3_Mail_Notification",

        // the name of the logged in user creating or updating the subscription
        // added by Eiffel Intelligence if LDAP is enabled, defaults to an empty string
        "ldapUserName" : "ABC",

        // instructs whether same subscription should be re-triggered for new additions
        // to the aggregated object. If false only first time the conditions are fulfilled
        // a notification will be triggered. No matter how many times the aggregated object
        // is updated.
        "repeat" : false,
        "notificationMessageKeyValuesAuth" : [],
        "authenticationType" : "NO_AUTH",

        // creation time in system time. added by Eiffel Intelligence
        "created" : 1542802953782,

        // how to notify the user
        "notificationType" : "MAIL",
        "notificationMeta" : "mymail@company.com",
        "emailSubject" : "My Email Subject",
        "restPostBodyMediaType" : "",

        // content of the email message
        "notificationMessageKeyValues" : [
            {
                // form key should always be an empty string for email messages.
                // form value will be run through JMESPATH engine to extract
                // content from aggregated object.

                "formkey" : "",
                "formvalue" : "{mydata: [{ fullaggregation : to_string(@) }]}"
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

