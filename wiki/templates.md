# Templates

Via this REST API the user can download templates files from Eiffel Intelligence.
These endpoints exists today:

|Method|Endpoint                        |Comment                                        |
|------|--------------------------------|-----------------------------------------------|
|GET   |/templates                      |List available templates that can be downloaded|
|GET   |/templates/subscriptions        |Returns subscription template file             |
|GET   |/templates/rules                |Returns rules template file                    |
|GET   |/templates/events               |Returns events template file                   |


## List Available REST Endpoints to Download Templates

    GET /templates

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:port/templates

## Get Subscription Template File

    GET /templates/subscriptions

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/templates/subscriptions

## Get Rules Template File

    GET /templates/rules

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/templates/rules

## Get Events Template File

    GET /templates/events
    
Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/templates/events
