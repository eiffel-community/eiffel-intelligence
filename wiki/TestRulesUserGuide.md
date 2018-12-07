# Test Rules User Guide

<h3>Rule testing mechanism via "Test Rules" GUI interface</h3>

<p>
The graphical user interface for testing rules consists of two panes. Left pane 
is intended to upload the rule. To do this one will need to chose the rule type 
from dropdown menu, then insert the rule content and upload the rule. Right 
pane is intended for uploading the Eiffel events. After uploading the events 
one may check the result of applying rules by pressing "Find Aggregated Object" 
button specifying the UUID of this object.
</p>

<img src="https://github.com/eiffel-community/eiffel-intelligence-frontend/blob/master/src/main/resources/static/assets/images/GUI_frontend_example.png">
</img>

<h3>Introduction</h3>

<p>
JMESPath is a query language for JSON. You can extract and transform elements 
from a JSON document. The result of applying a JMESPath expression against a 
JSON document will always result in valid JSON, provided there are no errors 
during the evaluation process. This also means that, with the exception of 
JMESPath expression types, JMESPath only supports the same types support by 
JSON:
</p>

<ul>
    <li>number (integers and double-precision floating-point format in JSON)</li>
    <li>string</li>
    <li>boolean (true or false)</li>
    <li>array (an ordered, sequence of values)</li>
    <li>object (an unordered collection of key value pairs)</li>
    <li>null</li>
</ul>

<p>
Let's go to an example of simple query called identifier. An identifier is the 
most basic expression and can be used to extract a single element from a JSON 
object. The return value for an identifier is the value associated with the 
identifier. If the identifier does not exist in the JSON document, than a null 
value is returned. Assume that we have such JSON object 
<code>{"a": {"b": {"c": {"d": "value"}}}}</code> and we need to get the string
<code>"value"</code> from object. The JMESPath identifier will be 
<code>a.b.c.d</code>. <a href="http://jmespath.org/specification.html#identifiers">JMESPath identifier documentation</a>.
</p>

<p>
Lets do some more complicated example on Eiffel event where we need to extract 
the <code>"SUCCESS"</code> from the event. The identifier for such JSON query 
will be <code>data.value</code>
</p>

<h3>Aggregated object</h3>

<p>
The term "Aggregated object" in Eiffel syntax means a composition of several 
Eiffel events into one big JSON object using a special rule mechanism. The main 
purpose for aggregating objects is to create customizable JSON documents which 
could later be used for visualization and better tracing using different 
visualization tools. Inside the rule mechanism the JMESPath is used for 
selecting the needed data from Eiffel event and inserting it.
</p>

<h3>Rule set up</h3>

<p>
The rules for object aggregation consist of a JSON object with a defined 
structure inside it. The key in this object is a rule specification and the 
value is a JMESPath identifier. A separate rule is created for each event 
type that is going to take part in the creation of the aggregated object. This 
means that if you want to create your aggregated object from 3 event types and 
then all other event types are ignored. Each rule might contain some of the 
keys listed below:
</p>


