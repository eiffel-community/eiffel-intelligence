# Running Rules On Objects

|Method|Endpoint             |
|------|---------------------|
|POST |/rules/rule-check|
|POST | /rules/rule-check/aggregation|

Before deploying a new instance of Eiffel Intelligence it is good to test the desired rules that they give the desired outcome. Therefore it is possible to test the rules on a test instance of Eiffel Intelligence. This means that this instance needs to be dedicated to only testing rules and it can be one per company,organization or department. The property controlling this feature should be set to true:

    testaggregated.enabled: true

In this way every user testing will get its own test space that will be removed after feedback to the user.

A GUI is also implemented in [Eiffel Intelligence Frontend](https://github.com/Ericsson/eiffel-intelligence-frontend) but only visible when above property is set to true in the backend (this application).

## Test JMESPath expression on given Event

This endpoint allows to test the result of a JMESPath expression of an event.

    POST /rules/rule-check

**Body (application/json)**
```javascript
{
  "rule": <JMESPATH Expression>,
  "event" <Eiffel Event>: 
}
```
Examples of this endpoint using curl  

    curl -X POST -H "Content-type:application/json" --data @body.json http://localhost:8090/rules/rule-check

For example if we run the following JMESPath expresion:
```javascript
{
   id:meta.id,
   type:meta.type,
   time:meta.time,
   gav:data.gav,
   fileInformation:data.fileInformation,
   buildCommand:data.buildCommand
}
```
on Eiffel event:
```javascript
{
   "links": [
      {
         "target": "1921bc9f-59e1-45b7-8bb1-26602ed667b1",
         "type": "COMPOSITION"
      },
      {
         "target": "1921bc9f-59e1-45b7-8bb1-26602ed667b1",
         "type": "CAUSE"
      },
      {
         "target": "55da5d47-fb10-43c8-97d2-cbd5eb9a2676",
         "type": "PREVIOUS_VERSION"
      }
   ],
   "meta": {
      "id": "e90daae3-bf3f-4b0a-b899-67834fd5ebd0",
      "source": {
         "domainId": "example.domain"
      },
      "time": 1484061386383,
      "type": "EiffelArtifactCreatedEvent",
      "version": "1.0.0"
   },
   "data": {
      "customData": [
         {
            "value": "ArtCC1",
            "key": "name"
         },
         {
            "value": 5000,
            "key": "iteration"
         }
      ],
      "fileInformation": [
         {
            "classifier": "",
            "extension": "jar"
         }
      ],
      "gav": {
         "artifactId": "component-1",
         "version": "1.5000.0",
         "groupId": "com.mycompany.myproduct"
      }
   }
}
```
will give
```javascript
{
  "id": "e90daae3-bf3f-4b0a-b899-67834fd5ebd0",
  "type": "EiffelArtifactCreatedEvent",
  "time": 1484061386383,
  "gav": {
    "artifactId": "component-1",
    "version": "1.5000.0",
    "groupId": "com.mycompany.myproduct"
  },
  "fileInformation": [
    {
      "classifier": "",
      "extension": "jar"
    }
  ],
  "buildCommand": null
}
```


## Test a list of rule sets on given list of events
This end point is to test a complete aggregation using rule sets for every event you need to be aggregated. And a list of events. The result is the aggregated object containing the desired information from the events as you have specified in the rules.

    POST /rules/rule-check/aggregation



**Body (application/json)**  
 
    {  
       "listEventsJson": <Eiffel Events>,  
       "listRulesJson":  <Rules>  
    }  

Examples of this endpoint using curl  

    curl -X POST -H "Content-type: application/json"  --data @body.json  http://localhost:8090/rules/rule-check/aggregation

For demo you can use following list of [events](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/test/resources/AggregateListEvents.json) and list of [rules](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/test/resources/AggregateListRules.json). They can be used to create a json with the structure as above and when send as body to the endpoint you should get following [aggregation result](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/test/resources/AggregateResultObject.json)
