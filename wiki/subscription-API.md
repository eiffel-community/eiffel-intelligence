# Subscription API

|Method|Endpoint                                           |Authentication|
|------|---------------------------------------------------|--------------|
|GET   |/subscriptions                                     |no            |
|GET   |/subscriptions/\<name\>                            |no            |
|GET   |/subscriptions?subscriptionNames=\<name\>,\<name\> |no            |
|POST  |/subscriptions                                     |yes           |
|PUT   |/subscriptions                                     |yes           |
|DELETE|/subscriptions/\<name\>                            |yes           |
|DELETE|/subscriptions?subscriptionNames=\<name\>,\<name\> |yes           |

## Get All Subscriptions

Retrieves all the subscriptions

    GET /subscriptions

Curl command example

    curl -X GET -H "Content-type: application/json"  http://<host>:8090/subscriptions

## Get Multiple Subscriptions

Retrieves one or more subscriptions with a comma separated list

    GET /subscriptions?subscriptionNames=<name1>,<name2>,...

Curl command example

    curl -X GET -H "Content-type: application/json"  http://<host>:8090/subscriptions?subscriptionNames=<name1>,<name2>,...

## Get Subscription for the Given Name

Get a single specific subscription, using either of the below commands:

    GET /subscriptions?subscriptionNames=<name1>
    GET /subscriptions/<name>

Curl command examples:

    curl -X GET -H "Content-type: application/json"  http://<host>:8090/subscriptions/<name>
    curl -X GET -H "Content-type: application/json"  http://<host>:8090/subscriptions?subscriptionNames=<name>

## Create Subscriptions

Takes one or several subscriptions in a JSON array as input. If LDAP is 
activated, the username of the person registering this subscription is 
included when saving the subscription in the database. The subscription 
name needs to be unique. 

    POST /subscriptions

### Curl Command Example

    curl -X POST -H "Content-type: application/json" --data @<path to file> http://<host>:8090/subscriptions

Eiffel Intelligence takes a JSON list of one or several subscription objects. 
Example of a subscription array input:

    [
      {
        ..Subscription 1..
      },
      {
        ..Subscription 2..
      }
    ]

 Guidelines for writing requirements and conditions for creating subscriptions can be found [here](subscriptions.md#writing-requirements-and-conditions)

## Update Subscriptions

Modify existing Subscriptions based on subscriptionName. Multiple subscriptions
may be sent through a JSON array.

    PUT /subscriptions

Curl command example

    curl -X PUT -H "Content-type: application/json"  --data @<path to json file> http://<host>:8090/subscriptions

## Delete Subscription for the Given Name

Delete a single specific subscription using either of the below commands:

    DELETE /subscriptions/<name>
    DELETE /subscriptions?subscriptionNames=<name>

Curl command examples:

    curl -X DELETE -H "Content-type: application/json"  http://<host>:8090/subscriptions/<name>
    curl -X DELETE -H "Content-type: application/json"  http://<host>:8090/subscriptions?subscriptionNames=<name>

## Delete Multiple Subscriptions

Delete one or more subscriptions with a comma separated list

    DELETE /subscriptions?subscriptionNames=<name1>,<name2>,...

Curl command example

    curl -X DELETE -H "Content-type: application/json"  http://<host>:8090/subscriptions?subscriptionNames=<name1>,<name2>,...

## Subscription related information in front-end documentation

Read more about curl examples for subscription endpoint in Eiffel Intelligence front-end docs [here](https://github.com/eiffel-community/eiffel-intelligence-frontend/blob/master/wiki/curl-examples.md#subscriptions) 
