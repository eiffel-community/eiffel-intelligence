# Subscriptions
When the desired Eiffel events have been aggregated a user would like to be
notified. It is possible to create a subscription which Eiffel Intelligence 
will match against the saved aggregations. 

Whenever an aggregated object is created or modified, it is evaluated against
all existing subscriptions to find out whether the aggregated object 
meets any subscription requirements. If it fulfills a subscription requirement 
then a notification is sent to the subscriber as specified in that subscription. 
For further explanation of the process, [consider the following example](step-by-step-subscription-notification.md).

### Adding Subscriptions
It is possible to add subscriptions using the [Eiffel Intelligence
front-end GUI](https://github.com/eiffel-community/eiffel-intelligence-frontend/blob/master/wiki/add-subscription.md).
It is also possible to manage subscriptions [using the REST API](subscription-API.md).

### Types of Notifications for Subscriptions
Today Eiffel Intelligence supports notifications by email or by sending
a HTTP POST request. Example subscriptions in JSON format with different types of notification
can be found in the below links:

* [Subscription with HTTP POST notification](subscription-with-REST-POST-notification.md)
* [Subscription with HTTP POST notification to trigger parameterized Jenkins job](triggering-jenkins-jobs.md)
* [Subscription with E-mail notification](subscription-with-email-notification.md)

The different fields in a subscription are also described in the 
[front-end documentation](https://github.com/eiffel-community/eiffel-intelligence-frontend/blob/master/wiki/add-subscription.md).

### Writing Requirements and Conditions
When writing subscription requirements, they are referencing the structure
of the aggregated object, which Eiffel Intelligence will create, based on
the rules it was configured with. It is therefore important to know what
the aggregated object will look like when writing the conditions. The 
requirements inside a subscription should reflect a wanted state of the 
aggregation, for example: "_Hey Eiffel Intelligence, let me know when my 
artifact has been published here!_" or "_I want to know when these two products 
have reached confidence level X_". 

We will go through how to write some example requirements in a subscription
based on the below aggregated object. What you see in the aggregation is 
the extracted content from Eiffel events. Aside from that Eiffel Intelligence 
has also added an extra _id key with the value of the start event. 
This _id serves as an index in Mongo DB to speed up searches in the database. 
The below aggregation is based on the [Artifact rules](../src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json).

It should be noted that requirements in subscriptions are written 
by using [JMESPath](https://jmespath.org/) language.

**Aggregation from several Eiffel events:**

    {
            "_id" : "df4cdb42-1580-4cff-b97a-4d0faa9b2b22",
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

In order for a subscription to be triggered, the subscription requirement
needs to be fulfilled. The requirements can be grouped with one or several
conditions inside them. Each requirement group is separated with an 'OR',
while the conditions inside a requirement are connected with an 'AND'. This
means all the conditions in a requirement group must be fulfilled, while
only one of the requirement groups needs to be fulfilled, for the
subscription to trigger. 

Let's say we need a subscription which is triggered on when an artifact
has been published to a location of type "NEXUS". This subscription wants
to listen for when an Eiffel ArtifactPublished event is aggregated by
Eiffel Intelligence.

Conditions in subscriptions are written in [JMESPath syntax](/rules.md#What-is-JMESPath?). 
The JSON structure of the subscription condition always contains a key 
"jmespath" and the value is a JMESPath expression. In the example below, 
we want to know when Eiffel Intelligence has aggregated information about 
an artifact being published in Nexus. A subscription with only this condition 
will be fulfilled as soon as Eiffel Intelligence aggregates data from an 
ArtifactPublished event with the correct data. 

    "requirements" : [
        {
            "conditions" : [
                {
                    "jmespath": "publications[?locations[?type=='NEXUS']]"
                }
            ]
        }
    ]

If we instead want to trigger a subscription based on when an artifact has 
reached the confidence level of 'SUCCESS' we can write the below expression 
for our condition:

    {
        "jmespath": "confidenceLevels[?name=='dummy_1_stable'] && confidenceLevels[?value=='SUCCESS']"
    }

the exact same condition can also be written like below:

    {
        "jmespath": "confidenceLevels[?name=='dummy_1_stable' && value=='SUCCESS']"
    }

If you want to include both of these conditions in the same subscription, 
(i.e. you want to know when the aggregation contains information about both
published artifact locations AND have reached the specified confidence 
level) the subscription will look like the below json structure:

    "requirements" : [
        {
            "conditions" : [
                {
                    "jmespath": "publications[?locations[?type=='NEXUS']]"
                },
                {
                    "jmespath": "confidenceLevels[?name=='dummy_1_stable' && value=='SUCCESS']"
                }
            ]
        }
    ]

This subscription now contains the requirement with two conditions: 1) the 
artifact name is the right one AND 2) the confidence level is what we are 
looking for. If we instead would like the subscription to check for the 
first condition OR the second one, the syntax would look like below:

    "requirements": [
        {
            "conditions": [
                {
                    "jmespath": "publications[?locations[?type=='NEXUS']]"
                }
            ]
        },
        {
            "conditions": [
                {
                    "jmespath": "confidenceLevels[?name=='dummy_1_stable' && value=='SUCCESS']"
                }
            ]
        }
    ]

This subscription describes two requirements and as soon as one of them 
has been fulfilled, Eiffel Intelligence would perform a notification.
More subscription templates [can be found here](../src/main/resources/templates).

### Repeat Handling
See in the front-end documentation [here](https://github.com/eiffel-community/eiffel-intelligence-frontend/blob/master/wiki/add-subscription.md)

### Adding Comments in Subscription

It is possible for users to add comments, at any level, in subscriptions. These
comments should be a new field in the given subscription, following the json format.
They will not be seen in the front-end GUI but only in the JSON structure if 
downloaded from Eiffel Intelligence.

    {
        "description": "It is my comment"
    }

### Using JMESPath in Subscriptions

Eiffel Intelligence supports several pre-defined methods in the JMESPath 
expressions inside a subscription. For example it is possible to include
the entire aggregation (or parts of it) in the notification Eiffel 
Intelligence sends when a subscription is fulfilled.

If Eiffel Intelligence should send a HTTP POST request somewhere with the 
complete aggregation it could look like this:

    "restPostBodyMediaType": "application/json",
    "notificationMeta": "some-external-api/endpoint"
    "notificationMessageKeyValues": [
        {
            "formkey": "external-api-parameter-name",
            "formvalue": "@"
        }
    ],

The form key should match the external API to which Eiffel Intelligence 
will send the notification of a fulfilled subscription. The form value 
will be run through JMESPath engine so it is possible to use JMESPath 
expressions to extract content from the aggregated object. The form value 
can only be one JSON object. In the above example the full aggregation can 
be sent using the '@' character. To access parts of the aggregation it is 
possible to define like the below examples:

    @.identity         // represents the identity string from the aggregation
    @.some_array       // it is possible to extract an array from the aggregation
