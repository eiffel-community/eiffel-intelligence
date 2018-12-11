# History Rules

**History Rules** below are used if you need to aggregate data from existing 
events linked upstream by received event.  An API to **ER (Event Repository)** 
that returns the historical events in right format must be configured in 
application.properties.

**HistoryExtractionRules** - JSON object of JMESPath identifier(s) which will 
create or modify the existing data in aggregated object for internal 
composition, same as "ExtractionRules".

**HistoryPathRules** - JMESPath identifier of the place where to insert the 
JSON object from "HistoryExtractionRules" in aggregated object, same as 
"MergeResolverRules" but it is relative to its position in the tree path 
returned from ER. The path for merging history data will get as long as the 
depth of the tree where your historical event exists.

A step by step example will be presented using [artifact aggregation rules](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/main/resources/ArtifactRules_new.json).

Assume that an EiffelArtifactCreatedEvent is received and the upstream response 
tree looks like [this](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/test/resources/upStreamResultFile.json).

The starting aggregated object is :

    {
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

The first event to be traversed in the tree is an EiffelCompositionDefinedEvent 
and the JSON object extracted from it with **HistoryExtractionRules** is 

    {"eventId":"fb6ef12d-25fb-4d77-b9fd-87688e66de47","time":2000,"name":"My composition"}

and it will be appended at following location in aggregated object

    internalComposition.compositions.0

This location is at the root object since this event is under the start 
EiffelArtifactCreatedEvent.

The new aggregated object is now:

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
       // internalComposition is the new addition
       "internalComposition":{
          "compositions":[
             {
                "eventId":"fb6ef12d-25fb-4d77-b9fd-87688e66de47",
                "name":"My composition",
                "time":2000
             }
          ]
       },
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

The next event to be traversed  is an EiffelArtifactCreatedEvent that previous 
EiffelCompositionDefinedEvent links to and the JSON object extracted from it 
with **HistoryExtractionRules** is 

    {
       "id":"1100572b-c3j4-441e-abc9-b62f48080011",
       "gav":{
          "version":"1.33.0",
          "artifactId":"other-system",
          "groupId":"com.othercompany.otherproduct"
       },
       "fileInformation":[
          {
             "extension":"jar",
             "classifier":"debug"
          }
       ]
    }



and it will be appended at following location in aggregated object

   internalComposition.compositions.0.artifacts.0

_**artifacts.0**_ is given by **HistoryPathRules** for 
EiffelArtifactCreatedEvent and has been appended to previous path.

The resulting aggregated object is now:

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
                // artifacts is the new addition
                "artifacts":[
                   {
                      "fileInformation":[
                         {
                            "extension":"jar",
                            "classifier":"debug"
                         }
                      ],
                      "id":"1100572b-c3j4-441e-abc9-b62f48080011",
                      "gav":{
                         "groupId":"com.othercompany.otherproduct",
                         "artifactId":"other-system",
                         "version":"1.33.0"
                      }
                   }
                ]
             }
          ]
       },
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

We continue with aggregating the **EiffelCompositionDefinedEvent** linked by 
latest EiffelArtifactCreatedEvent we aggregated. Its **HistoryExtractionRule** 
results in:

    {"eventId":"fb6ef12d-25fb-4d77-b9fd-87688e66da4j","time":5005,"name":"Other composition"}

with path for merge:

    internalComposition.compositions.0 

which appended to previous path become:

    internalComposition.compositions.0.artifacts.0.internalComposition.compositions.0

and the new aggregated object is:
    
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
                      // internalComposition is the new addition
                      "internalComposition":{
                         "compositions":[
                            {
                               "eventId":"fb6ef12d-25fb-4d77-b9fd-87688e66da4j",
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
                   }
                ]
             }
          ]
       },
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

You can see that this latest _internalComposition_ section was added to the 
latest artifacts element in the upper _internalComposition_ section. This is 
also a reflection of the upstream tree returned from _Event Repository_.

We move further down the tree and will aggregate 
**EiffelSourceChangeSubmittedEvent** and after applying its 
**HistoryExtractionRule** we get 

    {"SCSEventId":"3ce9df6e-cd45-4320-ae66-945f038caa1b","gitIdentifier":null,"submitter":{"name":"Jane Doe","email":"jane.doe@company.com"}}

with relative path for merge

    sourceChanges.0

which will give us the absolute path for merge

    internalComposition.compositions.0.artifacts.0.internalComposition.compositions.0.sourceChanges.0


