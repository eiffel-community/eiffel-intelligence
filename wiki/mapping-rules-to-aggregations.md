When writing subscriptions it is important that you know the structure of the aggregated object. In this section we present how example aggregated objects look like in relation to our existing rules.

[**Artifact Rules**](../src/main/resources/rules/ArtifactRules-Eiffel-Agen-Version.json) 
will result in an [**Artifact Object**](../src/test/resources/AggregatedDocumentInternalCompositionLatestIT.json) 
based on following [**events**](../src/test/resources/ArtifactFlowTestEvents.json) 
from the message bus and following historical [**events**](../src/test/resources/upStreamInput.json) 
from the event repository.

[**Source Change Rules**](../src/main/resources/rules/SourceChangeObjectRules-Eiffel-Agen-Version.json) 
will result in [**Source Change Object**](../src/test/resources/aggregatedSourceChangeObject.json) 
based on following [**events**](../src/test/resources/TestSourceChangeObjectEvents.json) 
from the message bus.

[**Test Execution Rules**](../src/main/resources/rules/TestExecutionObjectRules-Eiffel-Agen-Version.json) 
will result in [**Test Execution Object**](../src/test/resources/aggregatedTestActivityObject.json) 
based on following [**events**](../src/test/resources/TestExecutionTestEvents.json) 
from the message bus.
