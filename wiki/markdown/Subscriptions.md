# Subscriptions

When the desired Eiffel events have been aggregated a user would like to be
notified and there is possibility to register a subscription that will be run
on each save of an aggregated object in the database.

It is possible for users to add comments, at any level, in subscriptions. These
comments should be a new field in the given subscription, following the json format.

    {
        "description": "It is my comment"
    }


## Notifications

Today Eiffel Intelligence supports notifications by email or by sending
a HTTP POST request.

