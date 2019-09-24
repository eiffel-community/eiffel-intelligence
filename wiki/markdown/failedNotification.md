# FailedNotification

|Method|Endpoint             |
|------|---------------------|
|GET   |/failed-Notifications|


## failed notifications
    GET /failed-Notifications

**Query parameters**:
SubscriptionName=<Subscription Name/Subscription ID>

Examples of this endpoint using curl

    curl -X GET -H "Content-type: application/json" localhost:39835/failed-Notifications?SubscriptionName=Subscription_1

