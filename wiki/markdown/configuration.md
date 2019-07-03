# Configuration

Configuration for the message bus, MongoDB and other settings for
Eiffel Intelligence needs to be defined in [application.properties](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/main/resources/application.properties)
for the application to run properly.

## Setting up multiple EI instances

Eiffel Intelligence is designed to be able to collect different information
in different objects. What information to be collected from what events
to what object is configured using a set of rules.

### Set up multiple instances with different rule sets in each instance

In this case we use the same instance of RabbitMQ and MongoDB.

  - **rabbitmq.consumerName** property should be different for each rule set. Otherwise the rabbitMQ will split the events in the queue among all the instances listening to that queue.
  - MongoDb collection names should also be different for each rule set.

  <img src="images/multiple_EI_instances.png">
</img>

### Set up multiple instances with same rule set

This situation may be needed when the events throughput is very high. In this case the same configuration file is copied to the server where the extra instance will be started.

  <img src="images/multiple_EI_instances_same_rule.png">
</img>

## Configure Eiffel Intelligence with extraction rules for specific Eiffel protocol version

Extraction rules for a specific Eiffel protocol versions is configured by
setting "rules.path" property in [application.properties](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/main/resources/application.properties)
to point to the correct extraction rules json file.

Eiffel Intelligence provides default extractions rules json files for
different Eiffel protocol versions inside the war artifact file and in the
source code repository. All default extraction rules json files can be
found here: [extraction rules](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/main/resources/rules)

Example of setting "rules.path" property in application.properties using default extraction rules json files provided in eiffel-intelligence war file:
- rules.path: /rules/ArtifactRules-Eiffel-Agen-Version.json
- rules.path: /rules/ArtifactRules-Eiffel-Toulouse-Version.json
- rules.path: /rules/SourceChangeObjectRules-Eiffel-Toulouse-Version.json

It is possible to use external provided extraction rules by providing the full path or a URI.
URI schemes that are accepted are 'file','http' and 'https'.
Examples of setting "rules.path" property to an external rules file:
- rules.path: /full/path/to/ExtractionRules.json
- rules.path: file:///full/path/to/ExtractionRules.json
- rules.path: file://localhost/full/path/to/ExtractionRules.json
- rules.path: http://somehost.com/full/path/to/ExtractionRules.json
- rules.path: https://somehost.com/full/path/to/ExtractionRules.json

## Configuring aggregations

Eiffel Intelligence saves aggregated objects in a database. It is possible
to configure the collection name, the time to live and the name of the
aggregated object using the below properties:

* aggregated.object.name
* aggregated.collection.name

If Eiffel Intelligence is set up with [all_event_rules](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/all_event_rules.json)
it is recommended to set a time to live value to avoid having a copy of
Event repository. Recommended settings is 10 minutes.

* aggregated.collection.ttlValue (*seconds*)

When performing queries towards an aggregated object, **search.query.prefix**
is used to access the aggregated object when using JMESPath expressions.

Consider the following query:

    {
      "criteria": {
        "object.identity":"pkg:maven/com.mycompany.myproduct/sub-system@1.1.0"
      }
    }

Eiffel Intelligence will replace the prefix "object." with the name of the aggregated
object (in this case "aggregatedObject"), which is defined by **aggregated.object.name**.
Read more examples about [queries here](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/query.md).


### Testing aggregation rules

To test new rules for Eiffel Intelligence the property **testaggregated.enabled**
can be set to true. This gives users the possibility to try out different rule sets
on a specific set of Eiffel events and see the resulting aggregated object.


### Wait list

If Eiffel Intelligence receives events that are not connected to any
existing aggregated objects, and it is not declared as start event in the
rules, the event is stored in the wait list database for a while. The
"time-to-live" value limits how long an event is stored in the wait list.

* waitlist.collection.name
* waitlist.collection.ttlValue (*seconds*)

It is possible to configure how often Eiffel Intelligence should handle
unprocessed events lying in the wait list. These properties are in **milliseconds**:

* waitlist.initialDelayResend (*milliseconds*)
* waitlist.fixedRateResend (*milliseconds*)


## Subscriptions

Eiffel Intelligence stores subscriptions in a database with the collection
name configured with the property **subscription.collection.name**. When
subscriptions have fulfilled their requirements and notifications
should not be repeated, the subscription is stored together with the
matched aggregated object in the database. The name of this particular
collection is defined by the below property:

* subscription.collection.name
* subscription.collection.repeatFlagHandlerName


### Notifications

**email.sender** defines who should be the sender of the email Eiffel
Intelligence sends when a subscription is triggered. If a subscription is
defined with the notificationMeta "receivermail@example.com" this receiver
would be notified when the subscription fulfills all conditions via an E-mail.
The sender of this e-mail is defined by the **email.sender** property.
The value for email.subject is used by all subscriptions which have been
created via the REST API. When a subscription is created using the Eiffel
Intelligence front-end GUI it is possible to set individual email subject
for each subscription.

* email.sender
* email.subject


### Missed notifications

Should the subscription notification for some reason fail, the notification
is stored in a database, and **missedNotificationDatabaseName** property
defines what that database should be called. It is also possible to configure
what the collection name for missed notifications should be. **notification.ttl.value** tells
Eiffel Intelligence how long a missed notification will be stored in the
database before deletion. With **notification.failAttempt** property, it
is possible to configure the number of attempts Eiffel Intelligence will
retry to make a REST POST notification when a subscription is triggered.

* missedNotificationDatabaseName
* missedNotificationCollectionName
* notification.ttl.value
* notification.failAttempt


### Configure search in Event repository

For Eiffel Intelligence to search for linked events Event repository is
used. **er.url** takes a full URL to such a repository.

* er.url
