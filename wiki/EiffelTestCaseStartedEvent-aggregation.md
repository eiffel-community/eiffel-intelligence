# EiffelTestCaseStartedEvent Aggregation

Suppose that the test has started and as a result an EiffleTestCaseStarted event is received as follow: 

    {   
        "links":[   
           {   
              "target":"5ac05ae1-d7a2-4ef0-be0b-670ee4e8a8cf", 
              "type":"CONTEXT" 
           }, 
           {   
              "target":"6d3df0e0-404d-46ee-ab4f-3118457148f4", 
              "type":"TEST_CASE_EXECUTION" 
           } 
        ], 
        "meta":{   
           "id":"cb9d64b0-a6e9-4419-8b5d-a650c27c59ca", 
           "source":{   
              "domainId":"example.domain" 
           }, 
           "time":1481875925916, 
           "type":"EiffelTestCaseStartedEvent", 
           "version":"1.0.0" 
        }, 
        "data":{   
           "customData":[   
              {   
                 "value":"TCS5", 
                 "key":"name" 
              }, 
              {   
                 "value":1, 
                 "key":"iteration" 
              } 
           ] 
        } 
    } 

In the next step rules for this event are extracted: 

    {   
        "TemplateName":"ARTIFACT_1", 
        "Type":"EiffelTestCaseStartedEvent", 
        "TypeRule":"meta.type", 
        "IdRule":"meta.id", 
        "StartEvent":"NO", 
        "IdentifyRules":"links | [?type=='TEST_CASE_EXECUTION'].target", 
        "MatchIdRules":{   
           "_id":"%IdentifyRules_objid%" 
        }, 
        "ExtractionRules":"{ testCaseStartedEventId:meta.id, testCaseStartedTime:meta.time, outcome:data.testCase }", 
        "MergeResolverRules":"[{NONEPATH:NONE}, {testCaseExecutions: [{ testCaseTriggeredEventId: links | [?type=='TEST_CASE_EXECUTION'] | [0].target }] }]", 
        "ArrayOptions":"", 
        "HistoryIdentifyRules":"", 
        "HistoryExtractionRules":"", 
        "ProcessRules":null, 
        "ProcessFunction":null 
    } 

  

Following aggregated object is extracted by using the identify rules: 

    [   
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
              "internalComposition":{   
                 "compositions":[   
                    {   
                       "eventId":"fb6ef12d-25fb-4d77-b9fd-87688e66de47", 
                       "name":"My composition", 
                       "time":2000 
                    } 
                 ] 
              }, 
              "testCaseExecutions":[   
                 {   
                    "testCaseTriggeredTime":1490777327230, 
                    "testCaseTriggeredEventId":"6d3df0e0-404d-46ee-ab4f-3118457148f4", 
                    "outcome":{   
                       "tracker":"My Other Test Management System", 
                       "id":"TC5", 
                       "uri":"https://other-tm.company.com/testCase/TC5" 
                    } 
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
    ] 

Process begins to extract content from the event, as specified in the extraction rule, and put in the aggregated object. 

Extraction rule: 

    { testCaseStartedEventId:meta.id, testCaseStartedTime:meta.time, outcome:data.testCase } 

And Extracted contents are: 

    {   
        "testCaseStartedEventId":"cb9d64b0-a6e9-4419-8b5d-a650c27c59ca", 
        "testCaseStartedTime":1481875925916, 
        "outcome":null 
    } 

Next the merge rules template is: 

    [{NONEPATH:NONE}, {testCaseExecutions: [{ testCaseTriggeredEventId: links | [?type=='TEST_CASE_EXECUTION'] | [0].target }] }] 

Merge rules are created using JMESPath: 

    [{"NONEPATH":null},{"testCaseExecutions":[{"testCaseTriggeredEventId":"6d3df0e0-404d-46ee-ab4f-3118457148f4"}]}] 

  

Next the path to merge content in the aggregated object is computed: 

    “testCaseExecutions.0.testCaseTriggeredEventId” 

Contents are prepared to merge in the object: 

    {"testCaseExecutions":[{"testCaseStartedTime":1481875925916,"testCaseStartedEventId":"cb9d64b0-a6e9-4419-8b5d-a650c27c59ca","testCaseTriggeredEventId":"6d3df0e0-404d-46ee-ab4f-3118457148f4","outcome":null}]} 

Finally, after merging the content, the merged object looks like this: 

    {   
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
        "internalComposition":{   
           "compositions":[   
              {   
                 "eventId":"fb6ef12d-25fb-4d77-b9fd-87688e66de47", 
                 "name":"My composition", 
                 "time":2000 
              } 
           ] 
        }, 
        "testCaseExecutions":[   
           {   
              "testCaseStartedTime":1481875925916, 
              "testCaseStartedEventId":"cb9d64b0-a6e9-4419-8b5d-a650c27c59ca", 
              "testCaseTriggeredTime":1490777327230, 
              "testCaseTriggeredEventId":"6d3df0e0-404d-46ee-ab4f-3118457148f4", 
              "outcome":{   
                 "tracker":"My Other Test Management System", 
                 "id":"TC5", 
                 "uri":"https:\/\/other-tm.company.com\/testCase\/TC5" 
              } 
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

Next step is to extract the process rules apply on the aggregated object: 

In this case, since there are no process rules are provided so this step is skipped. 

 
