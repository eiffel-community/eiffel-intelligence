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
subscription to trigger. Subscription templates [can be found here](https://github.com/eiffel-community/eiffel-intelligence/tree/master/src/main/resources/templates).

We will go through how to write some example requirements in subscriptions
based on the below aggregated object. Conditions in subscriptions are
written with [JMESPath syntax](http://jmespath.org/specification.html).
Below is a document from the database which contains a unique ID and an
aggregated object.

    {
        "_id" : "df4cdb42-1580-4cff-b97a-4d0faa9b2b22",
        "aggregatedObject" : {
            "fileInformation" : [
                {
                    "extension" : "war",
                    "classifier" : ""
                }
            ],
            "artifactCustomData" : [],
            "buildCommand" : "trigger",
            "identity" : "pkg:maven/com.mycompany.myproduct/artifact-name@2.1.7",
            "confidenceLevels" : [
                {
                    "eventId" : "e3be0cf8-2ebd-4d6d-bf5c-a3b535cd084e",
                    "name" : "dummy_1_stable",
                    "time" : 1521452400324,
                    "value" : "SUCCESS"
                }
            ],
            "TemplateName" : "ARTIFACT_1",
            "id" : "df4cdb42-1580-4cff-b97a-4d0faa9b2b22",
            "time" : 1521452368194,
            "type" : "EiffelArtifactCreatedEvent",
            "publications" : [
                {
                    "eventId" : "2acd348d-05e6-4945-b441-dc7c1e55534e",
                    "locations" : [
                        {
                            "type" : "NEXUS",
                            "uri" : "http://host:port/path"
                        }
                    ],
                    "time" : 1521452368758
                }
            ]
        }
    }

Let's say we need a subscription which is triggered on when an artifact
has been published to a location of type "NEXUS". This subscription wants
to listen for when an Eiffel ArtifactPublished event is aggregated by
Eiffel Intelligence.

It is possible to use [JMESPath expressions](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/rules.md#What-is-JMESPath?)
in the conditions of a subscription. We can write a requirement in the
subscription with the condition to check for the data of interest. In this
case, it is a JSON object in the list of locations containing the key
"type" and value "NEXUS". This could be expressed like below:

    publications[?locations[?type=='NEXUS']]

It is important to note, when writing conditions the expression starts
inside the aggregated object. This means we only have to traverse the JSON
structure inside the aggregated object.

If we want to trigger a subscription based on when an artifact has reached
the confidence level of 'SUCCESS' we can write the below expression for
our condition:

    confidenceLevels[?name=='dummy_1_stable'] && confidenceLevels[?value=='SUCCESS']

These two conditions check both that the artifact name is the right one,
and that the confidence level is what we are looking for.

### Repeat handling
See in the frontend documentation [here](https://github.com/eiffel-community/eiffel-intelligence-frontend/blob/master/wiki/markdown/add-subscription.md)
