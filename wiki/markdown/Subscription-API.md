# Subscription API

|Method|Endpoint               |Authentication|
|------|-----------------------|--------------|
|POST  |/subscriptions         |yes           |
|GET   |/subscriptions/\<name\>|no            |
|PUT   |/subscriptions         |yes           |
|DELETE|/subscriptions/\<name\>|yes           |
|GET   |/subscriptions         |no            |

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
                "jmespath": "gav.groupId=='com.mycompany.myproduct'"
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
                "jmespath": "gav.groupId=='com.mycompany.myproduct'"
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

## Get subscriptions for the given names

Get a single specific subscription or get multiples subscriptions with a comma 
separated list to the same endpoint

    GET /subscriptions/<name>
    GET /subscriptions/<name>,<name>,<name>

Curl command example

    curl -X GET -H "Content-type: application/json"  http://<host>:8090/subscriptions/<name>,<name>,<name>

## Update subscriptions

Modify existing Subscriptions based on subscriptionName. Multiple subscriptions 
may be sent through a json array.

    PUT /subscriptions

Curl command example 

    curl -X PUT -H "Content-type: application/json"  --data @<path to json file> http://<host>:8090/subscriptions

## Delete subscriptions for the given names

Delete a single specific subscription or delete multiples subscriptions with a 
comma separated list to the same endpoint

    DELETE /subscriptions/<name>
    DELETE /subscriptions/<name>,<name>,<name>

Curl command example

    curl -X DELETE -H "Content-type: application/json"  http://<host>:8090/subscriptions/<name>

## Get all subscriptions

Retrieves all the subscriptions

    GET /subscriptions

Curl command example

    curl -X GET -H "Content-type: application/json"  http://<host>:8090/subscriptions
