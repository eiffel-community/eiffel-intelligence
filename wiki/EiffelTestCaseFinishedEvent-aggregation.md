# EiffelTestCaseFinishedEvent Aggregation

Eiffel Test Case Finished Event  

Suppose that and artifact has been build, published and uploaded and all tests in a test case has finished and as a result an EiffleTestCaseFinished event is received as follow:  

    { 
       "links":[ 
          { 
             "target":"cb9d64b0-a6e9-4419-8b5d-a650c27c59ca", 
             "type":"TEST_CASE_EXECUTION" 
          } 
       ], 
       "meta":{ 
          "id":"cb9d64b0-a6e9-4419-8b5d-a650c27c1111", 
          "source":{ 
             "domainId":"example.domain" 
          }, 
          "time":1481875935919, 
          "type":"EiffelTestCaseFinishedEvent", 
          "version":"1.0.0" 
       }, 
       "data":{ 
          "outcome":{ 
             "verdict":"PASSED", 
             "conclusion":"SUCCESSFUL" 
          }, 
          "customData":[ 
             { 
                "value":"TCF5", 
                "key":"name" 
             }, 
             { 
                "value":1, 
                "key":"iteration" 
             } 
          ] 
       } 
    } 

The next step is to fetch the rules for this event and they are:  

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
       "ExtractionRules":"{ testCaseFinishedEventId:meta.id, testCaseFinishedTime:meta.time, outcome:data.outcome}", 
       "MergeResolverRules":"{\"testCaseStartedEventId\":%IdentifyRules%}", 
       "ArrayMergeOptions":"", 
       "HistoryIdentifyRules":"", 
       "HistoryExtractionRules":"", 
       "ProcessRules":"{testCaseDuration : diff(testCaseExecutions | [?testCaseStartedEventId=='%IdentifyRules%'].testCaseFinishedTime | [0], testCaseExecutions | [?testCaseStartedEventId=='%IdentifyRules%'].testCaseStartedTime | [0])}", 
       "ProcessFunction":"difference" 
    } 

With help of identifyRule: 

    links | [?type=='TEST_CASE_EXECUTION'].target 

The following object’s id is selected: 

    [cb9d64b0-a6e9-4419-8b5d-a650c27c59ca] 

Following aggregated object is extracted by using the identify rules:  

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

The event EiffelTestCaseFinishedEvent is aggregated. The required content is extracted from the event as specified in the rule: 

    "ExtractionRules":"{ testCaseFinishedEventId:meta.id,    testCaseFinishedTime:meta.time, outcome:data.outcome}" 

And is put in to the object in the way as it is specified in this rule: 

    "MergeResolverRules":"{\"testCaseStartedEventId\":%IdentifyRules%}" 

Here we also have some processRules: 

    "ProcessRules":"{testCaseDuration : diff(testCaseExecutions | [?testCaseStartedEventId=='%IdentifyRules%'].testCaseFinishedTime | [0], testCaseExecutions | [?testCaseStartedEventId=='%IdentifyRules%'].testCaseStartedTime | [0])}", 

The processRules in this case makes a calculation where id calculates the difference from testCasestartedTime and testCaseFinishedTime this calculated time is the time it took for the test case to execute and is put in a key called testCaseDuration. 

JSON object with requested data will be put into array and stored in aggregated object with key “testsKeysExecutions”. Data in correct format will look like: 

    { 
      "testCaseStartedTime":1481875925916, 
      "testCaseFinishedEventId":"cb9d64b0-a6e9-4419-8b5d-a650c27c1111", 
      "testCaseStartedEventId":"cb9d64b0-a6e9-4419-8b5d-a650c27c59ca", 
      "testCaseFinishedTime":1481875935919, 
      "testCaseDuration":10003, 
      "outcome":{ 
        "conclusion":"SUCCESSFUL", 
        "verdict":"PASSED" 
      } 
    } 

Finally, you will be able to find the fully aggregated object that may contain information about one or several executed test cases: 

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
                                  "repoUri":"https://github.com/johndoe/myPrivateRepo.git", 
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
                                  "repoUri":"https://github.com/johndoe/myPrivateRepo.git", 
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
                                    "uri":"http://jira.company.com/browse/JIRA-1234", 
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
                  "gav":{ 
                    "groupId":"com.othercompany.otherproduct", 
                    "artifactId":"other-system", 
                    "version":"1.33.0" 
                  } 
                }, 
                { 
                  "fileInformation":[ 
                    { 
                      "extension":"jar", 
                      "classifier":"debug" 
                    } 
                  ], 
                  "id":"4400572b-c3j4-441e-abc9-b62f48080033", 
                  "gav":{ 
                    "groupId":"com.internalcompany.internalproduct", 
                    "artifactId":"internal-system", 
                    "version":"1.99.0" 
                  } 
                } 
              ] 
            } 
          ] 
        }, 
        "testCaseExecutions":[ 
          { 
            "testCaseStartedTime":1481875925916, 
            "testCaseFinishedEventId":"cb9d64b0-a6e9-4419-8b5d-a650c27c1111", 
            "testCaseStartedEventId":"cb9d64b0-a6e9-4419-8b5d-a650c27c59ca", 
            "testCaseFinishedTime":1481875935919, 
            "testCaseTriggeredTime":1490777327230, 
            "testCaseDuration":10003, 
            "testCaseTriggeredEventId":"6d3df0e0-404d-46ee-ab4f-3118457148f4", 
            "outcome":{ 
              "conclusion":"SUCCESSFUL", 
              "verdict":"PASSED", 
              "tracker":"My Other Test Management System", 
              "id":"TC5", 
              "uri":"https://other-tm.company.com/testCase/TC5" 
            } 
          } 
        ], 
        "confidenceLevels":[ 
          { 
            "eventId":"f37d59a3-069e-4f4c-8cc5-a52e73501a75", 
            "name":"readyForDelivery", 
            "time":1481875944272, 
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
        }, 
        "publications":[ 
          { 
            "eventId":"33d05e6f-9bd9-4138-83b6-e20cc74680a3", 
            "locations":[ 
              { 
                "type":"PLAIN", 
                "uri":"https://myrepository.com/mySubSystemArtifact" 
              } 
            ], 
            "time":1481875921763 
          } 
        ], 
        "artifacts":[ 
          { 
            "fileInformation":[ 
              { 
                "extension":"jar", 
                "classifier":"debug" 
              } 
            ], 
            "id":"1100572b-c3b4-461e-abc9-b62f48087011", 
            "gav":{ 
              "groupId":"com.othercompany.secondproduct", 
              "artifactId":"other-system", 
              "version":"1.33.0" 
            } 
          } 
        ] 
      } 
    } 
  
