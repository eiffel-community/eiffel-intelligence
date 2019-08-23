# Running Eiffel Intelligence

## _Prerequisites_

Eiffel intelligence is Spring microservice distributed in a war file.

Released Eiffel Interlligence front-end war files can be downloaded from Jitpack:
[jitpack.io](https://jitpack.io/#Ericsson/eiffel-intelligence) and look for the
latest version. Now replace the latest version in the link below:

    https://jitpack.io/com/github/Ericsson/eiffel-intelligence/<version>/eiffel-intelligence-<version>.war

## Running with maven command

If you want to test the latest code in github clone the project and compile it
with:

    mvn clean install

append **_-DskipTests_** if you want to skip the tests since the latest on
master always has passed the tests. The war file should now be found under
target folder in your cloned project.

If you run from source code, you can run Eiffel-Intelligence front-end with maven command:

    mvn spring-boot:run

With properties added to the maven command, e.g:
    
    mvn spring-boot:run -Dlogging.level.com.ericsson.ei=DEBUG -Dspring.data.mongodb.port=27019

 ## Running with java command 

Another option is to run the executable war file with java command.
If running from source code, war file is generated and produced by maven command (mvn install command can be used as well):

    mvn package -DskipTests

 This command should produce a eiffel-intelligence-<version>.war file in target folder, target/eiffel-intelligence-<version>.war. 

War file is executed by following command and with default configuration: 

    java -jar eiffel-intelligence-<version>.war

Own configuration can be provided with

    java -jar eiffel-intelligence-<version>.war --spring.config.location=file:<path to own application.properties>

remember to keep the name of the properties file if you are a beginner to
Spring. More advanced Spring user can look [here](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
for more information about using external configuration.

If only few properties need to be overridden, then use java opts, for example:

    java -jar eiffel-intelligence-<version>.war -Dspring.data.mongodb.port=27019

## Running in Tomcat instance

To run Eiffel-Intelligence in Tomcat, the war file must be put into the webapp folder in tomcat installation folder, also called catalina home folder:

    (catalina home)/webapp/

If Eiffel-Intelligence should be run without any conext-path in the url address, then overwrite ROOT.war file in webapp folder with eiffel-intelligence-<version>.war:

    cp eiffel-intelligence-<version>.war (catalina home)/webapp/ROOT.war

Remove "ROOT" folder in webapp folder:

    rm -rf (catalina home)/webapp/ROOT/

Create "config" folder in webapp folder, if it not exists. Spring and Eiffel-Intelligence will look for the applications.properties configuration file from this config folder:

    mkdir (catalina home)/webapp/config

Copy the application.properties file into the newly created config folder:
    
    cp applications.properties (catalina home)/webapp/config

Start Tomcat and Eiffel-Intelligence in background/daemon mode by exectuing command:

    (catalina home)/bin/catalina.sh start

To run Tomcat and Eiffel-Intelligence with logs printed to console:
    
    catalina home)/bin/catalina.sh run

## Eiffel-Intelligence fron-tend configurations and properties

All available Eiffel-Intelligence properties can be found in [application.properties](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/main/resources/application.properties) example file.

More documentation of each Eiffel-Intelligence properties and configurations can be found in [Configuration page](./configuration.md)

