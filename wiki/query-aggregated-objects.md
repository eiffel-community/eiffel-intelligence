# Query Aggregated Objects

|Method|Endpoint             |
|------|---------------------|
|GET   |/aggregated-objects|
|POST  |/aggregated-objects/query|


Query on aggregated objects can be performed using the above mentioned endpoint. 
There are two possibilities to query aggregated objects.

## Perform Query for Existing Aggregated Object Using Event Id
    GET /aggregated-objects

**URI parameters**:
\{documentID\}

Query through this option is more strict and the user must provide the id 
of the event that started the aggregated object.

Examples of this endpoint using curl with an a given id = 6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43

    curl -X GET -H "Content-type: application/json"  http://localhost:8090/aggregated-objects/6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43

## Perform Freestyle Query on Existing Aggregated Object
It is possible to make queries on aggregated objects using freestyle queries. 
These freestyle queries are plain Mongo DB queries, you can read more about 
that [here](https://docs.mongodb.com/manual/tutorial/query-documents/).

    POST /aggregated-objects/query

**Body (application/json)**:

    {
      "criteria": <MongoDB query>
      "options": <MongoDB query> // This one is optional
      "filter": <JMESPath expression> // This one is optional
    }

Examples of this endpoint using curl

    curl -X POST -H "Content-type: application/json"  --data @Body.json http://localhost:34911/aggregated-objects/query

### Examples of Criteria

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


## Example of Freestyle Query that Returns All Aggregated Objects
By using a query that contains only empty "criteria" it is possible to return
all aggregated objects from the database. The aggregated objects will be
returned from specific collection (which name is defined by property
aggregated.collection.name) that is stored in specific database (which name is
defined by property spring.data.mongodb.database). Read more about the
different properties in [application's properties](../src/main/resources/application.properties)
or in the [documentation](configuration.md).

Example:

    {
      "criteria": {}
    }

## Query an Aggregated Object and Filter It with Specific Key
It is possible to filter the result from the query and return only the values
which are of interest. This filter can be defined as a path in the aggregated
object, or a specific key. The parameter of filter condition is a JMESPath 
expression, you can read more about that
[here](http://jmespath.org/tutorial.html#pipe-expressions).

Example:

This matches an object in the same way as in the previous example. And then filters
it and returns eventId that has a path "publications[0].eventId".

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

This finds all objects where identity is "pkg:maven/com.mycompany.myproduct/sub-system@1.1.0".
Then it filters those objects and returns all values that has "gitIdentifier" as a key.

    {
      "criteria": {
         "identity":"pkg:maven/com.mycompany.myproduct/sub-system@1.1.0"
      },
      "filter" : "incomplete_path_filter(@, 'gitIdentifier')"
    }

As the filter functionality takes a plain JMESPath expression it can be a more
complex expression as well. In the case below we retrieve all aggregated objects
that contains a certain commit using the criteria(MongoDB query) and then filter
out which confidence levels the artifact has succeeded on. NOTE that the Mongo
DB query as well as the JMESPath expression needs to be on the same line as json
cannot handle multi-lines inside a string. Although some tools such as curl will
automatically minimize.

Example:

    {
        "criteria": {
            "$where": "function(){ var searchKey = 'id'; var value = 'JIRA-1234'; return searchInObj(obj); function searchInObj(obj){ for (var k in obj){ if (obj[k] == value && k == searchKey) { return true;  } if (isObject(obj[k]) && obj[k] !== null) { if (searchInObj(obj[k])) { return true;}}} return false; }}"
        },
        "filter": "{id: id, artifactIdentity: identity, confidenceLevels: confidenceLevels[?value=='SUCCESS'].{name: name, value: value}}"
    }
