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

Takes one or several subscriptions in a JSON array as input. If LDAP is 
activated, the username of the person registering this subscription is 
included when saving the subscription in the database. The subscription 
name needs to be unique. 

    POST /subscriptions

### Curl command example:

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
    
   
 ## Subscription related information in front-end documentation
 
Read more about curl examples for subscription endpoint in Eiffel Intelligence Frontend docs [here]
(https://github.com/eiffel-community/eiffel-intelligence-frontend/blob/master/wiki/markdown/curl-examples.md#subscriptions).

Guidelines about writing requirements and conditions cane be found [here](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/subscriptions.md#writing-requirements-and-conditions)
 
