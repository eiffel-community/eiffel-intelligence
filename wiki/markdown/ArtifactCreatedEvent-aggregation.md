# ArtifactCreatedEvent Aggregation

Suppose, we publish an “EiffelArtifactCreatedEvent” (given below) on the 
message bus. 

    {
    "meta": {
        "time": 1473177136433,
        "source": {
            "domainId": "example.domain"
        },
        "type": "EiffelArtifactCreatedEvent",
        "id": "ccce572c-c364-441e-abc9-b62fed080ca2",
        "version": "1.0.0"
    },
    "links": [{
            "target": "23df3bd2-c620-42ce-80ba-b7ba6551c9f9",
            "type": "COMPOSITION"
        },
        {
            "target": "9ace5eea-3547-45f6-be4d-25b43d87e6dc",
            "type": "ENVIRONMENT"
        },
        {
            "target": "51601fdb-db7c-4b3f-862e-da1154e4ae96",
            "type": "PREVIOUS_VERSION"
        },
        {
            "target": "51c77087-74e2-449f-9def-00acd5e2c944",
            "type": "CONTEXT"
        }
    ],
    "data": {
        "customData": [{
                "value": "ArtC2",
                "key": "name"
            },
            {
                "value": 53,
                "key": "iteration"
            }
        ],
        "fileInformation": [{
                "extension": "jar",
                "classifier": "debug"
            },
            {
                "extension": "",
                "classifier": "test"
            },
            {
                "extension": "exe",
                "classifier": ""
            }
        ],
        "gav": {
            "version": "1.53.0",
            "artifactId": "sub-system",
            "groupId": "com.mycompany.myproduct"
        }
    }
    }


When this message is received at EI, processing begins for possible aggregation 
and notification. In the next step rules object for the message is extracted. 
The rules object is a set of rules in JSON format that helps extract required 
information from an event by using JMESPath API (for more information about the 
EI rules follow this [link](./Rules.md)). From the rules object, 
“IdentifyRules” is extracted which is a JMESPath identifier of ids and will be 
used to search for all existing aggregated objects linked to the event under 
process. Here it should be noted that rules object path is configured in the 
application.properties file as “rules.path”.

**Rule object extracted from the event under consideration:**

    {
    "TemplateName": "ARTIFACT_1",
    "Type": "EiffelArtifactCreatedEvent",
    "TypeRule": "meta.type",
    "IdRule": "meta.id",
    "StartEvent": "YES",
    "IdentifyRules": "[meta.id]",
    "MatchIdRules": {
        "_id": "%IdentifyRules_objid%"
    },
    "ExtractionRules": "{ id : meta.id, type : meta.type, time : meta.time, gav : data.gav, fileInformation : data.fileInformation, buildCommand : data.buildCommand }",
    "DownstreamIdentifyRules": "links | [?type=='COMPOSITION'].target",
    "DownstreamMergeRules": "{\"externalComposition\":{\"eventId\":%IdentifyRules%}}",
    "DownstreamExtractionRules": "{artifacts: [{id : meta.id}]}",
    "HistoryIdentifyRules": "links | [?type=='COMPOSITION'].target",
    "HistoryExtractionRules": "{id : meta.id, gav : data.gav, fileInformation : data.fileInformation}",
    "HistoryPathRules": "{artifacts: [{id: meta.id}]}",
    "ProcessRules": null,
    "ProcessFunction": null
    }

If no event-linked aggregated object is found, then next step is to check 
whether the given event is a “start event”. It should be clear that, term 
“start event” means that the event might be starting a new flow and therefore, 
a new aggregated object may be created out of this. If the current event is not 
a “start event” then it is stored in the database. The name of the collection 
is configured in the application.properties file as “waitlist.collection.name”. 
The event is stored with the aim that if an aggregated object is created later 
with links to this specific event then it can be merged with that object. The 
event storage life time is set in the properties file as 
“waitlist.collection.ttlValue”.

Otherwise, If the event is a start event (as is the current event) then the 
next step is information extraction from the event and adding into the 
aggregated object. It should be noted that this event has no links to any 
aggregated object. However, as it is a start event so a new aggregated object 
is created by extracting information from this event.

**Information Extraction from an Event:** In this step, information from a 
given event is extracted. This information will then be added into a relevant 
aggregated object. This is what the term aggregation comes from. What 
information is extracted from a given event is encoded in the extraction rules. 
Extraction rules are represented in the rules object with a key named 
“ExtractionRules”. For example, for the current event and rule object, 
following information will be extracted:

    {
    "id": "ccce572c-c364-441e-abc9-b62fed080ca2",
    "type": "EiffelArtifactCreatedEvent",
    "time": 1473177136433,
    "gav": {
        "version": "1.53.0",
        "artifactId": "sub-system",
        "groupId": "com.mycompany.myproduct"
    },
    "fileInformation": [{
        "extension": "jar",
        "classifier": "debug"
    }, {
        "extension": "",
        "classifier": "test"
    }, {
        "extension": "exe",
        "classifier": ""
    }],
    "buildCommand": null
    }

Once the information extraction is complete, the next processing steps are 
different for start events (an event with no linked aggregated object) and 
events with link to an associated aggregated object. For start event, an object 
is created out of the extracted content and inserted into the database, with a 
name configured in the properties file as “aggregated.collection.name”. The 
next step is to query the database for all the upstream linked objects for this 
event. Then each of the linked object is processed individually. For each 
object, its rule object field “HistoryExtractionRules” is used to extract 
contents from it and merge with the aggregated object. More about the 
“HistoryExtractionRules” can be found [here](./Rules.md). Finally, the modified 
aggregated object is updated in the database.

On the other hand, If the event is linked to aggregated object(s) then each of 
the aggregated object is processed together with this event. The contents of 
the event to be merged into the aggregated object are extracted and added to 
the aggregated object using “MergeResolverRules”. More about 
“MergeResolverRules” can be found [here](./Rules.md). Next, “ProcessRules” 
(if exist) are applied on the aggregated object and the resulting modified 
aggregated object is updated in the database.
