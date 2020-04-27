# Example Rules

We have created some example rules that represent use cases which triggered
the development of Eiffel Intelligence. The files containing the rules
can be found [**here**](../src/main/resources/rules).

We have also illustrated the Eiffel event flows which these example rules
need, to perform the aggregation. The illustrations contain the events
and how they are linked together. Any upstream events linked from this are looked up from ER if HistoryRules
are defined. Following events in the chain which are linked back to the
ArtifactCreated event will be included in the aggregation, according to
the rules.

## Flow with the Events Required for Artifact Object Flow
The start event for this flow is the ArtifactCreated (ArtC2) event.

<img src="images/ArtifactRules.png">


## Flow with the Events Required for SourceChange Object Flow
The start event for this flow is the SourceChangeSubmitted (SCS1) event.

<img src="images/SourceChangeRules.png">


## Flow with the Events Required for TestExecution Object Flow
The start event for this flow is the ActivityTriggered (ActT) event.

<img src="images/TestExecutionRules.png">


## All Events Rules
In the example "[all event rules](../src/main/resources/rules/AllEventsRules-Eiffel-Agen-Version.json)"
no aggregation is defined. Eiffel Intelligence will simply listen to the
Eiffel events listed, and put them as is in the database.
Each Eiffel event will become a separate object/document in the database,
regardless of whether the events are linked together or not.

These rules are **not meant for production use**, since the database will become
a copy of any other storage implementation for Eiffel events. These example
rules can be used as a base to further extend, and could be a good starting
point when testing out new rules in Eiffel Intelligence.
