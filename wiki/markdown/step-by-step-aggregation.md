# Step By Step Aggregation

Two main tasks of the Eiffel Intelligence(EI) are data aggregation and
subscription notification. EI flow tests serve as a tool to understand the
flow of EI to perform the above-mentioned tasks. In the following text, we go
through a flow test step-by-step.

We will use the rules for aggregating a so called Artifact object using the
rules defined [here](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json).

The types of the events to be aggregated will be:
* EiffelArtifactCreatedEvent (start event)

* EiffelTestCaseTriggeredEvent (2 events of this type)

* EiffelTestCaseStartedEvent (2 events of this type)

* EiffelTestCaseFinishedEvent (2 events of this type)

* EiffelArtifactPublishedEvent (2 events of this type)

* EiffelConfidenceLevelModifiedEvent (2 events of this type)


* [ArtifactCreated event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/artifact-created-event-aggregation.md)
* [TestCaseTriggered event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/test-case-triggered-event-aggregation.md)
* [TestCaseStarted event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/test-case-started-event-aggregation.md)
* [ArtifactPublished event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/artifact-published-event-aggregation.md)
* [ConfidenceLevelModified event aggregation](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/confidence-level-modified-event-aggregation.md)
* [event aggregation]()

