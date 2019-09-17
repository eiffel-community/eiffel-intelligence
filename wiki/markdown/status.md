# Status

|Method|Endpoint         |Authentication|
|------|-----------------|--------------|
|GET   |/status          |no            |

## Check the Eiffel Intelligence status

An endpoint to check if Eiffel Intelligence back-end is working properly.
The response data contains status of Eiffel Intelligence and dependent service
such as RabbitMQ and MongoDB.

There are 3 differnt kind of status indications:
 **NOT_SET** *A system check has not yet occured and status is unknown.*
 **AVAILABLE** *A status check has been performed and the service is woring as intended.*
 **UNAVAILABLE** *A system check has been performed and a service is not working properly.*

Eiffel Intelligece status reflects the status dependent services and will only be
available if all dependent service is available.

Endpoint

    GET /status

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/status

Example of response body

    {
        "eiffelIntelligenceStatus": "UNAVAILABLE",
        "rabbitMQStatus": "AVAILABLE",
        "mongoDBStatus": "NOT_SET"
    }
