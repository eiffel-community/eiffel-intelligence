# ConfidenceLevelModified event Aggregation

## Eiffel events are sent

### 1) f37d59a3-069e-4f4c-8cc5-a52e73501a76
Suppose an EiffelConfidenceLevelModified event is received:

    {
      "links": [
        {
          "target": "40df7fc4-0c40-40a3-a349-9f6da0ba81c5",
          "type": "CAUSE"
        },
        {
          "target": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
          "type": "SUBJECT"
        }
      ],
      "meta": {
        "id": "f37d59a3-069e-4f4c-8cc5-a52e73501a76",
        "source": {
           "domainId": "example.domain"
        },
        "time": 1481875988767,
        "type": "EiffelConfidenceLevelModifiedEvent",
        "version": "3.0.0"
      },
      "data": {
        "value": "SUCCESS",
        "customData": [
          {
            "value": "CLM2",
            "key": "name"
          },
          {
            "value": 1,
            "key": "iteration"
          }
        ],
        "name": "performance"
      }
    }

## Extract data from Eiffel event based on rules
Next the specific rule for this event is found and extracted:

    {
      "TemplateName": "ARTIFACT_1",
      "Type": "EiffelConfidenceLevelModifiedEvent",
      "TypeRule": "meta.type",
      "IdRule": "meta.id",
      "StartEvent": "NO",
      "IdentifyRules": "links | [?type=='SUBJECT'].target",
      "MatchIdRules": { "_id": "%IdentifyRulesEventId%" },
      "ExtractionRules": "{  eventId:meta.id,  time:meta.time,  name:data.name,  value:data.value }",
      "MergeResolverRules": "[ {NONEPATH:NONE}, {confidenceLevels: [{ eventId: meta.id }]} ]",
      "ProcessRules": null,
    }

With help of IdentifyRules:

    links | [?type=='SUBJECT'].target

the following object’s id is selected:

    ["6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43"]

But there is no object with such id in the database and the aggregated object
returns empty. The Eiffel event is added to the wait list. There it waits until an object
with the requested id appears in the database. 

## Current aggregation
After some time, the event is fetched again and the whole process starts from the 
beginning. The rule is extracted, and the ids are selected. This time the object 
with the required id exists in the database, but it was already aggregated with 
some other objects. The fetched object looks like below:

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
                    "identity": "pkg:maven/com.othercompany.otherproduct/other-system@1.33.0"
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
          ],
          "publications": [
            {
              "eventId": "33d05e6f-9bd9-4138-83b6-e20cc74680a3",
              "locations": [
                {
                  "type": "PLAIN",
                  "uri": "https://myrepository.com/mySubSystemArtifact"
                }
              ],
              "time": 1481875921763
            }
          ]        
      }

## Data extraction from event 
The required content is extracted from the event as specified in the rule:

    "ExtractionRules": "{  eventId:meta.id,  time:meta.time,  name:data.name,  value:data.value }"

And is put in to the aggregated object in the way it is specified in this rule:

    "MergeResolverRules": "[ {NONEPATH:NONE}, {confidenceLevels: [{ eventId: meta.id }]} ]"

The JSON object with requested data will be put into an array and stored
in the aggregated object with the key “confidenceLevels”. Data in the correct
format will look like below:

    "confidenceLevels": [
      {
         "eventId": "f37d59a3-069e-4f4c-8cc5-a52e73501a76",
         "name": "performance",
         "time": 1481875988767,
         "value": "SUCCESS"
      }
    ]

## Updated aggregation
And the resulting aggregated object will be:

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
                                "issues": []
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
                  "identity": "pkg:maven/com.othercompany.otherproduct/other-system@1.33.0"
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
        ],
        "publications": [
          {
            "eventId": "33d05e6f-9bd9-4138-83b6-e20cc74680a3",
            "locations": [
              {
                "type": "PLAIN",
                "uri": "https://myrepository.com/mySubSystemArtifact"
              }
            ],
            "time": 1481875921763
          }
        ]
    }

## Eiffel events are sent

### 2) f37d59a3-069e-4f4c-8cc5-a52e73501a75
Then a new ConfidenceLevelModified event arrives:

    {
      "links": [
        {
          "target": "40df7fc4-0c40-40a3-a349-9f6da0ba81c5",
          "type": "CAUSE"
        },
        {
          "target": "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
          "type": "SUBJECT"
        }
      ],
      "meta": {
        "id": "f37d59a3-069e-4f4c-8cc5-a52e73501a75",
        "source": {
          "domainId": "example.domain"
        },
        "time": 1481875944272,
        "type": "EiffelConfidenceLevelModifiedEvent",
        "version": "3.0.0"
      },
      "data": {
        "value": "SUCCESS",
        "customData": [
          {
            "value": "CLM3",
            "key": "name"
          },
          {
            "value": 2,
            "key": "iteration"
          }
        ],
        "name": "readyForDelivery"
      }
    }

## Extract data from Eiffel event based on rules
The whole process with finding the configured rule is repeated for this
Eiffel event and the rule is the same as above. With help of IdentifyRule:

    links | [?type=='SUBJECT'].target

the following object’s id is selected:

    ["6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43"]

## Current aggregation 
It is the same event id as in the previous aggregation but some other
aggregations were done during the time between this and previous
ConfidenceLevelModified event appearance. Because of that the object looks like this:

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
                    "identity": "pkg:maven/com.othercompany.otherproduct/other-system@1.33.0"
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
          ],
          "publications": [
            {
              "eventId": "33d05e6f-9bd9-4138-83b6-e20cc74680a3",
              "locations": [
                {
                  "type": "PLAIN",
                  "uri": "https://myrepository.com/mySubSystemArtifact"
                }
              ],
              "time": 1481875921763
            }
          ]
    }

## Data extraction from event 
The required content is extracted from the event as specified in the rule:

    "ExtractionRules": "{  eventId:meta.id,  time:meta.time,  name:data.name,  value:data.value }"

And is put in to the aggregated object in the way it is specified in this rule:

    "MergeResolverRules": "[ {NONEPATH:NONE}, {confidenceLevels: [{ eventId: meta.id }]} ]"

Data in the correct format will look like below:

    "confidenceLevels": [
        {
            "eventId": "f37d59a3-069e-4f4c-8cc5-a52e73501a75",
            "name": "readyForDelivery",
            "time": 1481875944272,
            "value": "SUCCESS"
        }
    ]

But because the aggregated object already contains a key “confidenceLevels”
that contains an array. The JSON object with data will be added to existing
array. New “confidenceLevels” array will look like below:

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
        ]

## Updated aggregation
And the resulting aggregated object will include two JSON objects in the array
called confidenceLevels.

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
                  "identity": "pkg:maven/com.othercompany.otherproduct/other-system@1.33.0"
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
            "testCaseTriggeredTime": 1490777357289,
            "testCaseTriggeredEventId": "6d3df0e0-404d-46ee-ab4f-3118457148f5",
            "outcome": null
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
        ],
        "publications": [
          {
            "eventId": "33d05e6f-9bd9-4138-83b6-e20cc74680a3",
            "locations": [
              {
                "type": "PLAIN",
                "uri": "https://myrepository.com/mySubSystemArtifact"
              }
            ],
            "time": 1481875921763
          }
        ]
    }
