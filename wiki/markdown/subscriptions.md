# Subscriptions

When the desired Eiffel events have been aggregated a user would like to be
notified and there is possibility to register a subscription that will be run
on each save of an aggregated object in the database.

Whenever an aggregated object is created or modified, it is evaluated against
all registered subscriptions to find out whether the aggregated object meets any subscription
requirements. If it fulfills a subscription requirement then a notification is
sent to the subscriber as specified in that subscription. For further
explanation of the process, [consider the following example](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/step-by-step-subscription-notification.md).


### Adding subscriptions
It is possible to add subscriptions using the [Eiffel Intelligence
front-end GUI](https://github.com/eiffel-community/eiffel-intelligence-frontend/blob/master/wiki/markdown/add-subscription.md).
It is also possible to manage subscriptions [using the REST API](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/subscription-API.md).


### Adding comments inside subscription

It is possible for users to add comments, at any level, in subscriptions. These
comments should be a new field in the given subscription, following the json format.

    {
        "description": "It is my comment"
    }


### Types of notifications for subscriptions

Today Eiffel Intelligence supports notifications by email or by sending
a HTTP POST request. It is also possible to pass parameters with the
HTTP request. Example subscriptions with different types of notification
can be found in the below links:

* [Subscription with HTTP POST notification](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/subscription-with-REST-POST-notification.md)
* [Subscription with HTTP POST notification to trigger parameterized Jenkins job](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/triggering-jenkins-jobs.md)
* [Subscription with E-mail notification](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/subscription-with-email-notification.md)


### Writing requirements and conditions

When writing subscription requirements, they are referencing the structure
of the aggregated object which Eiffel Intelligence will create based on
the rules it was configured with. It is therefore important to know what
the aggregated object will look like when writing the conditions.

In order for a subscription to be triggered, the subscription requirement
needs to be fulfilled. The requirements can be grouped with one or several
conditions inside them. Each requirement group is separated with an 'OR',
while the conditions inside a requirement are connected with an 'AND'. This
means all the conditions in a requirement group must be fulfilled, while
only one of the requirement groups needs to be fulfilled, for the
subscription to trigger.

Some example subscriptions for a particular aggregated object [can be found here]().
