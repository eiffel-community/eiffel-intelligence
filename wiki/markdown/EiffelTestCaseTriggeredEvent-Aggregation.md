# EiffelTestCaseTriggeredEvent Aggregation

When the start event ArtifactCreatedEvent has been aggregated we assume that it 
has triggered a test case and an EiffelTestCaseTriggeredEvent is received that 
looks like below. 

    { 
      "data": { 
        "customData": [ 
          { 
            "key": "name", 
            "value": "TCT7" 
          }, 
          { 
            "key": "iteration", 
            "value": 1 
          } 
        ], 
        "testCase": { 
          "id": "TC5", 
          "uri": "https://other-tm.company.com/testCase/TC5", 
          "tracker": "My Other Test Management System" 
        } 
      }, 
      "meta": { 
        "type": "EiffelTestCaseTriggeredEvent", 
        "source": { 
          "domainId": "example.domain" 
        }, 
        "version": "1.0.0", 
        "id": "6d3df0e0-404d-46ee-ab4f-3118457148f4", 
        "time": 1490777327230 
      }, 
      "links": [ 
        { 
          "type": "CONTEXT", 
          "target": "bebdb0b7-f59e-4b77-ba8e-3912593d0153" 
        }, 
        { 
          "type": "IUT", 
          "target": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43" 
        } 
      ] 
    } 
     
The next step is to fetch the rules for this event and they are: 

    { 
      "TemplateName": "ARTIFACT_1", 
      "Type": "EiffelTestCaseTriggeredEvent", 
      "TypeRule": "meta.type", 
      "IdRule": "meta.id", 
      "StartEvent": "NO", 
      "IdentifyRules": "links | [?type=='IUT'].target", 
      "MatchIdRules": { 
        "_id": "%IdentifyRules_objid%" 
      }, 
      "ExtractionRules": "{ testCaseTriggeredEventId:meta.id, testCaseTriggeredTime:meta.time, outcome:data.testCase }", 
      "MergeResolverRules": "[ {NONEPATH:NONE}, {testCaseExecutions: [{ testCaseTriggeredEventId: meta.id }]} ]", 
      "ArrayOptions": "", 
      "HistoryIdentifyRules": "", 
      "HistoryExtractionRules": "", 
      "ProcessRules": null, 
      "ProcessFunction": null 
    } 
     
With help of identifyRule: 

    links | [?type=='IUT'].target 

the following object’s id is selected: 

    ["6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43"] 

But there is no object with such id in the database and aggregatedObject 
returns empty. Event is added to wait list. There it waits until the object 
with requested id appears in database. 

Under a time a second EiffelTestCaseTriggeredEvent is received and it looks 
like below: 

    { 
      "data": { 
        "customData": [ 
          { 
            "key": "name", 
            "value": "TCT7" 
          }, 
          { 
            "key": "iteration", 
            "value": 1 
          } 
        ] 
      }, 
      "meta": { 
        "type": "EiffelTestCaseTriggeredEvent", 
        "source": { 
          "domainId": "example.domain" 
        }, 
        "version": "1.0.0", 
        "id": "6d3df0e0-404d-46ee-ab4f-3118457148f5", 
        "time": 1490777357289 
      }, 
      "links": [ 
        { 
          "type": "CONTEXT", 
          "target": "bebdb0b7-f59e-4b77-ba8e-3912593d0153" 
        }, 
        { 
          "type": "IUT", 
          "target": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43" 
        } 
      ] 
    } 
     
The next step is to fetch the rules for the second event and they are: 

    { 
      "TemplateName": "ARTIFACT_1", 
      "Type": "EiffelTestCaseTriggeredEvent", 
      "TypeRule": "meta.type", 
      "IdRule": "meta.id", 
      "StartEvent": "NO", 
      "IdentifyRules": "links | [?type=='IUT'].target", 
      "MatchIdRules": { 
        "_id": "%IdentifyRules_objid%" 
      }, 
      "ExtractionRules": "{ testCaseTriggeredEventId:meta.id, testCaseTriggeredTime:meta.time, outcome:data.testCase }", 
      "MergeResolverRules": "[ {NONEPATH:NONE}, {testCaseExecutions: [{ testCaseTriggeredEventId: meta.id }]} ]", 
      "ArrayOptions": "", 
      "HistoryIdentifyRules": "", 
      "HistoryExtractionRules": "", 
      "ProcessRules": null, 
      "ProcessFunction": null 
    } 
     
