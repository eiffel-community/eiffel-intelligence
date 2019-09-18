# Query

|Method|Endpoint             |
|------|---------------------|
|GET   |/queryAggregatedObject|
|POST   |/query|
|GET   |/queryMissedNotifications|

## Perform query on created aggregated object
    GET /queryAggregatedObject

**Query parameters**:
ID=\<documentID\>

Examples of this endpoint using curl

    curl -X GET -H "Content-type: application/json"  http://localhost:8090/queryAggregatedObject?ID=6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43

## Perform freestyle query on created aggregated object
It is possible to make queries on aggregated objects using freestyle queries. These freestyle
queries are plain Mongo DB queries, you can read more about that [here](https://docs.mongodb.com/manual/tutorial/query-documents/).

    POST /query

**Body (application/json)**:

    {
      "criteria": <MongoDB query>
      "options": <MongoDB query> // This one is optional
      "filter": <JMESPath expression> // This one is optional
    }

Examples of this endpoint using curl

    curl -X POST -H "Content-type: application/json"  --data @Body.json http://localhost:34911/query

Examples of criterias:

    // This returns all objects where the testCaseExecutions.outcome.id is"TC5"
    // or testCaseExecutions.outcome.id is "TC6" and "identity"
    // is "pkg:maven/com.mycompany.myproduct/sub-system@1.1.0".

    {
      "criteria": {
        "$or": [
          {
            "testCaseExecutions.outcome.id":"TC5"
          }, {
            "testCaseExecutions.outcome.id":"TC6"
          }
        ],
        "identity":"pkg:maven/com.mycompany.myproduct/sub-system@1.1.0"
      }
    }


    // This returns all objects where TC5 succeeded in
    // the array testCaseExecutions and identity is "pkg:maven/com.mycompany.myproduct/sub-system@1.1.0".
    // Options section is optional.

    {
      "criteria": {
        "testCaseExecutions": {
          "$elemMatch": {
            "outcome.conclusion": "SUCCESSFUL",
            "outcome.id": "TC5"
          }
        }
      },
      "options": {
        "identity":"pkg:maven/com.mycompany.myproduct/sub-system@1.1.0"
      }
    }


    // This returns all objects where TC5 succeeded
    // and are related to JIRA ticket JIRA-1234
    {
        "criteria": {
             "testCaseExecutions": {
                 "$elemMatch": {
                     "outcome.conclusion": "SUCCESSFUL",
                     "outcome.id": "TC5"
                 }
             },
             "$where" : "function() {
                 var searchKey = 'id';
                 var value = 'JIRA-1234';
                 return searchInObj(obj);
                 function searchInObj(obj){
                     for (var k in obj){
                         if (obj[k] == value && k == searchKey) {
                             return true;
                         }

                         if (isObject(obj[k]) && obj[k] !== null) {
                             if (searchInObj(obj[k])) {
                                 return true;
                             }
                         }
                     }
                     return false;
                 }
             }"
        }
    }


## Example of freestyle query that returns all aggregated objects
By using a query that contains only empty "criteria" it is possible to return
all aggregated objects from the database. The aggregated objects will be
returned from specific collection (which name is defined by property
aggregated.collection.name) that is stored in specific database (which name is
defined by property spring.data.mongodb.database). Read more about the
different properties in [application's properties](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/application.properties)
or in the [documentation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/configuration.md).

Example:

    {
      "criteria": {}
    }


## Query an aggregated object and filter it with specific key
It is possible to filter the result from the query and return only the values
which are of interest. This filter can be defined as a path in the aggregated
object, or a specific key. The parameter of filter condition is a JMESPath 
expression, you can read more about that
[here](http://jmespath.org/tutorial.html#pipe-expressions).

Example:

    // This match an object in the same way as in the previous example. And then filter
    // it and returns eventId that has a path "publications[0].eventId".

    {
      "criteria": {
        "testCaseExecutions": {
          "$elemMatch": {
            "outcome.conclusion": "SUCCESSFUL",
            "outcome.id": "TC5"
          }
        }
      },
      "options": {
        "identity":"pkg:maven/com.mycompany.myproduct/sub-system@1.1.0"
      },
      "filter" : "publications[0].eventId"
    }


To filter with only the key or the partial path, it is required to use
"incomplete_path_filter(@, 'some key')".

Example:

    // This finds all objects where identity is "pkg:maven/com.mycompany.myproduct/sub-system@1.1.0".
    // Then it filters those objects and returns all values that has "gitIdentifier" as a key.

    {
      "criteria": {
         "identity":"pkg:maven/com.mycompany.myproduct/sub-system@1.1.0"
      },
      "filter" : "incomplete_path_filter(@, 'gitIdentifier')"
    }

As the filter functionality takes a plain jmespath expression it can be a more
complex expression as well. In the case below we retrieve all aggregated objects
that contains a certain commit using the criteria(MongoDB query) and then filter
out which confidence levels the artifact has succeeded on. NOTE that the Mongo
DB query as well as the jmespath expression needs to be on the same line as json
cannot handle multilines inside a string. Although some tools such as curl will
automatically minimize.

Example

    {
        "criteria": {
            "$where": "function(){ var searchKey = 'id'; var value = 'JIRA-1234'; return searchInObj(obj); function searchInObj(obj){ for (var k in obj){ if (obj[k] == value && k == searchKey) { return true;  } if (isObject(obj[k]) && obj[k] !== null) { if (searchInObj(obj[k])) { return true;}}} return false; }}"
        },
        "filter": "{id: id, artifactIdentity: identity, confidenceLevels: confidenceLevels[?value=='SUCCESS'].{name: name, value: value}}"
    }

## Query missed notifications
    GET /queryMissedNotifications

**Query parameters**:
SubscriptionName=<Subscription Name/Subscription ID>

Examples of this endpoint using curl

    curl -X GET -H "Content-type: application/json" localhost:39835/queryMissedNotifications?SubscriptionName=Subscription_1

The content of a failed notification json object may look something like the following

    {
        "subscriptionName": "RestFailureTestSubscription",
        "aggregatedObject": {
            ...
        },
        "notificationMeta": "http://localhost:9999/some-endpoint",
        "_id": {
            "$oid": "5d807a1d821b960af311fab3"
        },
        "time": {
            "$date": "2019-09-17T06:15:57.000Z"
        },
        "message": "Failed to send REST/POST notification!\nMessage: I/O error on POST request for \"http://localhost:9999/some-endpoint\": Connect to localhost:9999 [localhost/127.0.0.1] failed: Connection refused (Connection refused); nested exception is org.apache.http.conn.HttpHostConnectException: Connect to localhost:9999 [localhost/127.0.0.1] failed: Connection refused (Connection refused)"
    }

It contains the name of the subscription that failed, a snapshot of the aggregated object that triggered the subscription, the notification meta which is the mail or service endpoint to contact, the time of occurence and the message that contains the exception that was thrown.
This information is meant to help you figure out why the notification failed. In the example above the reason is that the notification meta URL is invalid and because of this could not establish a connection.