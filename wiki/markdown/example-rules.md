# Example rules

We have created some example rules that represent use cases which triggered 
the development of Eiffel Intelligence. The files containing the rules 
can be found [**here**](https://github.com/eiffel-community/eiffel-intelligence/tree/master/src/main/resources/rules).

We have also illustrated the Eiffel event flows which these example rules 
need, to perform the aggregation. The illustrations contain the events 
and how they are linked together. 
Any upstream events linked from this are looked up from ER if HistoryRules 
are defined. Following events in the chain which are linked back to the 
ArtifactCreated event will be included in the aggregation, according to
the rules.

## Flow with the events required for Artifact object flow.
The start event for this flow is the ArtifactCreated (ArtC2) event.

<img src="images/ArtifactRules.png">
</img>

## Flow with the events required for SourceChange object flow.
The start event for this flow is the SourceChangeSubmitted (SCS1) event.

<img src="images/SourceChangeRules.png">
</img>

## Flow with the events required for TestExecution object flow.
The start event for this flow is the ActivityTriggered (ActT) event.

<img src="images/TestExecutionRules.png">
</img>