With help of identifyRule: 

    links | [?type=='IUT'].target

the following object’s id is selected: 

    ["6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43"] 

It is the same id, as previously and the object with that id is still not in 
the database. aggregatedObject returns empty and event is added to wait list. 
There, it waits until the object with requested id appears in the database. 

Object with id "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43" appears but is modified 
before one of the above events are taken from the wait list. 

The object with id "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43" looks like below: 

    { 
        "_id": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43", 
        "aggregatedObject": { 
          "fileInformation": [ 
            { 
              "extension": "jar", 
              "classifier": "debug" 
            }, 
            { 
              "extension": "txt", 
              "classifier": "test" 
            }, 
            { 
              "extension": "exe", 
              "classifier": "application" 
            } 
          ], 
          "buildCommand": null, 
          "confidenceLevels": [ 
            { 
              "eventId": "f37d59a3-069e-4f4c-8cc5-a52e73501a76", 
              "name": "performance", 
              "time": 1481875988767, 
              "value": "SUCCESS" 
            }, 
            { 
              "eventId": "f37d59a3-069e-4f4c-8cc5-a52e73501a75", 
              "name": "readyForDelivery", 
              "time": 1481875944272, 
              "value": "SUCCESS" 
            } 
          ], 
          "TemplateName": "ARTIFACT_1", 
          "id": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43", 
          "time": 1481875891763, 
          "type": "EiffelArtifactCreatedEvent", 
          "gav": { 
            "groupId": "com.mycompany.myproduct", 
            "artifactId": "sub-system", 
            "version": "1.1.0" 
          } 
        } 
      } 
     
First event with id "6d3df0e0-404d-46ee-ab4f-3118457148f4" is aggregated. The 
required content is extracted from the event as specified in the rule:  

    "ExtractionRules": "{ testCaseTriggeredEventId:meta.id, testCaseTriggeredTime:meta.time, outcome:data.testCase }" 

And is put in to the object in the way as it is specified in this rule: 

    "MergeResolverRules": "[ {NONEPATH:NONE}, {testCaseExecutions: [{ testCaseTriggeredEventId: meta.id }]} ]" 

JSON object with requested data will be put into array and stored in aggregated 
object with key “testsKeysExecutions”. Data in correct format will look like: 

    "testCaseExecutions": [ 

    { 
        "testCaseTriggeredTime": 1490777327230, 
        "testCaseTriggeredEventId": "6d3df0e0-404d-46ee-ab4f-3118457148f4", 
        "outcome": { 
          "tracker": "My Other Test Management System", 
          "id": "TC5", 
          "uri": "https://other-tm.company.com/testCase/TC5" 
        } 
      } 
    ] 

And the result object will look like below: 

    { 
      "_id": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43", 
      "aggregatedObject": { 
        "fileInformation": [ 
          { 
            "extension": "jar", 
            "classifier": "debug" 
          }, 
          { 
            "extension": "txt", 
            "classifier": "test" 
          }, 
          { 
            "extension": "exe", 
            "classifier": "application" 
          } 
        ], 
        "buildCommand": null, 
        "confidenceLevels": [ 
          { 
            "eventId": "f37d59a3-069e-4f4c-8cc5-a52e73501a76", 
            "name": "performance", 
            "time": 1481875988767, 
            "value": "SUCCESS" 
          }, 
          { 
            "eventId": "f37d59a3-069e-4f4c-8cc5-a52e73501a75", 
            "name": "readyForDelivery", 
            "time": 1481875944272, 
            "value": "SUCCESS" 
          } 
        ], 
        "testCaseExecutions": [ 
          { 
            "testCaseTriggeredTime": 1490777327230, 
            "testCaseTriggeredEventId": "6d3df0e0-404d-46ee-ab4f-3118457148f4", 
            "outcome": { 
              "tracker": "My Other Test Management System", 
              "id": "TC5", 
              "uri": "https://other-tm.company.com/testCase/TC5" 
            } 
          } 
        ], 
        "TemplateName": "ARTIFACT_1", 
        "id": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43", 
        "time": 1481875891763, 
        "type": "EiffelArtifactCreatedEvent", 
        "gav": { 
          "groupId": "com.mycompany.myproduct", 
          "artifactId": "sub-system", 
          "version": "1.1.0" 
        } 
      } 
    } 
     
