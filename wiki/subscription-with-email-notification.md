# Subscription with EMAIL Notification

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

Below are the limitations of notificationMessageKeyValues.

### Limitations

* The form key should always be an empty string.
* The content should be in JSON format.
* The form value can only be one JSON object is allowed (not several objects on the same level).
* Only one key/value pair is allowed (since only the first value will be used).
* It is possible to make use of the data inside the aggregation, using the "@" symbol.
* As with the conditions, it is also possible to write JMESPath expressions.

In the below example the full aggregation can be sent using the '@' character.

    "notificationMessageKeyValues": [
        {
            "formkey": "",
            "formvalue": "@"
        }
    ]

To access parts of the aggregation it is possible to define like the below example:

    "notificationMessageKeyValues": [{
        "formkey": "",
        "formvalue": "{parameters: [{ name: 'jsonparams', value : to_string(@) }, { name: 'artifactIdentity', value : @.identity }]}"
    }]

The key is 'jsonparams' and the value is the full aggregated object. The second parameter is the artifact identity which is extracted from the aggregation, These are part of the notification message for this particular subscription. Below is a list of the parameters defined in the above notification message which will be sent for the subscription.

    parameters:
        jsonparams: //full aggregated object
        artifactIdentity: //extract the identity string from the aggregation


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

Additional examples of how to write subscription with email notification:

The below subscription to trigger a notification an aggregation should contain an artifact with a given identity.

    {
        "subscriptionName" : "Subscription_with_Mail_Notification",
        "ldapUserName" : "ABC",
        "repeat" : false,
        "created" : 1580287633704,

        "notificationType" : "MAIL",
        "notificationMeta" : "mymail@company.com, another@email.com",
        "emailSubject" : "My Email Subject",

        "authenticationType" : "NO_AUTH",

        "notificationMessageKeyValues" : [
            {
                "formkey" : "",
                "formvalue" : "{parameters: [{ name: 'artifactIdentity', value : to_string(@.identity) }, { name: 'testCase', value: to_string(@.testCaseExecutions[0].testCase.id) }]}"
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

The JMESPath expression in the subscription notification selects the field identity from the aggregated object and this is used as value for the parameter "artifactIdentity". The second parameter is also extracted from the aggregation and results in a string value of the testcase id. The complete notification message can be seen below:

    parameters:
        artifactIdentity: //extract the identity string from the aggregation
        testCase: //extract the testCaseId string from the aggregation

The below subscription will perform a MAIL notification when an artifact identity should contain a given artifact namespace.

    {
        "subscriptionName" : "Subscription_with_Mail_Notification",
        "ldapUserName" : "ABC",
        "repeat" : false,
        "created" : 1580287633708,

        "notificationType" : "MAIL",
        "notificationMeta" : "mymail@company.com, another@email.com",
        "emailSubject" : "My Email Subject",

        "authenticationType" : "NO_AUTH",

        "notificationMessageKeyValues" : [
            {
                "formkey" : "",
                "formvalue" : "@"
            }
        ],
        "requirements" : [
            {
                "conditions" : [
                    {
                        "jmespath" : "split(identity, '/') | [1] =='com.othercompany.library'"
                    }
                ]
            }
        ]
    }

The above example mail notififcation message is the full aggregated object.

## Enabling E-mail Notification for Eiffel Intelligence

In order to use subscriptions with e-mail notifications, Eiffel Intelligence
must be set up with an e-mail server in [application.properties](../src/main/resources/application.properties).
The e-mail subject for a subscription can be set globally or for each individual
subscription. If the field emailSubject is left empty in the subscription, Eiffel
Intelligence will use the default one.

## Requirements and Conditions
Read more on how Eiffel Intelligence groups [requirements and conditions in subscriptions](subscriptions.md#writing-requirements-and-conditions).