<ul>
    <li><code>"TemplateName"</code> - Used for specifying a template group, any string you like to name your template</li>
    <li><code>"Type"</code> - Eiffel event <a href="https://github.com/Ericsson/eiffel/tree/master/eiffel-vocabulary">type</a>,
        which will be used to find the matching template while creating the aggregated object.
        Example: <code>"EiffelConfidenceLevelModifiedEvent"</code>
    </li>
    <li><code>"TypeRule"</code> - JMESPath identifier for the location of Eiffel event type in the received Eiffel
        event. Example: <code>"meta.type"</code>.
    </li>
    <li><code>"IdRule"</code> - JMESPath identifier for the location of Eiffel event id in received Eiffel event.
        Example: <code>"meta.id"</code>.
    </li>
    <li><code>"StartEvent"</code> - Denotes to start the object aggregation. If StartEvent is <code>"YES"</code> then
        it will be the first processed event in the aggregation sequence. If StartEvent is <code>"NO"</code> then the
        Eiffel event is part of a sequence and will be stored until the sequence has started.
    </li>
    <li><code>"MatchIdRules"</code> - Denotes the received event id of the object. Example: <code>"%IdentifyRules_objid%"</code></li>
    <li><code>"IdentifyRules"</code> - JMESPath identifier of the event id that will be used in the aggregated object.
        Should produce an JSON array,
        for example <code>"[meta.id]"</code> which will return the specified field in an array
        <code>["sb6e51h0-25ch-4dh7-b9sd-876g8e6kde47"]</code>.
        Another common example is
        <code>"links | [?type=='CAUSE'].target"</code> which will extract the event id from the <code>links</code>
        array value of
        <code>target</code>, where "type" is equal to "CAUSE".
        Link looks like <code>{"links": [
                {
                "target": "f37d59a3-069e-4f4c-8cc5-a52e73501a75",
                "type": "CAUSE"
                },
                {
                "target": "cfce572b-c3j4-441e-abc9-b62f48080ca2",
                "type": "ELEMENT"
                }
                ] }</code>
    </li>
    <li><code>"ExtractionRules"</code> - JSON object of JMESPath identifier(s) which will create or modify the existing
        data in the aggregated object.
        For example <code>"{confidenceLevels :[{ eventId:meta.id, time:meta.time, name:data.name, value:data.value}]}"</code>
        will create
        JSON object "confidenceLevels" and the value of it will be an array with 1 JSON element. This JSON object key
        will
        be "eventId" and the value
        will be the result of searching for "meta.id" identifier in the received Eiffel event,
        for example <code>"cfce572b-c3j4-441e-abc9-b62f48080ca2"</code> and so on.
        The result of the above specified extraction rules could look something like the following JSON object
        <code>{"confidenceLevels":[{"eventId":"f37d59a3-069e-4f4c-8cc5-a52e73501a75",
                "name":"readyForDelivery","time":1481875944272,"value":"SUCCESS"}]}</code> which could be added to the
        root of the aggregated object or
        to the inner structure of the aggregated object depending on MergeResolverRules.
    </li>
    <li><code>"MergeResolverRules"</code> - JMESPath identifier of the place to insert the JSON object from
        "ExtractionRules".
        If MergeResolverRules is <code>null</code> ExtractionRules object will be inserted to the root of the
        aggregated
        object.
        An example is <code>"[{NONEPATH:NONE}, {test_suite: [{ test_case : [{ trigger_event_id:meta.id}]} ]} ]"</code>
        which consists of an
        array with two fields, first is the expression <code>{NONEPATH:NONE}</code> that <strong>should not exist</strong>
        in the aggregated object,
        it could be any words you like.
        Second part is <code>{test_suite: [{ test_case : [{ trigger_event_id:meta.id}]}</code> that will form a new
        JSON object that will be
        inserted to the aggregated object after evaluation of "meta.id".
        Another example could be inserting the object obtained in "ExtractionRules" to the array element that has <code>trigger_event_id</code>
        matching the event id target field from incoming event "TEST_CASE_EXECUTION" link JSON object.
    </li>
    <li><code>"HistoryIdentifyRules"</code> - JMESPath identifier of event id that will be used in the aggregated
        object
        for internal composition. Follows the same instructions as "IdentifyRules".
    </li>
    <li><code>"HistoryExtractionRules"</code> - JSON object of JMESPath identifier(s) which will create or modify the
        existing data in the aggregated
        object for internal composition. Follows the same instructions as "ExtractionRules".
    </li>
    <li><code>"HistoryPathRules"</code> - JMESPath identifier of the place where to insert the JSON object form
        "ExtractionRules" in the aggregated
        object for internal composition. Follows the same instructions as "MergeResolverRules".
    </li>
    <li><code>"DownstreamIdentifyRules"</code> - JMESPath identifier of event id that will be used in the aggregated
        object
        for external composition. Follows the same instructions as "IdentifyRules".
    </li>
    <li><code>"DownstreamExtractionRules"</code> - JSON object of JMESPath identifier(s) which will create or modify
        the existing data in
        the aggregated object for external composition. Follows the same instructions as "ExtractionRules".
    </li>
    <li><code>"DownstreamMergeRules"</code> - JMESPath identifier of the place where to insert the JSON object form
        "ExtractionRules" in
        the aggregated object for external composition. Follows the same instructions as "MergeResolverRules".
    </li>
    <li><code>"ProcessRules"</code> - Allows modifying of the aggregated object obtained after processing of previously
        mentioned rules.
        This rule will be executed in the end, right before storing the aggregated object to database, only has access
        to the aggregated object.
    </li>
    <li><code>"ProcessFunction"</code> - Reserved for future use.</li>
</ul>


<h3>The most common operation you would do</h3>
<h4>Creating new aggregated object from an Eiffel event</h4>

<p>
Lets consider that an aggregated object should be created upon receiving the 
EiffelArtifactCreated event, and it should form the aggregated object from the 
received event. The aggregated object should contain the keys "id", 
"artifactId", and "temp", where "id" will be extracted from the received event 
key "id", artifactId will be extracted from the event GAV and temp will be set 
to true.
</p>

EiffelArtifactCreatedEvent example, click to unfold

    {
        "meta":{
            "id":"66208918-4422-42cb-a0bf-17b15d1043d2",
            "type":"EiffelArtifactCreatedEvent",
            "version":"1.1.0",
            "time":1522326376538,
            "tags":[
            
            ],
            "source":{
                "domainId":"eiffel.aaa.bbb",
                "host":"eselivm2v1464l",
                "name":"fem-eiffel",
                "serializer":{
                    "groupId":"com.github.Ericsson",
                    "artifactId":"eiffel-remrem-semantics",
                    "version":"0.0.10"
                },
                "uri":"https://some.url.com:8443/jenkins/job/user_test/31/"
            }
        },
        "data":{
            "gav":{
                "groupId":"com.ericsson.test",
                "artifactId":"test_arm_test",
                "version":"0.0.31"
            },
            "fileInformation":[
                {
                    "classifier":"",
                    "extension":"txt"
                }
            ],
            "buildCommand":"",
            "requiresImplementation":"NONE",
            "dependsOn":[
        
            ],
            "implements":[
        
            ],
            "name":"",
            "customData":[
        
            ]
        },
        "links":[
        
        ]
    }


<p>
So we need to form a set of basic rules for the Eiffel event that will fulfill 
our aggregated object. Keep in mind that we are adding to the root of 
aggregated object so no need to specify MergeResolverRules.
</p>

    {
       "TemplateName":"ARTIFACT_1",
       "Type":"EiffelArtifactCreatedEvent",
       "TypeRule":"meta.type",
       "IdRule":"meta.id",
       "StartEvent":"YES",
       "IdentifyRules":"[meta.id]",
       "MatchIdRules":{
          "_id":"%IdentifyRules_objid%"
       },
       "ExtractionRules":"{ id : meta.id, artifactId : data.gav.artifactId, temp : `\"true\"` }"
    }

And here is the resulting aggregated object.

    {
       "_id":"b46ef12d-25gb-4d7y-b9fd-8763re66de47",
       "aggregatedObject":{
          "id":"b46ef12d-25gb-4d7y-b9fd-8763re66de47",
          "artifactId": "test_arm_test",
          "temp":"true",
          "TemplateName":"ARTIFACT_1"
       }
    }

<h4>Inserting a JSON array to the aggregated object</h4>

<p>
Lets consider that upon receiving an EiffelConfidenceLevelModifiedEvent message, 
confidence levels array should be inserted to the aggregated object. Each time 
such an eiffel message is received it should form a new element and be inserted 
into aggregated object. Rule example is below.
</p>

    {
       "TemplateName":"ARTIFACT_1",
       "Type":"EiffelConfidenceLevelModifiedEvent",
       "TypeRule":"meta.type",
       "IdRule":"meta.id",
       "StartEvent":"NO",
       "IdentifyRules":"links | [?type=='SUBJECT'].target",
       "MatchIdRules":{
          "_id":"%IdentifyRules_objid%"
       },
       "ExtractionRules":"{ eventId:meta.id, time:meta.time, name:data.name, value:data.value }",
       "MergeResolverRules":"[ {NONEPATH:NONE}, {confidenceLevels: [{ eventId: meta.id }]} ]"
    }

<p>
When receiving an EiffelConfidenceLevelModifiedEvent message the aggregated 
object <code>"id"</code> is located in the <code>links</code> object where the
array is located. Each element of the array is a valid JSON object. There are 
key-value pairs in this array, and in one of those there should be a key called
<code>"type"</code> with the value <code>"SUBJECT"</code> and we need to extract 
the value of <code>"target"</code> which will be our id for the aggregated 
object. The identifier described is placed in 
<code>"IdentifyRules":"links | [?type=='SUBJECT'].target"</code>.
At this point we know the needed aggregated object id and now we need to specify 
the desired location for the confidence level information. Lets move the data 
obtained in "ExtractionRules" under the <code>"confidenceLevels"</code> key in 
the root of the aggregated object and the value should be an array. The 
identifier is expressed as 
<code>{confidenceLevels: [{ eventId: meta.id }]}</code>. To insert the new 
element to an array we need to provide an expression that won't be found in 
the existing aggregated object (if the provided identifier is found in the 
aggregated object, it will modify the existing element). In our case 
<code>{NONEPATH:NONE}</code> does not exist in the aggregated object so we can 
use that. The full expression would then be 
<code>"MergeResolverRules":"[ {NONEPATH:NONE}, {confidenceLevels: [{ eventId: meta.id }]} ]"</code>.
</p>


EiffelConfidenceLevelModifiedEvent for example, click to unfold

    {
       "links":[
          {
             "target":"40df7fc4-0c40-40a3-a349-9f6da0ba81c5",
             "type":"CAUSE"
          },
          {
             "target":"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
             "type":"SUBJECT"
          }
       ],
       "meta":{
          "id":"f37d59a3-069e-4f4c-8cc5-a52e73501a76",
          "source":{
             "domainId":"example.domain"
          },
          "time":1481875988767,
          "type":"EiffelConfidenceLevelModifiedEvent",
          "version":"1.0.0"
       },
       "data":{
          "value":"SUCCESS",
          "customData":[
             {
                "value":"CLM2",
                "key":"name"
             },
             {
                "value":1,
                "key":"iteration"
             }
          ],
          "name":"performance"
       }
    }

Aggregated object before receiving EiffelConfidenceLevelModifiedEvent click to unfold
    
    {
       "_id":"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
       "aggregatedObject":{
          "id":"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
          "type":"EiffelArtifactCreatedEvent",
          "time":1481875891763,
          "gav":{
             "artifactId":"sub-system",
             "version":"1.1.0",
             "groupId":"com.mycompany.myproduct"
          },
          "fileInformation":[
             {
                "classifier":"debug",
                "extension":"jar"
             },
             {
                "classifier":"test",
                "extension":"txt"
             },
             {
                "classifier":"application",
                "extension":"exe"
             }
          ],
          "buildCommand":null,
          "TemplateName":"ARTIFACT_1"
       }
    }

Aggregated object after receiving EiffelConfidenceLevelModifiedEvent click to unfold

    {
       "_id":"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
       "aggregatedObject":{
          "fileInformation":[
             {
                "extension":"jar",
                "classifier":"debug"
             },
             {
                "extension":"txt",
                "classifier":"test"
             },
             {
                "extension":"exe",
                "classifier":"application"
             }
          ],
          "buildCommand":null,
          "confidenceLevels":[
             {
                "eventId":"f37d59a3-069e-4f4c-8cc5-a52e73501a76",
                "name":"performance",
                "time":1481875988767,
                "value":"SUCCESS"
             }
          ],
          "TemplateName":"ARTIFACT_1",
          "id":"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
          "time":1481875891763,
          "type":"EiffelArtifactCreatedEvent",
          "gav":{
             "groupId":"com.mycompany.myproduct",
             "artifactId":"sub-system",
             "version":"1.1.0"
          }
       }
    }


<h4>Updating an array in JSON objects where the a field is pointing to Eiffel message id</h4>

<p>
Lets consider that upon receiving an EiffelTestCaseStartedEvent message we need 
to update the aggregated object. The event message has a key 
<code>"TEST_CASE_EXECUTION"</code> that contains an id that matches the one 
stated in the aggregated object and the data in that JSON object needs to be 
updated. As a result of applying the rule, the key <code>ongoing</code> in the 
aggregated object will change the value from <code>"ongoing":"false"</code>
to <code>"ongoing":"true"</code>.
</p>


Rules for EiffelTestCaseStartedEvent

    {
       "TemplateName":"TEST_EXECUTION_1",
       "Type":"EiffelTestCaseStartedEvent",
       "TypeRule":"meta.type",
       "IdRule":"meta.id",
       "StartEvent":"NO",
       "IdentifyRules":"links | [?type=='TEST_CASE_EXECUTION'].target",
       "MatchIdRules":{
          "_id":"%IdentifyRules_objid%"
       },
       "ExtractionRules":"{ ongoing : `\"true\"`}",
       "MergeResolverRules":"[{NONEPATH:NONE}, {test_suite: [{ test_case : [{ trigger_event_id: links | [?type=='TEST_CASE_EXECUTION'] | [0].target }]} ]}]",
       "ArrayMergeOptions":"",
       "HistoryIdentifyRules":"",
       "HistoryExtractionRules":"",
       "ProcessRules":null,
       "ProcessFunction":null
    }

Aggregated object before receiving EiffelTestCaseStartedEvent, click to unfold

    {
       "_id":"b46ef12d-25gb-4d7y-b9fd-8763re66de47",
       "aggregatedObject":{
          "ongoing":"true",
          "test_suite":[
             {
                "test_case":[
                   {
                      "ongoing":"false",
                      "test_data":null,
                      "trigger_event_id":"v46ef19d-20gb-4d2y-h9fa-87dada6kde47"
                   }
                ]
             }
          ],
          "TemplateName":"TEST_EXECUTION_1",
          "id":"b46ef12d-25gb-4d7y-b9fd-8763re66de47",
          "time":1234567890,
          "type":"EiffelActivityTriggeredEvent",
          "version":"1.0.0",
          "test_suite_name":"Pre-release installation and security verification"
       }
    }

EiffelTestCaseStartedEvent, click to unfold

    {
       "meta":{
          "type":"EiffelTestCaseStartedEvent",
          "version":"1.0.0",
          "time":1234567890,
          "id":"v46ef19d-20ab-4d2y-h9fe-87haha6kde47"
       },
       "data":{
          "executor":"My Test Framework",
          "liveLogs":[
             {
                "name":"My test log",
                "uri":"file:///tmp/logs/data.log"
             }
          ]
       },
       "links":[
          {
             "type":"ENVIRONMENT",
             "target":"aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeee1"
          },
          {
             "type":"TEST_CASE_EXECUTION",
             "target":"v46ef19d-20gb-4d2y-h9fa-87dada6kde47"
          }
       ]
    }

Aggregated object after receiving EiffelTestCaseStartedEvent, click to unfold

    {
       "_id":"b46ef12d-25gb-4d7y-b9fd-8763re66de47",
       "aggregatedObject":{
          "ongoing":"true",
          "test_suite":[
             {
                "test_case":[
                   {
                      "ongoing":"true",
                      "test_data":null,
                      "trigger_event_id":"v46ef19d-20gb-4d2y-h9fa-87dada6kde47"
                   }
                ]
             }
          ],
          "TemplateName":"TEST_EXECUTION_1",
          "id":"b46ef12d-25gb-4d7y-b9fd-8763re66de47",
          "time":1234567890,
          "type":"EiffelActivityTriggeredEvent",
          "version":"1.0.0",
          "test_suite_name":"Pre-release installation and security verification"
       }
    }


<h4>Upstream events</h4>

<p>
Upstream events in Eiffel means those events that were processed/issued before 
or were the cause to the specified event. Such a connection works via the 
<a href="https://github.com/Ericsson/eiffel/blob/master/eiffel-syntax-and-usage/the-links-object.md">links</a>
mechanism. For example we have EiffelSourceChangeCreatedEvent, 
EiffelSourceChangeSubmittedEvent, EiffelArtifactCreatedEvent, 
EiffelArtifactPublishedEvent, so the upstream events for 
EiffelArtifactCreatedEvent will be EiffelSourceChangeCreatedEvent and 
EiffelSourceChangeSubmittedEvent.
</p>


<h4>Downstream events</h4>

<p>
Downstream events in Eiffel means those events that will be issued after or were 
the outcome to the specified event. Such a connection works via the 
<a href="https://github.com/Ericsson/eiffel/blob/master/eiffel-syntax-and-usage/the-links-object.md">links</a>
mechanism. For example we have EiffelSourceChangeCreatedEvent,
EiffelSourceChangeSubmittedEvent, EiffelArtifactCreatedEvent, 
EiffelArtifactPublishedEvent, so the downstream event for
EiffelArtifactCreatedEvent will be EiffelArtifactPublishedEvent.
</p>


<h4>Rule examples with different identifiers that might be used as a reference</h4>

    {
       "TemplateName":"TEST_EXECUTION_1",a
       "Type":"EiffelTestCaseTriggeredEvent",
       "TypeRule":"meta.type",
       "IdRule":"meta.id",
       "StartEvent":"NO",
       "IdentifyRules":"links | [?type=='CONTEXT'].target",
       "MatchIdRules":{
          "_id":"%IdentifyRules_objid%"
       },
       "ExtractionRules":"{trigger_event_id : meta.id, test_data : data.testcase, ongoing : `\"false\"`}",
       "MergeResolverRules":"[{NONEPATH:NONE},  {test_suite: [{ test_case : [{ trigger_event_id:meta.id}]} ]}]",
       "ArrayMergeOptions":"",
       "HistoryIdentifyRules":"",
       "HistoryExtractionRules":"",
       "ProcessRules":null,
       "ProcessFunction":null
    }
    {
       "TemplateName":"TEST_EXECUTION_1",
       "Type":"EiffelActivityFinishedEvent",
       "TypeRule":"meta.type",
       "IdRule":"meta.id",
       "StartEvent":"NO",
       "IdentifyRules":"links | [?type=='ACTIVITY_EXECUTION'].target",
       "MatchIdRules":{
          "_id":"%IdentifyRules_objid%"
       },
       "ExtractionRules":"{ ongoing : `\"false\"`, outcome : data.outcome}",
       "MergeResolverRules":null,
       "ArrayMergeOptions":"",
       "HistoryIdentifyRules":"",
       "HistoryExtractionRules":"",
       "ProcessRules":null,
       "ProcessFunction":null
    }
    {
       "TemplateName":"ARTIFACT_1",
       "Type":"EiffelConfidenceLevelModifiedEvent",
       "TypeRule":"meta.type",
       "IdRule":"meta.id",
       "StartEvent":"NO",
       "IdentifyRules":"links | [?type=='SUBJECT'].target",
       "MatchIdRules":{
          "_id":"%IdentifyRules_objid%"
       },
       "ExtractionRules":"{confidenceLevels :[{ eventId:meta.id, time:meta.time, name:data.name, value:data.value}]}",
       "ArrayMergeOptions":"",
       "HistoryIdentifyRules":"",
       "HistoryExtractionRules":"",
       "ProcessRules":null,
       "ProcessFunction":null
    }
    {
       "TemplateName":"ARTIFACT_1",
       "Type":"EiffelArtifactCreatedEvent",
       "TypeRule":"meta.type",
       "IdRule":"meta.id",
       "StartEvent":"YES",
       "IdentifyRules":"[meta.id]",
       "MatchIdRules":{
          "_id":"%IdentifyRules_objid%"
       },
       "ExtractionRules":"{ id : meta.id, type : meta.type, time : meta.time, gav : data.gav, fileInformation : data.fileInformation, buildCommand : data.buildCommand }",
       "DownstreamIdentifyRules":"links | [?type=='COMPOSITION'].target",
       "DownstreamMergeRules":"{\"externalComposition\":{\"eventId\":%IdentifyRules%}}",
       "DownstreamExtractionRules":"{artifacts: [{id : meta.id}]}",
       "ArrayMergeOptions":"",
       "HistoryIdentifyRules":"links | [?type=='COMPOSITION'].target",
       "HistoryExtractionRules":"{internalComposition:{artifacts: [{id : meta.id}]}}",
       "HistoryPathRules":"{artifacts: {id: meta.id}}",
       "ProcessRules":null,
       "ProcessFunction":null
    }
    {
       "TemplateName":"ARTIFACT_1",
       "Type":"EiffelTestCaseFinishedEvent",
       "TypeRule":"meta.type",
       "IdRule":"meta.id",
       "StartEvent":"NO",
       "IdentifyRules":"links | [?type=='TEST_CASE_EXECUTION'].target",
       "MatchIdRules":{
          "$and":[
             {
                "aggregatedObject.testCaseExecutions.testCaseStartedEventId":"%IdentifyRules%"
             }
          ]
       },
       "ExtractionRules":"{ testCaseFinishEventId:meta.id, testCaseFinishedTime:meta.time, testCase:data.outcome}",
       "MergeResolverRules":"{\"testCaseStartedEventId\":%IdentifyRules%}",
       "ArrayMergeOptions":"",
       "HistoryIdentifyRules":"",
       "HistoryExtractionRules":"",
       "ProcessRules":"{testCaseDuration : diff(testCaseExecutions[0].testCaseFinishedTime, testCaseExecutions[0].testCaseStartedTime)}",
       "ProcessFunction":"difference"
    }

<p>
Also note that if you refer to a key that does not exist, a value of null (or 
the language equivalent of null) is returned.
</p>
