# Running Eiffel Intelligence

## _Prerequisites_

Eiffel intelligence is Spring microservice distributed in a war file. To start 
the service download the war file from 
[jitpack.io](https://jitpack.io/#Ericsson/eiffel-intelligence) and look for the 
latest version. Now replace the latest version in the link below:

    https://jitpack.io/com/github/Ericsson/eiffel-intelligence/<version>/eiffel-intelligence-<version>.war

If you want to test the latest code in github clone the project and compile it 
with:

    mvn clean install

append **_-DskipTests_** if you want to skip the tests since the latest on 
master always has passed the tests. The war file should now be found under 
target folder in your cloned project.

Now that you have the executable run :

    java -jar eiffel-intelligence-<version>.war

if you want to run with default configuration for local message buss and 
Mongo DB.

Own configuration can be provided with 

    java -jar eiffel-intelligence-<version>.war --spring.config.location=file:<path to own application.properties>

remember to keep the name of the properties file if you are a beginner to 
Spring. More advanced Spring user can look [here](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) 
for more information about using external configuration.

If only few properties need to be overridden then use java opts, for example

    java -jar eiffel-intelligence-<version>.war -Dspring.data.mongodb.port=27019
