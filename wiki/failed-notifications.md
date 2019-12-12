# Failed Notifications

|Method|Endpoint             |
|------|---------------------|
|GET   |/failed-notifications|

## Get All Failed Notifications

    GET /failed-notifications

**Query parameters**:
subscriptionNames={Subscription1,Subscription2,...}

Examples of this endpoint using curl

    curl -X GET -H "Content-type: application/json" localhost:39835/failed-notifications?subscriptionNames=Subscription1

The content of a failed notification json object may look something like the following

    [
        {
            "subscriptionName": "Subscription1",
            "aggregatedObject": {},
            "notificationMeta": "http://localhost:9999/some-endpoint",
            "_id": {
                "$oid": "5d807a1d821b960af311fab3"
            },
            "time": {
                "$date": "2020-10-17T06:15:57.000Z"
            },
            "message": "Failed to send REST/POST notification!\nMessage: I/O error on POST request for \"http://localhost:9999/some-endpoint\": Connect to localhost:9999 [localhost/127.0.0.1] failed: Connection refused (Connection refused); nested exception is org.apache.http.conn.HttpHostConnectException: Connect to localhost:9999 [localhost/127.0.0.1] failed: Connection refused (Connection refused)"
        }
    ]

It contains the name of the subscription that failed, a snapshot of the 
aggregated object that triggered the subscription, the notification meta 
which is the mail or service endpoint to contact, the time of occurrence 
and the message that contains the exception that was thrown. This information 
is meant to help you figure out why the notification failed. In the example 
above the reason is that the notification meta URL is invalid and because 
of this Eiffel Intelligence could not establish a connection.
