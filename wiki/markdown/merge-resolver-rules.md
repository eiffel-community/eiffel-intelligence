# Merge Resolver Rules

JMESPath identifier of the place where to insert the JSON object from 
_"ExtractionRules"_. If MergeResolverRules is null _"ExtractionRules"_ object 
will be inserted to the root of aggregated object. 

The location in the aggregated object where the extracted content will be merge 
can be specified with path elements and a value to exist at that, but not all 
elements of a path are needed. The aggregated object will be flattened and all 
paths that contains the path elements in the given order will be collected. 
Then we check the value and the path with the value is the winning path. If no 
value from the paths match the given value then we take the longest path that 
contains the given path elements.

Example object:

    {
       "id":"eventId",
       "fakeArray":[
          {
             "event_id":"fakeId",
             "fake_data":"also_fake"
          }
       ],
       "level1":{
          "property1":"p1value",
          "level2":{
             "property2":"p2value",
             "lvl2Array":[
                {
                   "oneElem":"oneElemValue",
                   "2ndElem":{
                      "3rdElem":"3rdElemValue",
                      "artifacts":[
                         {
                            "event_id":"artifact_id_1",
                            "artifact_data":"artifact1data"
                         },
                         {
                            "event_id":"artifact_id_2",
                            "artifact_data":"artifact2data"
                         }
                      ]
                   }
                }
             ]
          }
       },
       "type":"eventType",
       "test_cases":[
          {
             "event_id":"testcaseid1",
             "test_data":"testcase1data"
          },
          {
             "event_id":"testcaseid2",
             "test_data":"testcase2data"
          }
       ]
    }

## Example 1:

    "MergeResolverRules" : "{level2:{event_id: %IdentifyRules%}}"

will result after replacing the marker by EI in rule:

    {level2:{event_id: someLevelId}}

With extracted content:
    
    "{test_time: some_time, test_name: some_name}"

We get the below object to append to the root. We have also identified that 
level2 exists under level1 and so property2 will get three more siblings.

    {
       level1:{
          level2:{
             test_time:some_time,
             event_id:someLevelId,
             test_name:some_name
          }
       }
    }

The same result could have been achieved with merge rule:

    "MergeResolverRules" : "{level2:{property2: p2value}}"

and extracted content:

    "{test_time: some_time, test_name: some_name, event_id: someLevelId}"

## Example 2 - array aggregations:

For aggregating arrays we use an array of JSON objects as merge rule. 

     "MergeResolverRules" :"[{NONEPATH:NONE},  {test_suite: [{test_suite_started_event_id: meta.id}]} ]" 

and content object:

    {test_suite_started_event_id: some_id, test_suite_name : some_name }    

The first element in the array will be used to find the location where the new 
array will be stored. If that location is not found then the root will be 
considered as the location for the array aggregation. In this example we have a 
path that will not be found in an aggregated object so an array with key name 
test_suite will be created and its first element will be the content object.

    "MergeResolverRules": "[{test_suite_started_event_id: links | [?type=='IUT'] | [0].target},  {test_case : [{ test_case_triggered_event_id:meta.id}] }]",

The rule above helps us create an array for test cases or append a test case to 
a certain test suite identified by the linked id.
