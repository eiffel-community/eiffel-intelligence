# Step By Step Aggregation

Two main tasks of the Eiffel Intelligence(EI) are data aggregation and subscription notification. EI flow tests serve as a tool to understand the flow of EI to perform the above-mentioned tasks. In the following text, we go through a flow test step-by-step.

We will use the rules for aggregating a so called Artifact object using the rules defined [here](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/ArtifactRules_new.json).

The types of the events to be aggregated will be:
* EiffelArtifactCreatedEvent
* EiffelTestCaseTriggeredEvent (2 events of this type)
* EiffelTestCaseStartedEvent (2 events of this type)
* EiffelTestCaseFinishedEvent (2 events of this type)
* EiffelArtifactPublishedEvent (2 events of this type)
* EiffelConfidenceLevelModifiedEvent (2 events of this type)
