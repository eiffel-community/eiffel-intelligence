# ArtifactPublishedEvent Aggregation

## Eiffel events are sent
Consider an artifact has been published and ArtifactPublishedEvent is
generated as follow:

    {
        "links":[
           {
              "target":"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
              "type":"ARTIFACT"
           },
           {
              "target":"c04fa59a-3c36-4601-8eac-7a26b8910f08",
              "type":"CONTEXT"
           }
        ],
        "meta":{
           "id":"33d05e6f-9bd9-4138-83b6-e20cc74680a3",
           "source":{
              "domainId":"example.domain"
           },
           "time":1481875921763,
           "type":"EiffelArtifactPublishedEvent",
           "version":"3.0.0"
        },
        "data":{
           "customData":[
              {
                 "value":"ArtP2",
                 "key":"name"
              },
              {
                 "value":1,
                 "key":"iteration"
              }
           ],
           "locations":[
              {
                 "uri":"https://myrepository.com/mySubSystemArtifact",
                 "type":"PLAIN"
              }
           ]
        }
    }

## Extract data from Eiffel event based on rules
In the next step rules for this event are extracted:

    {
        "TemplateName":"ARTIFACT_1",
        "Type":"EiffelArtifactPublishedEvent",
        "TypeRule":"meta.type",
        "IdRule":"meta.id",
        "StartEvent":"NO",
        "IdentifyRules":"links | [?type=='ARTIFACT'].target",
        "MatchIdRules":{ "_id":"%IdentifyRulesEventId%" },
        "ExtractionRules":"{ eventId : meta.id, time : meta.time, locations : data.locations }",
        "MergeResolverRules":"[ {NONEPATH:NONE}, {publications: [{ eventId: meta.id }]} ]",
        "HistoryIdentifyRules":"",
        "HistoryExtractionRules":"",
        "ProcessRules":null,
        "ProcessFunction":null
    }

