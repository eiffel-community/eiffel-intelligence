# Download Files

Via this RestApi user can download EI templates files.

These entrypoint exits today:

|Method|Endpoint                        |Comment                                    |
|------|--------------------------------|-------------------------------------------|
|GET   |/download                       |List available files that can be downloaded|
|GET   |/download/subscriptionsTemplate |Returns subscription template file|
|GET   |/download/rulesTemplate         |Returns rules template file|
|GET   |/download/eventsTemplate        |Returns events template file|


## List available files

    GET /download

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/download

## Download subscription template file

    GET /download/subscriptionsTemplate

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/download/subscriptionsTemplate

## Download rules template file

    GET /download/rulesTemplate

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/download/rulesTemplate

## Download events template file

    GET /download/eventsTemplate
Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/download/eventsTemplate