Then the second event with id "6d3df0e0-404d-46ee-ab4f-3118457148f5" is taken 
from wait list and it is aggregated. 

The required content is extracted from the event as specified in the rule:  

     "ExtractionRules": "{ testCaseTriggeredEventId:meta.id, testCaseTriggeredTime:meta.time, outcome:data.testCase }" 

And is put in to the object in the way as it is specified in this rule: 

    "MergeResolverRules": "[ {NONEPATH:NONE}, {testCaseExecutions: [{ testCaseTriggeredEventId: meta.id }]} ]" 

Data in correct format will look like: 

    "testCaseExecutions": [ 
      { 
        "testCaseTriggeredTime": 1490777357289, 
        "testCaseTriggeredEventId": "6d3df0e0-404d-46ee-ab4f-3118457148f5", 
        "outcome": null 
      } 

But because the object already contains a key “testCaseExecutions” that contains 
an array. The JSON object with data will be added to existing array. New 
“testCaseExecutions“ array will look like: 

    "testCaseExecutions": [ 
          { 
            "testCaseTriggeredTime": 1490777327230, 
            "testCaseTriggeredEventId": "6d3df0e0-404d-46ee-ab4f-3118457148f4", 
            "outcome": { 
              "tracker": "My Other Test Management System", 
              "id": "TC5", 
              "uri": "https://other-tm.company.com/testCase/TC5" 
            } 
          }, 
          { 
            "testCaseTriggeredTime": 1490777357289, 
            "testCaseTriggeredEventId": "6d3df0e0-404d-46ee-ab4f-3118457148f5", 
            "outcome": null 
          } 
        ] 
     
And the result object will look like below: 

    { 
      "_id": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43", 
      "aggregatedObject": { 
        "fileInformation": [ 
          { 
            "extension": "jar", 
            "classifier": "debug" 
          }, 
          { 
            "extension": "txt", 
            "classifier": "test" 
          }, 
          { 
            "extension": "exe", 
            "classifier": "application" 
          } 
        ], 
        "buildCommand": null, 
        "confidenceLevels": [ 
          { 
            "eventId": "f37d59a3-069e-4f4c-8cc5-a52e73501a76", 
            "name": "performance", 
            "time": 1481875988767, 
            "value": "SUCCESS" 
          }, 
          { 
            "eventId": "f37d59a3-069e-4f4c-8cc5-a52e73501a75", 
            "name": "readyForDelivery", 
            "time": 1481875944272, 
            "value": "SUCCESS" 
          } 
        ], 
        "testCaseExecutions": [ 
          { 
            "testCaseTriggeredTime": 1490777327230, 
            "testCaseTriggeredEventId": "6d3df0e0-404d-46ee-ab4f-3118457148f4", 
            "outcome": { 
              "tracker": "My Other Test Management System", 
              "id": "TC5", 
              "uri": "https://other-tm.company.com/testCase/TC5" 
            } 
          }, 
          { 
            "testCaseTriggeredTime": 1490777357289, 
            "testCaseTriggeredEventId": "6d3df0e0-404d-46ee-ab4f-3118457148f5", 
            "outcome": null 
          } 
        ], 
        "TemplateName": "ARTIFACT_1", 
        "id": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43", 
        "time": 1481875891763, 
        "type": "EiffelArtifactCreatedEvent", 
        "gav": { 
          "groupId": "com.mycompany.myproduct", 
          "artifactId": "sub-system", 
          "version": "1.1.0" 
        } 
      } 
    } 
