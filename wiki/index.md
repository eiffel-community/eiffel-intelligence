# What is Eiffel Intelligence?

## Introduction

Eiffel Intelligence is a service that aggregates information from different
events of a flow with the purpose of notifying subscribers when content of
interest has been collected from desired flow events. The aggregation is stored
in a JSON object, an "aggregated object". By flow we mean a chain of Eiffel
events that are linked together directly or indirectly.

### Aggregated objects

Term "Aggregated objects" in Eiffel Intelligence vocabulary means a composition of
several Eiffel events into one big JSON object using a special rule mechanism.
The main purpose for these aggregations is to create customizable JSON documents
which could be used later used for visualization and better tracing using
different visualization tools. The rules are used to select needed data from Eiffel 
events and inserting it into the aggregated object. 
[Follow a step-by-step aggregation process here](step-by-step-aggregation.md).

### Rules

Eiffel Intelligence uses a set of rules to define what information will be
extracted from an Eiffel event in the flow and at what location to store this
information in the aggregated object. Only one rule set can be run per Eiffel
Intelligence back-end instance and the reason is that in an Eiffel domain with millions of
events flowing we will have multiple extractions and checks for each rule set.
Multiple rule sets in the same service will require large machines and more
difficult optimization. To understand what rules are and how they are used, [read more here](rules.md).

### Subscriptions and notifications

It is possible to create subscriptions to be notified whenever Eiffel events 
with desired content has been sent and aggregated into an object by Eiffel 
Intelligence. Every time an aggregated object is updated with data from a new 
Eiffel event, Eiffel Intelligence checks whether any subscription is fulfilled by
the current state of the aggregated object. Read more about [subscriptions](subscriptions.md).
