
# _Introduction_

Eiffel Intelligence is a service that aggregates information from different events of a flow with the purpose of notifying subscribers when content of interest has been collected from desired flow events. The aggregation is stored in a JSON object. By flow we mean a chain of Eiffel events that are linked together directly or indirectly. 

Eiffel Intelligence uses a set of rules to define what information will be extracted from an Eiffel event in the flow and at what location to store this information in the aggregated object. Today only one rule set can be run in each instance. The reason is that in an Eiffel domain with millions of events flowing we will have multiple extractions and checks for each rule set. Multiple rule sets in same service instance will require large machines and more difficult optimization.

Eiffel intelligence uses subscriptions to notify interested parties when several events have occurred with desired content in each event aggregated in an aggregated object. Every time an aggregated object is updated we check whether any subscription is fulfilled by the current state of the aggregated object. 

[Under the hood](under_hood.md)

[Under the hood](under_hood.md)

[Rules](rules.md)

[Subscriptions](subscriptions.md)

[Starting Eiffel Intelligence](starting.md)
