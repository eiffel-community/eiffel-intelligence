# Configuration

Address and port for message buss and MongoDB needs to be configured in 
[application.properties](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/main/resources/application.properties) 
for the application to run properly.

Each property is explained in the file so open the properties file for a view 
on properties role.

## Setting up multiple EI instances

Eiffel intelligence is designed to be able to collect different information in different objects. What information to be collected from what events to what object is configured using a set of rules.

### Set up multiple instances with different rule sets in each instance

In this case we use the same instance of Rabbitmq and MongoDb.

  - rabbitmq.consumerName property should be different for each rule set. Otherwise the rabbitmq will split the events in the queue among all the instances listening to that queue
  - MongoDb collection names should also be different for each rule set
  
### Set up multiple instances with same rule set

This situation may be needed when the events throughput is very high. In this case the same configuration file is copied to the server where the extra instance will be started. 
  
  
  Both situations are presented in the picture below.
  
<img src="images/multiple_EI_instances.png">
</img>
