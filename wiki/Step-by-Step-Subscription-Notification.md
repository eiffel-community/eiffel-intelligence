# Step By Step Subscription Notification

Whenever an aggregated object is created or modified, it is evaluated against 
all registered subscriptions to find out whether it meets any subscription 
requirements. If it fulfils a subscription requirement then notification is 
sent to the subscriber as specified in that subscription. For further explanation, 
consider the following example:
Suppose a subscription is created (as shown below) by a user and then that is 
stored in the subscription database.
More detail about subscription and its fields can be found [here](./Subscription-AP.mdI) 
and [here](./Add-Subscription.md).

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
                        "jmespath": "gav.groupId=='com.mycompany.myproduct'"
                    },
                    {
                        "jmespath": "testCaseExecutions[?testCase.conclusion == 'SUCCESSFUL' && testCase.id=='TC5']"
                    }
                ],
                "type": "ARTIFACT_1"
            },
            {
                "conditions": [{
                        "jmespath": "gav.groupId=='com.mycompany.myproduct'"
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
as JMESPath rule. Let us suppose that an aggregated object, as shown in below, 
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
        "gav": {
            "groupId": "com.mycompany.myproduct",
            "artifactId": "sub-system",
            "version": "1.1.0"
        }
    }


When this aggregated object is evaluated against the subscriptions stored in 
database, then it fulfils our subscription criteria. It can be seen that both 
conditions of the first requirement are satisfied by the aggregated object. 
More specifically, in the first condition, JMESPath rule is looking for the 
gav.groupId=='com.mycompany.myproduct' and in the second condition it looks for 
testCaseExecutions.testCase.conclusion == 'SUCCESSFUL' and testCase.id=='TC5'. 
Both strings can be found in the aggregated object JSON. Consequently, the 
process is started to send notification to specified subscriber. For this, 
'notificationType' and 'notificationType' field values are extracted from the 
subscription. In this case the notification need to be send as “REST POST” at 
the url http://127.0.0.1:3000/ei/test_subscription_rest. Notification message 
is prepared as key value pairs. For this case the notification message body 
look like this:

**Notification Message:**

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
                "gav": {
                    "groupId": "com.mycompany.myproduct",
                    "artifactId": "sub-system",
                    "version": "1.1.0"
                }
            }
        ]
    }

If the notification message sending fails, then a fixed number of attempts are 
made to resend successfully. The number of attempts are specified in the 
property file as “notification.failAttempt”. If message sending attempts fails 
for the specified number of time, then a missed notification is prepared and 
stored in database. The name of the database is specified in the property file 
as “missedNotificationDataBaseName” and collection name as 
“missedNotificationCollectionName”. The message is stored in the database for 
a certain duration before being deleted. This time can be configured in the 
property file as “notification.ttl.value”.

If the “notificationType” of the subscription is “MAIL” then the notification 
message is sent to the email address(es) specified in the “notificationMeta” 
field. If more than one email address is written, it should be written as a 
comma separated string. Currently, the subject for email notification is not 
configurable for individual subscriptions. It is the same for all email 
notifications and configured in the property file as “email.subject”.

**Miss notification in the miss notification database with TTL value:**

    {
        "subscriptionName": "Subscription_1",
        "notificationMeta": "http://127.0.0.1:3000/ei/test_subscription_rest",
        "Time": {
            "$date": "2018-11-10T20:21:56.000Z"
        },
        "AggregatedObject": {
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
            "gav": {
                "groupId": "com.mycompany.myproduct",
                "artifactId": "sub-system",
                "version": "1.1.0"
            }
        }
    }
