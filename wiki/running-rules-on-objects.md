# Running Rules On Objects

|Method|Endpoint                               |
|------|---------------------------------------|
|GET   | /rules                                |
|POST  | /rule-test/run-single-rule                     |
|POST  | /rule-test/run-full-aggregation         |
|GET   | /rule-test |

## Checking the active rules in your backend instance

You can fetch the rules content from the backend to see what rules Eiffel Intelligence is currently using.
This can give you an idea on how to write your own rules or could be saved and used
in the /rule-test/run-single-rule endpoints to test the current rules against your events.

    curl -X GET http://<host>:8090/rules
    
## Testing rules and events in Eiffel Intelligence

Before deploying a new instance of Eiffel Intelligence it is good to test the
desired rules that they give the desired outcome. Therefore it is possible to
test the rules on a test instance of Eiffel Intelligence. This means that this
instance needs to be dedicated to only testing rules and it can be one per
company, organization or department. In this way every user testing will get its own
test space that will be removed after feedback to the user. The property controlling this feature
should be set to true:

    testaggregated.enabled: true

A GUI for this feature is also implemented in [Eiffel Intelligence front-end](https://github.com/eiffel-community/eiffel-intelligence-frontend) 
but only visible when above property is set to true in the backend (this application).

## Check if testRules are enabled on the selected instance

Using the following endpoint one can check if rule tests are enabled in the 
Eiffel Intelligence instance.

    curl -X GET http://<host>:8090/rule-test

## Test JMESPath expression on given Eiffel event

This endpoint allows to test the result of a JMESPath expression of an event.

    POST /rule-test/run-single-rule

**Body (application/json)**

    {
      "rule": <JMESPATH expression>,
      "event" <Eiffel event>:
    }

Examples of this endpoint using curl

    curl -X POST -H "Content-type:application/json" --data @body.json http://localhost:8090/rule-test/run-single-rule

For example if we run the following JMESPath expression:

    {
       id:meta.id,
       type:meta.type,
       time:meta.time,
       identity:data.identity,
       fileInformation:data.fileInformation,
       buildCommand:data.buildCommand
    }

on Eiffel event:

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
          "version": "3.0.0"
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
          "identity": "pkg:maven/com.mycompany.myproduct/component-1@1.5000.0"
       }
    }

will give

    {
      "id": "e90daae3-bf3f-4b0a-b899-67834fd5ebd0",
      "type": "EiffelArtifactCreatedEvent",
      "time": 1484061386383,
      "identity": "pkg:maven/com.mycompany.myproduct/component-1@1.5000.0",
      "fileInformation": [
        {
          "classifier": "",
          "extension": "jar"
        }
      ],
      "buildCommand": null
    }


## Test a list of rule sets on given list of events
This end point is to test a complete aggregation using rule sets for every
event you need to be aggregated. And a list of events. The result is the
aggregated object containing the desired information from the events as you
have specified in the rules.

    POST /rule-test/run-full-aggregation

**Body (application/json)**

    {
       "listEventsJson": <Eiffel Events>,
       "listRulesJson":  <Rules>
    }

Examples of this endpoint using curl

    curl -X POST -H "Content-type: application/json"  --data @body.json  http://localhost:8090/rule-test/run-full-aggregation

For demo you can use following list of [events](../src/test/resources/AggregateListEvents.json) 
and list of [rules](../src/test/resources/AggregateListRules.json). They 
can be used to create a json with the structure as above and when send as 
body to the endpoint you should get following [aggregation result](../src/test/resources/AggregateResultObject.json)
