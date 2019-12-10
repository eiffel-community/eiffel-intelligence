When writing subscriptions it is important that you know the structure of the aggregated object. In this section we present how example aggregated objects look like in relation to our existing rules.


[**Artifact Rules**](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json) will result in an [**Artifact Object**](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/test/resources/AggregatedDocumentInternalCompositionLatestIT.json) based on following [**events**](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/test/resources/ArtifactFlowTestEvents.json) from message bus and following historical [**events**](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/test/resources/upStreamInput.json) from event repository.


[**Source Change Rules**](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/rules/SourceChangeObjectRules-Eiffel-Agen-Version.json) will result in [**Source Change Object**](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/test/resources/aggregatedSourceChangeObject.json) based on following [**events**](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/test/resources/TestSourceChangeObjectEvents.json) from message bus.


[**Test Execution Rules**](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/main/resources/rules/TestExecutionObjectRules-Eiffel-Agen-Version.json) will result in [**Test Execution Object**](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/test/resources/aggregatedTestActivityObject.json) based on following [**events**](https://github.com/eiffel-community/eiffel-intelligence/blob/master/src/test/resources/TestExecutionTestEvents.json) from message bus.
