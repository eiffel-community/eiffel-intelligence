# Under the hood

A more detailed description of what it happens with an event is described on 
this page. 

When the service is [started](starting.md) it will connect to configured Mongo 
DB instance and message bus. The message bus instance must be the same you use 
to publish the events to. If several message buses are used then it is needed 
to federate these into one more extra message bus and configure Eiffel 
Intelligence to use the federated message bus.

When an event is received 
