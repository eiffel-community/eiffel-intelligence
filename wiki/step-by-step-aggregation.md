# Step By Step Aggregation

Two main tasks of Eiffel Intelligence (EI) are data aggregation and subscription
notification.

The flow tests for EI (located in this repository) serve as a tool to help
understand the process of performing the above-mentioned tasks.
We will go through a flow test step-by-step to understand what happens in EI,
using the rules for aggregating a so called Artifact object [defined here](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json).
The types of the events to be aggregated in this particular flow will be
the below events, in order. Read more about the aggregation process happening
in EI for each of these Eiffel events.

1. [ArtifactCreated event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/artifact-created-event-aggregation.md) (*start event*)
2. [TestCaseTriggered event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/test-case-triggered-event-aggregation.md) (*2 events of this type*)
3. [TestCaseStarted event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/test-case-started-event-aggregation.md) (*2 events of this type*)
4. [TestCaseFinished event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/test-case-finished-event-aggregation.md) (*2 events of this type*)
5. [ArtifactPublished event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/artifact-published-event-aggregation.md) (*2 events of this type*)
6. [ConfidenceLevelModified event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/confidence-level-modified-event-aggregation.md) (*2 events of this type*)

