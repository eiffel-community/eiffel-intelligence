# Rules

## What is JMESPath?

JMESPath is a query language for JSON. You can extract and transform elements
from a JSON document. The result of applying a JMESPath expression against a
JSON document will always result in valid JSON, provided there are no errors
during the evaluation process. This also means that, with the exception of
JMESPath expression types, JMESPath only supports the same types support by
JSON:

    number (integers and double-precision floating-point format in JSON)
    string
    boolean (true or false)
    array (an ordered, sequence of values)
    object (an unordered collection of key value pairs)
    null

Let's go to an example of simple query called identifier. An identifier is the
most basic expression and can be used to extract a single element from a JSON
object. The return value for an identifier is the value associated with the
identifier. If the identifier does not exist in the JSON document, than a null
value is returned. Assume that we have such JSON object
{"a": {"b": {"c": {"d": "value"}}}} and we need to get the string "value" from
object. The JMESPath identifier will be a.b.c.d. [JMESPath identifier documentation](http://jmespath.org/specification.html#identifiers).

### Extract data from Eiffel event

        {
            "meta": {
                "id": "e3be0cf8-2ebd-4d6d-bf5c-a3b535cd084e",
                "type": "EiffelConfidenceLevelModifiedEvent",
                "version": "1.1.0",
                "time": 1521452400324,
                "tags": [],
                "source": {
                    "serializer": "pkg:maven/com.mycompany.tools/eiffel-serializer@1.0.3",
                    "uri": "http://host:port/path"
                }
            },
            "data": {
                "name": "dummy_1_stable",
                "value": "SUCCESS"
            },
            "links": [
                {
                    "type": "SUBJECT",
                    "target": "df4cdb42-1580-4cff-b97a-4d0faa9b2b22"
                }
            ]
        }

Lets go through an example, in which we need to extract the value "SUCCESS"
from an Eiffel event. The expression for such a JSON query will be
"data.value".

Eiffel Intelligence uses JMESPath for extracting information from events and
for post processing of data in an aggregated object. Hence, JMESPath is used
to traverse JSON structures when writing rules, subscription requirements or
querying an aggregated object.

## Rule set up

Rules for object aggregation consist of JSON object with a defined structure
inside it. Key in this object is rule specification and value is JMESPath
identifier. Separate rule set is created for each event type that is going to
take participation in creating of aggregated object. This means that if you
want to create your aggregated object from 3 event types and all other event
types to discard, you will need 3 set of rules. Each rule might contain some of
these keys, that have not null or empty values. Explicit list of rule keys are
below:

### TemplateName 
used for specifying a template group, any string you like to
name your template

### Type 
Eiffel event type, which will be used to find the matching rule set
for the received Eiffel event while creating aggregated object. Example:
"EiffelConfidenceLevelModifiedEvent"

### TypeRule 
JMESPath identifier for the location of Eiffel event type in
received Eiffel event. Example: "meta.type".

### IdRule 
JMESPath identifier for the location of Eiffel event id in
received Eiffel event. Example: "meta.id". Used as fall back when storing in
database and no id is provided or to link id of aggregated objects to events
that contributed to the aggregated object.

### StartEvent
denotes if this event type starts the object aggregation. If
StartEvent is "YES" then it will be the first processed event in the aggregation
sequence. If StartEvent is "NO" then Eiffel event of this type will be used to
append information to existing aggregated object. If no aggregated object exist
then it will wait a certain time as defined by property
 _waitlist.collection.ttlValue_ in [application's properties](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/application.properties)
 until wanted aggregated object has been created. If no aggregated object is
 created in time then the event will no longer be processed and it will be
 removed from the wait list.

### IdentifyRules
JMESPath identifier of ids that will be used to search for
an existing aggregated object. Should produce an JSON array, for example
    "[meta.id]"

which will return the specified field in array

    ["sb6e51h0-25ch-4dh7-b9sd-876g8e6kde47"].

Another common example is

    "links | [?type=='CAUSE'].target"

 which will extract the event id from links array value of target, where "type"
 is equal "CAUSE". Links in an Eiffel event look like

    {
       "links":[
          {
             "target":"f37d59a3-069e-4f4c-8cc5-a52e73501a75",
             "type":"CAUSE"
          },
         {
             "target":"cfce572b-c3j4-441e-abc9-b62f48080ca2",
             "type":"ELEMENT"
         }
      ]
    }

and applying the above links extraction rule will give ["f37d59a3-069e-4f4c-8cc5-a52e73501a75"]

If ids of all links is needed then

    links | [].target

will return

    [
     "f37d59a3-069e-4f4c-8cc5-a52e73501a75",
     "cfce572b-c3j4-441e-abc9-b62f48080ca2"
    ]

### MatchIdRules 
This rule is used to search in database containing the
aggregated objects. The syntax of this rule is specific to querying in Mongo DB.
Use marker "%IdentifyRules_objid%" to inject the ids generated by
**_IdentifyRules_**. Examples:

    {"_id": "%IdentifyRules_objid%"},
    { "$and": [{"testCaseExecutions.testCaseStartedEventId": "%IdentifyRules%"}]}

### ExtractionRules
JSON object of JMESPath identifier(s) which will create
or modify the existing data in aggregated object. For a start event the content
extracted with this rule will be the initial aggregated object.

For example

    "{confidenceLevels :[{ eventId:meta.id, time:meta.time, name:data.name, value:data.value}]}"

will create JSON object "confidenceLevels" and the value of it will be array
with 1 JSON element. This one key in this JSON object will be "eventId" and its
value will be the result of searching for "meta.id" identifier in the received
Eiffel event, for example "cfce572b-c3j4-441e-abc9-b62f48080ca2" and so on.

Evaluation of specified extraction rules on the received Eiffel event may be
the JSON object

    {
      "confidenceLevels":[
        {
         "eventId":"f37d59a3-069e-4f4c-8cc5-a52e73501a75",
         "name":"readyForDelivery",
         "time":1481875944272,
         "value":"SUCCESS"
        }
     ]
    }

which could be added to the root of aggregated object or to the inner structure
of aggregated object depending on **_MergeResolverRules_**.

### MergeResolverRules
This is a JSON object describing the entire path or parts of the path to where 
the extracted content will be stored. More detailed explanation can be found [here](merge-resolver-rules.md)

### ProcessRules 
This rule is used to append (_only additions or modifications,
no deletions, of existing content at time rule is applied_) new key value pairs
in the aggregated object based on existing values in the aggregation object.
For example if you have aggregated the job finished time and job started time
then you can create one more value for the computed duration so that other
systems do not need to compute it.

### History Rules 
If your start event, meaning the event that starts the chain of aggregation, 
contains links that point to upstream events, HistoryRules makes it 
possible for these to be aggregated into the object as well. They are 
explained [**here**](history-rules.md).

## Reserved Key names
The keys "_id" and "time" (at the root level of the aggregated object) are 
reserved and added in the Mongo DB document by Eiffel Intelligence. User should 
not use these two keys at the root level of an aggregated object. Even if 
user add these two keys at the root level, Eiffel Intelligence will overwrite those.