## Current aggregation
Following aggregated object is extracted by using the IdentifyRules:

      {
         "_id": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
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
         "internalComposition": {
            "compositions": [
                  {
                     "eventId": "fb6ef12d-25fb-4d77-b9fd-87688e66de47",
                     "name": "My composition",
                     "time": 2000,
                     "artifacts": [
                        {
                              "fileInformation": [
                                 {
                                    "extension": "jar",
                                    "classifier": "debug"
                                 }
                              ],
                              "internalComposition": {
                                 "compositions": [
                                    {
                                          "eventId": "fb6ef12d-25fb-4d77-b9fd-87688e66da4j",
                                          "sourceChanges": [
                                             {
                                                "submitter": {
                                                      "name": "Jane Doe",
                                                      "email": "jane.doe@company.com"
                                                },
                                                "gitIdentifier": null,
                                                "SCSEventId": "3ce9df6e-cd45-4320-ae66-945f038caa1b",
                                                "sourceCreations": [
                                                      {
                                                         "gitIdentifier": {
                                                            "repoName": "myPrivateRepo",
                                                            "repoUri": "https://github.com/johndoe/myPrivateRepo.git",
                                                            "commitId": "fd090b60a4aedc5161da9c035a49b14a319829b4",
                                                            "branch": "myBranch"
                                                         },
                                                         "SCCEventId": "ac085e24-ac4c-41be-912d-08c7afd32285",
                                                         "author": {
                                                            "name": "John Doe",
                                                            "id": "johndoe",
                                                            "email": "john.doe@company.com",
                                                            "group": "Team Gophers"
                                                         },
                                                         "issues": [
                                                         ]
                                                      },
                                                      {
                                                         "gitIdentifier": {
                                                            "repoName": "myPrivateRepo",
                                                            "repoUri": "https://github.com/johndoe/myPrivateRepo.git",
                                                            "commitId": "fd090b60a4aedc5161da9c035a49b14a319829b4",
                                                            "branch": "myBranch"
                                                         },
                                                         "SCCEventId": "552ad6a4-c522-47e2-9195-0481930979e4",
                                                         "author": null,
                                                         "issues": [
                                                            {
                                                                  "tracker": "JIRA",
                                                                  "id": "JIRA-1234",
                                                                  "type": "BUG",
                                                                  "uri": "http://jira.company.com/browse/JIRA-1234",
                                                                  "transition": "RESOLVED"
                                                            }
                                                         ]
                                                      }
                                                ]
                                             }
                                          ],
                                          "name": "Other composition",
                                          "time": 5005
                                    }
                                 ]
                              },
                              "id": "1100572b-c3j4-441e-abc9-b62f48080011",
                              "identity": "pkg:maven/com.mycompany.otherproduct/other-system@1.33.0"
                        },
                        {
                              "fileInformation": [
                                 {
                                    "extension": "jar",
                                    "classifier": "debug"
                                 }
                              ],
                              "id": "4400572b-c3j4-441e-abc9-b62f48080033",
                              "identity": "pkg:maven/com.internalcompany.internalproduct/internal-system@1.99.0"
                        }
                     ]
                  }
            ]
         },
         "confidenceLevels": [
            {
                  "eventId": "f37d59a3-069e-4f4c-8cc5-a52e73501a76",
                  "name": "performance",
                  "time": 1481875988767,
                  "value": "SUCCESS"
            }
         ],
         "testCaseExecutions": [
            {
                  "testCaseTriggeredTime": 1490777357289,
                  "testCaseTriggeredEventId": "6d3df0e0-404d-46ee-ab4f-3118457148f5",
                  "outcome": null
            },
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
         "identity": "pkg:maven/com.mycompany.myproduct/sub-system@1.1.0",
         "artifacts": [
            {
                  "fileInformation": [
                     {
                        "extension": "jar",
                        "classifier": "debug"
                     }
                  ],
                  "id": "1100572b-c3b4-461e-abc9-b62f48087011",
                  "identity": "pkg:maven/com.othercompany.secondproduct/other-system@1.33.0"
            }
         ]
      }

## Data extraction from event 
Process begins to extract content from the event as specified in the extraction
rule and puts it in the aggregated object.

Extraction rule:

    { eventId : meta.id, time : meta.time, locations : data.locations }

And Extracted contents are:

    {
        "eventId":"33d05e6f-9bd9-4138-83b6-e20cc74680a3",
        "time":1481875921763,
        "locations":[
           {
              "uri":"https://myrepository.com/mySubSystemArtifact",
              "type":"PLAIN"
           }
        ]
    }

Next the merge rules is extracted as:

    [{NONEPATH:NONE}, {publications: [{ eventId: meta.id }]}]

Using JMESPath, merge rules are created:

    [{"NONEPATH":null},{"publications":[{"eventId":"33d05e6f-9bd9-4138-83b6-e20cc74680a3"}]}]

Next the path to merge content in the aggregated object is computed:

    “publications.0.eventId”

Contents are prepared to merge in the object:

    {
        "publications":[
           {
              "eventId":"33d05e6f-9bd9-4138-83b6-e20cc74680a3",
              "locations":[
                 {
                    "type":"PLAIN",
                    "uri":"https:\/\/myrepository.com\/mySubSystemArtifact"
                 }
              ],
              "time":1481875921763
           }
        ]
    }

## Updated aggregation
Finally, after merging the content, the aggregated object looks like this:

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
                 "time":2000,
                 "artifacts":[
                    {
                       "fileInformation":[
                          {
                             "extension":"jar",
                             "classifier":"debug"
                          }
                       ],
                       "internalComposition":{
                          "compositions":[
                             {
                                "eventId":"fb6ef12d-25fb-4d77-b9fd-87688e66da4j",
                                "sourceChanges":[
                                   {
                                      "submitter":{
                                         "name":"Jane Doe",
                                         "email":"jane.doe@company.com"
                                      },
                                      "gitIdentifier":null,
                                      "SCSEventId":"3ce9df6e-cd45-4320-ae66-945f038caa1b",
                                      "sourceCreations":[
                                         {
                                            "gitIdentifier":{
                                               "repoName":"myPrivateRepo",
                                               "repoUri":"https:\/\/github.com\/johndoe\/myPrivateRepo.git",
                                               "commitId":"fd090b60a4aedc5161da9c035a49b14a319829b4",
                                               "branch":"myBranch"
                                            },
                                            "SCCEventId":"ac085e24-ac4c-41be-912d-08c7afd32285",
                                            "author":{
                                               "name":"John Doe",
                                               "id":"johndoe",
                                               "email":"john.doe@company.com",
                                               "group":"Team Gophers"
                                            },
                                            "issues":[
                                           ]
                                         },
                                         {
                                            "gitIdentifier":{
                                               "repoName":"myPrivateRepo",
                                               "repoUri":"https:\/\/github.com\/johndoe\/myPrivateRepo.git",
                                               "commitId":"fd090b60a4aedc5161da9c035a49b14a319829b4",
                                               "branch":"myBranch"
                                            },
                                            "SCCEventId":"552ad6a4-c522-47e2-9195-0481930979e4",
                                            "author":null,
                                            "issues":[
                                               {
                                                  "tracker":"JIRA",
                                                  "id":"JIRA-1234",
                                                  "type":"BUG",
                                                  "uri":"http:\/\/jira.company.com\/browse\/JIRA-1234",
                                                  "transition":"RESOLVED"
                                               }
                                            ]
                                         }
                                      ]
                                   }
                                ],
                                "name":"Other composition",
                                "time":5005
                             }
                          ]
                       },
                       "id":"1100572b-c3j4-441e-abc9-b62f48080011",
                       "identity": "pkg:maven/com.othercompany.otherproduct/other-system@1.33.0"
                    },
                    {
                       "fileInformation":[
                          {
                             "extension":"jar",
                             "classifier":"debug"
                          }
                       ],
                       "id":"4400572b-c3j4-441e-abc9-b62f48080033",
                       "identity": "pkg:maven/com.internalcompany.internalproduct/internal-system@1.99.0"
                    }
                 ]
              }
           ]
        },
        "confidenceLevels":[
           {
              "eventId":"f37d59a3-069e-4f4c-8cc5-a52e73501a76",
              "name":"performance",
              "time":1481875988767,
              "value":"SUCCESS"
           }
        ],
        "testCaseExecutions":[
           {
              "testCaseTriggeredTime":1490777357289,
              "testCaseTriggeredEventId":"6d3df0e0-404d-46ee-ab4f-3118457148f5",
              "outcome":null
           },
           {
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
        "identity": "pkg:maven/com.mycompany.myproduct/sub-system@1.1.0",
        "artifacts":[
           {
              "fileInformation":[
                 {
                    "extension":"jar",
                    "classifier":"debug"
                 }
              ],
              "id":"1100572b-c3b4-461e-abc9-b62f48087011",
              "identity": "pkg:maven/com.othercompany.secondproduct/other-system@1.33.0"
           }
        ],
        "publications":[
           {
              "eventId":"33d05e6f-9bd9-4138-83b6-e20cc74680a3",
              "locations":[
                 {
                    "type":"PLAIN",
                    "uri":"https:\/\/myrepository.com\/mySubSystemArtifact"
                 }
              ],
              "time":1481875921763
           }
        ]
    }

Next step is to apply the ProcessRules on the aggregated object, but in 
this case no ProcessRules are defined for this Eiffel event, so this step is skipped.

#### [**Next: Aggregating an ConfidenceLevelModified event**](confidence-level-modified-event-aggregation.md)

