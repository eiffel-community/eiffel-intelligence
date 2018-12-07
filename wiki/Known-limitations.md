# Known Limitations

1. All events that contribute to an aggregated object are mapped to the id of 
the event that started the aggregation. But if you have an event belonging to a 
flow and link to an event that was skipped then we will not be able to found 
the target aggregated object to append the extracted event data to. It is 
therefore important that all events desired to be aggregated must link to at 
least one previously aggregated event. In some cases you might need to 
aggregated parts of an event, ex. the id, just to make it possible for future 
events to be aggregated.

We can take the following chain of events:

* event1 - start event and it will be aggregated
* event2 - rules defined for its type and links to event1, it will be aggregated
* event3 - it links to event2, rules not defined for its type so it will not be aggregated
* event4 - it links to event3, rules defined for its type but since event3 is not aggregated then the aggregation chain is broken and this event will not be aggregated since we have no ways to find the start event for it.