and the aggregated object will now look like:

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
                               // sourceChanges is the new addition
                               "sourceChanges":[
                                  {
                                     "submitter":{
                                        "name":"Jane Doe",
                                        "email":"jane.doe@company.com"
                                     },
                                     "SCSEventId":"3ce9df6e-cd45-4320-ae66-945f038caa1b",
                                     "gitIdentifier":null
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
                   }
                ]
             }
          ]
       },
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

Now it is time to aggregate the **EiffelSourceChangeCreatedEvent** and its 
**HistoryExtractionRule** gives us following object to merge:

    {
       "SCCEventId":"ac085e24-ac4c-41be-912d-08c7afd32285",
       "author":{
          "name":"John Doe",
          "email":"john.doe@company.com",
          "id":"johndoe",
          "group":"Team Gophers"
       },
       "issues":[
    
       ]
    }

with relative path 

    sourceCreations.0

which will give us the absolute path for merge

    internalComposition.compositions.0.artifacts.0.internalComposition.compositions.0.sourceChanges.0.sourceCreations.0

and the aggregated object will now look like:

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
                                     "SCSEventId":"3ce9df6e-cd45-4320-ae66-945f038caa1b",
                                     "gitIdentifier":null,
                                     // sourceCreations is the new addition
                                     "sourceCreations":[
                                        {
                                           "SCCEventId":"ac085e24-ac4c-41be-912d-08c7afd32285",
                                           "author":{
                                              "name":"John Doe",
                                              "id":"johndoe",
                                              "email":"john.doe@company.com",
                                              "group":"Team Gophers"
                                           },
                                           "issues":[
    
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
                   }
                ]
             }
          ]
       },
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

Since the tree returns the first **EiffelSourceChangeCreatedEvent** we 
aggregate following information from it too.
    
    {
       "SCCEventId":"552ad6a4-c522-47e2-9195-0481930979e4",
       "author":null,
       "issues":[
          {
             "type":"BUG",
             "tracker":"JIRA",
             "id":"JIRA-1234",
             "uri":"http://jira.company.com/browse/JIRA-1234",
             "transition":"RESOLVED"
          }
       ]
    }

with relative path:

    sourceCreations.1

and absolute path:

    internalComposition.compositions.0.artifacts.0.internalComposition.compositions.0.sourceChanges.0.sourceCreations.1.SCCEventId


which gives us the following aggregated object:

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
                                     "SCSEventId":"3ce9df6e-cd45-4320-ae66-945f038caa1b",
                                     "gitIdentifier":null,
                                     "sourceCreations":[
                                        {
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
                                        // This second entry in sourceCreations is the new additions
                                        {
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
                   }
                ]
             }
          ]
       },
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

Now we return back in the tree and process the second 
**EiffelArtifactCreatedEvent** making the first composition. So we will append:

    {
       "id":"4400572b-c3j4-441e-abc9-b62f48080033",
       "gav":{
          "version":"1.99.0",
          "artifactId":"internal-system",
          "groupId":"com.internalcompany.internalproduct"
       },
       "fileInformation":[
          {
             "extension":"jar",
             "classifier":"debug"
          }
       ]
    }

with relative path:

    artifacts.1

and absolute path:

   internalComposition.compositions.0.artifacts.1

This results in following aggregated object:

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
                                     "SCSEventId":"3ce9df6e-cd45-4320-ae66-945f038caa1b",
                                     "gitIdentifier":null,
                                     "sourceCreations":[
                                        {
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
                   // this second artifact information is the new addition to 
                   // internal composition 
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

At last we aggregate the **EiffelArtifactCreatedEvent** that the received
**EiffelArtifactCreatedEvent** links to. The same HistoryExtractionRule is
used by all **EiffelArtifactCreatedEvent** and here we will get:

    {
       "id":"1100572b-c3b4-461e-abc9-b62f48087011",
       "gav":{
          "version":"1.33.0",
          "artifactId":"other-system",
          "groupId":"com.othercompany.secondproduct"
       },
       "fileInformation":[
          {
             "extension":"jar",
             "classifier":"debug"
          }
       ]
    }

to merge at relative path "" and absolute path:

    artifacts.0

The final aggregated object is now:

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
                                     "SCSEventId":"3ce9df6e-cd45-4320-ae66-945f038caa1b",
                                     "gitIdentifier":null,
                                     "sourceCreations":[
                                        {
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
       "TemplateName":"ARTIFACT_1",
       "id":"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43",
       "time":1481875891763,
       "type":"EiffelArtifactCreatedEvent",
       "gav":{
          "groupId":"com.mycompany.myproduct",
          "artifactId":"sub-system",
          "version":"1.1.0"
       },
       // artifacts array is the last addition now
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
