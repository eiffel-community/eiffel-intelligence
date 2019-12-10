# Subscription with EMAIL notification

A valid subscription for email notification is provided below with each 
field explained.

_**Subscription templates can be found [here](../src/main/resources/templates).**_

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
The recipient of the email notification. One or several email addresses can 
be defined, separated with a comma.

## emailSubject
It is possible to define an email subject per subscription, or use the email 
subject which is configured in Eiffel Intelligence application.properties. Not required.

## authenticationType
If any authentication is needed by Eiffel Intelligence to send the notification email.

## notificationMessageKeyValues
Content of the email message. The form key should always be an empty string 
for email messages. Only one key/value pair is allowed for email message body, 
since only the first value will be used. 

The form value will be run through JMESPath engine so it is possible to use 
JMESPath expressions to extract content from the aggregated object. The 
form value can only be one JSON object.

## requirements
An array of one or several requirements. At least one requirement should be 
fulfilled to trigger this subscription. A requirement can have several conditions.

## conditions
Array of conditions. The key in the condition object must be "jmespath". 
The value can be any JMESPath expression to extract data from the aggregated 
object. All conditions needs to be fulfilled in order for a requirement to 
be fulfilled.

    {
        "subscriptionName" : "Subscription3_Mail_Notification",
        "ldapUserName" : "ABC",
        "repeat" : false,
        "created" : 1542802953782,

        "notificationType" : "MAIL",
        "notificationMeta" : "mymail@company.com, another@email.com",
        "emailSubject" : "My Email Subject",

        "authenticationType" : "NO_AUTH",

        "notificationMessageKeyValues" : [
            {
                "formkey" : "",
                "formvalue" : "{mydata: [{ fullaggregation : to_string(@) }]}"
            }
        ],
        "requirements" : [
            {
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
must be set up with an e-mail server in [application.properties](../src/main/resources/application.properties).
The e-mail subject for a subscription can be set globally or for each individual
subscription. If the field emailSubject is left empty in the subscription, Eiffel
Intelligence will use the default one.

## Requirements and conditions

Read more on how Eiffel Intelligence groups [requirements and conditions in subscriptions](subscriptions.md#writing-requirements-and-conditions).
