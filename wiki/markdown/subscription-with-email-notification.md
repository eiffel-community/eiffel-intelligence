# Subscription with EMAIL notification

A valid subscription for email notification is provided below with comments for
each field.

_**OBS! Comments are only for documentation purposes and a subscription should
not contain them. The subscription will be rejected at this moment if it
contains comments like below.**_

_**Subscription templates can be found [here](https://github.com/eiffel-community/eiffel-intelligence/tree/master/src/main/resources/templates).**_

    {
        // The name of the subscription to make it easy to search for.
        // Only numbers, letters and underscore allowed.
        "subscriptionName" : "Subscription3_Mail_Notification",

        // The name of the logged in user creating or updating the subscription
        // added by Eiffel Intelligence if LDAP is enabled. Not required. Defaults to an empty string.
        "ldapUserName" : "ABC",

        // Instructs whether the same subscription should be re-triggered
        // for new additions to the aggregated object. If this is set to
        // false, only the first time the conditions are fulfilled, a
        // notification will be triggered. No matter how many times the
        // aggregated object is updated.
        "repeat" : false,

        // Creation time in system time, added by Eiffel Intelligence.
        "created" : 1542802953782,

        // If any authentication is needed by Eiffel Intelligence to send 
        // the notification email.
        "authenticationType" : "NO_AUTH",

        // How to notify when a subscription is fulfilled.
        "notificationType" : "MAIL",
        
        // The recipient of the email. One or several email addresses can 
        // be defined, separated with a comma.
        "notificationMeta" : "mymail@company.com, another@email.com",
        
        // It is possible to define an email subject per subscription, or
        // use the email subject which is configured in Eiffel Intelligence 
        // application.properties. Not required.
        "emailSubject" : "My Email Subject",

        // Content of the email message.
        "notificationMessageKeyValues" : [
            {
                // The form key should always be an empty string for email messages.
                // The form value will be run through JMESPATH engine so
                // it is possible to use JMESPATH expressions to extract
                // content from the aggregated object. The form value can
                // only be one JSON object.

                "formkey" : "",
                "formvalue" : "{mydata: [{ fullaggregation : to_string(@) }]}"
            }
        ],

        /// An array of one or several requirements. At least one requirement 
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
                    }
                ]
            }
        ]
    }

## Enabling e-mail notification for Eiffel Intelligence

In order to use subscriptions with e-mail notifications, Eiffel Intelligence
must be set up with an e-mail server in [application.properties](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/application.properties).
The e-mail subject for a subscription can be set globally or for each individual
subscription. If the field emailSubject is left empty in the subscription, Eiffel
Intelligence will use the default one.

## Requirements and conditions

Read more on how Eiffel Intelligence groups [requirements and conditions in subscriptions](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/subscriptions.md#writing-requirements-and-conditions).



