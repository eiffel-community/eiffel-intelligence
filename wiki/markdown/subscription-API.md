# Subscription API

|Method|Endpoint                                |Authentication|
|------|----------------------------------------|--------------|
|POST  |/subscriptions                          |yes           |
|GET   |/subscriptions                          |no            |
|GET   |/subscriptions/\<name\>                 |no            |
|PUT   |/subscriptions                          |yes           |
|DELETE|/subscriptions                          |yes           |
|DELETE|/subscriptions/\<name\>                 |yes           |

## Create subscriptions

Takes the subscription rules, the name for subscription and the user name of
the person registering this subscription and saves the subscription in
subscription database. The subscription name needs to be unique. Multiple
subscriptions may be sent through a json array.

    POST /subscriptions

Curl command example

    curl -X POST -H "Content-type: application/json" --data @<path to file> http://<host>:8090/subscriptions

Example subscriptions

    [
      {
        "subscriptionName": "Subscription_Test",
        "ldapUserName": "ABC",
        "created": "2018-08-13",
        "repeat": false,
        "notificationMeta": "http://127.0.0.1:3000/ei/test_subscription_rest",
        "notificationType": "REST_POST",
        "restPostBodyMediaType": "application/x-www-form-urlencoded",
        "notificationMessageKeyValues": [
          {
            "formkey": "e",
            "formvalue": "{parameter: [{ name: 'jsonparams', value : to_string(@) }, { name: 'runpipeline', value : 'mybuildstep' }]}"
          }
        ],
        "requirements": [
          {
            "type": "ARTIFACT_1",
            "conditions": [
              {
               // notice single quotes surrounds the value to be checked
               // this tells jmespath that this is a constant and not
               // an object from the aggregated object
                "jmespath": "identity=='pkg:maven/com.mycompany.myproduct/artifact-name@1.0.0'"
              },
              {
                "jmespath": "testCaseExecutions[?testCase.conclusion == 'SUCCESSFUL' && testCase.id=='TC5']"
              }
            ]
          },
          {
            "type": "ARTIFACT_1",
            "conditions": [
              {
                "jmespath": "identity=='pkg:maven/com.mycompany.myproduct/artifact-name@1.0.0'"
              },
              {
                "jmespath": "testCaseExecutions[?testCaseStartedEventId == '13af4a14-f951-4346-a1ba-624c79f10e98']"
              }
            ]
          }
        ]
      }
    ]


    // this subscription will trigger a REST POST CALL
    // when an artifact for issue JIRA-1234
    // has passed testcase TC5 successfully
    {
        "created": "2017-07-26",
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
            "description" : "A subscription that will notify when an artifact for given issue id, has passed a certain test successfully",
            "conditions": [
              {
                "jmespath": "incomplete_path_contains(@, 'issues.id','JIRA-1234')"
              },
              {
                "jmespath": "(length(testCaseExecutions[?(outcome.id == 'TC5' && outcome.conclusion == 'SUCCESSFUL')]) > `0`)"
              }
            ],
            "type": "ARTIFACT_1"
          }
        ],
        "subscriptionName": "artifactRequirementSubscription",
        "ldapUserName": "ABC"
    }


Example of a subscription array

    [
      {
        ..Subscription 1..
      },
      {
        ..Subscription 2..
      }
    ]

## Get all subscriptions

Retrieves all the subscriptions

    GET /subscriptions

Curl command example

    curl -X GET -H "Content-type: application/json"  http://<host>:8090/subscriptions

## Get multiple subscriptions

Retrieves one or more subscriptions with a comma separated list

    GET /subscriptions?subscriptionNames=<name1>,<name2>,...

Curl command example

    curl -X GET -H "Content-type: application/json"  http://<host>:8090/subscriptions?subscriptionNames=<name1>,<name2>,...

## Get subscription for the given name

Get a single specific subscription

    GET /subscriptions/<name>

Curl command example

    curl -X GET -H "Content-type: application/json"  http://<host>:8090/subscriptions/<name>

## Update subscriptions

Modify existing Subscriptions based on subscriptionName. Multiple subscriptions
may be sent through a json array.

    PUT /subscriptions

Curl command example

    curl -X PUT -H "Content-type: application/json"  --data @<path to json file> http://<host>:8090/subscriptions

## Delete multiple subscriptions

Delete one or more subscriptions with a comma separated list

    DELETE /subscriptions?subscriptionNames=<name1>,<name2>,...

Curl command example

    curl -X DELETE -H "Content-type: application/json"  http://<host>:8090/subscriptions?subscriptionNames=<name1>,<name2>,...


## Delete subscription for the given name

Delete a single specific subscription

    DELETE /subscriptions/<name>

Curl command example

    curl -X DELETE -H "Content-type: application/json"  http://<host>:8090/subscriptions/<name>
