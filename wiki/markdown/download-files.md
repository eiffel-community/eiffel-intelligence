# Download Files

Via this RestApi user can download EI templates files.

These entrypoint exits today:

|Method|Endpoint                         |Comment                                    |
|------|---------------------------------|-------------------------------------------|
|GET   |/download                        |List available files that can be downloaded|
|GET   |/download/subscriptions-template |Returns subscription template file|
|GET   |/download/rules-template         |Returns rules template file|
|GET   |/download/events-template        |Returns events template file|


## List available files

    GET /download

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/download

## Download subscription template file

    GET /download/subscriptions-template

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/download/subscriptions-template

## Download rules template file

    GET /download/rules-template

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/download/rules-template

## Download events template file

    GET /download/events-template
Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/download/events-template
