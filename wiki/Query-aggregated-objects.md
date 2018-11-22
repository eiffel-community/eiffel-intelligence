# Query Aggregated Objects

Query aggregated objects can be done using two endpoints.

## The first endpoint uses several criteria

    http://<host adress>:<host port>/query

1. The criteria can be passed as URL parameter in a get request

        http://<host adress>:<host port>/query?request="testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43"

2. The criteria can be passed as form body in a post request, example of body is presented below. _Options_ section is optional.
``` javascript
// This returns all objects that fulfills all three criteria. 
// Notice that this is the same as just sending the id as criteria.
// Since there will not be any other object with same id
{
   "criteria":{
      "testCaseExecutions.testCase.verdict":"PASSED",
      "testCaseExecutions.testCase.id":"TC5"
   },
   "options":{
      "id":"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43"
   }
}
``` 

``` javascript
// This returns all objects where the verdict is PASSED
// and the test case id is either TC5 or TC6. 
{
   "criteria":{
      "testCaseExecutions.testCase.verdict":"PASSED",
      "$or":[
         {
            "testCaseExecutions.testCase.id":"TC5"
         },
         {
            "testCaseExecutions.testCase.id":"TC6"
         }
      ]
   }
}
``` 


## The second endpoint is more strict and the user must provide the id of the event that started the aggregated object.

    http://<host adress>:<host port>/queryAggregatedObject?ID=6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43
