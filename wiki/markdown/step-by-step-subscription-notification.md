# Step By Step Subscription Notification

Suppose a subscription is created (as shown below) by a user and then that is
stored in the subscription database. Read more about the [subscription REST API](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/subscription-API.md)
and [adding subscriptions via Eiffel Intelligence frontend GUI](https://github.com/eiffel-community/eiffel-intelligence-frontend/blob/master/wiki/markdown/add-subscription.md).

    {
        "created": "2017-07-26",
        "notificationMeta": "http://127.0.0.1:3000/ei/test_subscription_rest",
        "notificationType": "REST_POST",
        "restPostBodyMediaType": "application/x-www-form-urlencoded",
        "notificationMessageKeyValues": [{
            "formkey": "e",
            "formvalue": "{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}"
        }],
        "repeat": false,
        "requirements": [{
                "conditions": [{
                        "jmespath": "identity=='pkg:maven/com.mycompany.myproduct/sub-system@1.1.0'"
                    },
                    {
                        "jmespath": "testCaseExecutions[?testCase.conclusion == 'SUCCESSFUL' && testCase.id=='TC5']"
                    }
                ],
                "type": "ARTIFACT_1"
            },
            {
                "conditions": [{
                        "jmespath": "identity=='pkg:maven/com.mycompany.myproduct/sub-system@1.1.0'"
                    },
                    {
                        "jmespath": "testCaseExecutions[?testCaseStartedEventId == '13af4a14-f951-4346-a1ba-624c79f10e98']"
                    }
                ],
                "type": "ARTIFACT_1"
            }
        ],
        "subscriptionName": "Subscription_Test",
        "userName": "ABC"
    }


In this subscription, two requirements are given, where each requirement in turn
contains two conditions. As per subscription logic, when all the conditions in
any one of the given requirements are met in an aggregated object then the
subscription is triggered. Triggering means that the subscriber will be notified
with the chosen notification method. It should be noted that conditions are given
as JMESPath expression. Let us suppose that an aggregated object, as shown below,
is created:

    {
        "fileInformation": [{
            "extension": "jar",
            "classifier": ""
        }],
        "buildCommand": null,
        "testCaseExecutions": [{
            "testCaseFinishEventId": "11109351-41e0-474a-bc1c-f6e81e58a1c9",
            "testCaseStartedTime": 1481875925916,
            "testCaseStartedEventId": "cb9d64b0-a6e9-4419-8b5d-a650c27c59ca",
            "testCaseFinishedTime": 1481875935919,
            "testCase": {
                "conclusion": "SUCCESSFUL",
                "verdict": "PASSED",
                "tracker": "My Other Test Management System",
                "id": "TC5",
                "uri": "https://other-tm.company.com/testCase/TC5"
            }
        }],
        "id": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
        "time": 1481875891763,
        "type": "ARTIFACT_1",
        "identity": "pkg:maven/com.mycompany.myproduct/sub-system@1.1.0"
    }


When this aggregated object is evaluated against the subscriptions stored in
database, then it fulfills our subscription criteria. It can be seen that both
conditions of the first requirement are satisfied by the aggregated object.
More specifically, in the first condition, JMESPath rule is looking for:

    identity=='pkg:maven/com.mycompany.myproduct/sub-system@1.1.0'

and in the second condition it looks for

    testCaseExecutions.testCase.conclusion == 'SUCCESSFUL' && testCase.id=='TC5'

Both strings can be found in the aggregated object JSON. Consequently, the process
is started to send notification to specified subscriber. For this, 'notificationMeta'
and 'notificationType' field values are extracted from the subscription.

## Notify via REST POST
In the example subscription above, the notification is sent as **REST POST** to
the url http://127.0.0.1:3000/ei/test_subscription_rest. The notification message
in this subscription is prepared as key value pairs in the request.

    "notificationMessageKeyValues": [{
        "formkey": "e",
        "formvalue": "{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}"
    }]

The key is 'jsonparams' and the value is the full aggregated object. These are part
of the notification message for this particular subscription. Below is a list of the
key value pairs which will be sent for this subscription.

    parameters:
        jsonparameters: {full aggregated object}
        runpipeline: mybuildstep

If it was sent as raw JSON body, the notification message would look like below:

    {
        [
            {
                "fileInformation": [{
                    "extension": "jar",
                    "classifier": ""
                }],
                "buildCommand": null,
                "testCaseExecutions": [{
                    "testCaseFinishEventId": "11109351-41e0-474a-bc1c-f6e81e58a1c9",
                    "testCaseStartedTime": 1481875925916,
                    "testCaseStartedEventId": "cb9d64b0-a6e9-4419-8b5d-a650c27c59ca",
                    "testCaseFinishedTime": 1481875935919,
                    "testCase": {
                        "conclusion": "SUCCESSFUL",
                        "verdict": "PASSED",
                        "tracker": "My Other Test Management System",
                        "id": "TC5",
                        "uri": "https://other-tm.company.com/testCase/TC5"
                    }
                }],
                "id": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
                "time": 1481875891763,
                "type": "ARTIFACT_1",
                "identity": "pkg:maven/com.mycompany.myproduct/sub-system@1.1.0"
            }
        ]
    }


## Notify via MAIL
If the “notificationType” of the subscription is “MAIL” then the notification
message is sent to the email address(es) specified in the “notificationMeta”
field. If more than one email address is written, it should be written as a
comma separated string. The subject for the email can be set globally (same
for all subscriptions) in application.properties as "email.subject". It can
also be set for individual subscriptions using the Eiffel Intelligence front-end GUI.

As with the conditions, it is also possible to write JMESPath expressions
for the notification message. If we use the example subscription above, the
notification could be to send parameters which contains only some of the
information from the aggregated object. We could for example use the below
JMESPath expression in the subscription notification message:

    "{parameter: [{ name: 'artifactIdentity', value : to_string(@.identity) }, { name: 'testCase', value: to_string(@.testCaseExecutions[0].testCase.id) } { name: 'runpipeline', value : 'mybuildstep' }]}"

This expression only selects the field identity from the aggregated object
and this is used as value for the parameter "artifactIdentity", and the
complete notification message can be seen below:

    parameters:
        artifactIdentity: pkg:maven/com.mycompany.myproduct/sub-system@1.1.0
        testCase: TC5
        runpipeline: mybuildstep


## Failed notifications

If the notification via REST POST fails, then a fixed number of attempts are
made to resend successfully. The number of attempts are specified in the
[application.properties](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/application.properties)
as “notification.failAttempt”. If message sending attempts fails for the
specified number of time, then a failed notification is prepared and stored
in database. The name of the collection is specified in the application.properties
file as “failed.notification.collection-name”. The message is stored in the database
for a certain duration before being deleted. This time can be configured in
application.properties as “notification.ttl.value”.

**Failed notification in the failed notification database with TTL value:**

    [
        {
            "subscriptionName": "Sub1",
            "aggregatedObject": {},
            "notificationMeta": "http://localhost:9999/some-endpoint",
            "_id": {
                "$oid": "5d807a1d821b960af311fab3"
            },
            "time": {
                "$date": "2020-10-17T06:15:57.000Z"
            },
            "message": "Failed to send REST/POST notification!\nMessage: I/O error on POST request for \"http://localhost:9999/some-endpoint\": Connect to localhost:9999 [localhost/127.0.0.1] failed: Connection refused (Connection refused); nested exception is org.apache.http.conn.HttpHostConnectException: Connect to localhost:9999 [localhost/127.0.0.1] failed: Connection refused (Connection refused)"
        }
    ]
