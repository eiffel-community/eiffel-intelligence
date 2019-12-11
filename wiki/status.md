# Status

|Method|Endpoint         |Authentication|
|------|-----------------|--------------|
|GET   |/status          |no            |

## Check the Eiffel Intelligence Status

An endpoint to check if Eiffel Intelligence back-end is working properly.
The response data contains status of Eiffel Intelligence and dependent service
such as RabbitMQ and MongoDB.

There are 3 different kind of status indications:
 **NOT_SET** *A system check has not yet occurred and status is unknown.*
 **AVAILABLE** *A status check has been performed and the service is working as intended.*
 **UNAVAILABLE** *A system check has been performed and a service is not working properly.*

Eiffel Intelligence status reflects the status dependent services and will only be
available if all dependent service is available.

### Endpoint

    GET /status

### Curl Command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/status

### Example of Response Body

    {
        "eiffelIntelligenceStatus": "UNAVAILABLE",
        "rabbitMQStatus": "AVAILABLE",
        "mongoDBStatus": "NOT_SET"
    }
