# Templates

Via this REST API the user can download templates files from Eiffel Intelligence.
These endpoints exists today:

|Method|Endpoint                        |Comment                                        |
|------|--------------------------------|-----------------------------------------------|
|GET   |/templates                      |List available templates that can be downloaded|
|GET   |/templates/subscriptions        |Returns subscription template file             |
|GET   |/templates/rules                |Returns rules template file                    |
|GET   |/templates/events               |Returns events template file                   |


## List available REST endpoints to download templates

    GET /templates

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:port/templates

## Get subscription template file

    GET /templates/subscriptions

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/templates/subscriptions

## Get rules template file

    GET /templates/rules

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/templates/rules

## Get events template file

    GET /templates/events
    
Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/templates/events
