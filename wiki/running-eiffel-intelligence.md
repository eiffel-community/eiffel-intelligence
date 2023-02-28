# Running Eiffel Intelligence

## Prerequisites

Existing Eiffel Intelligence `war` file is expected.
If the file doesn't exist it can be compiled using Maven

    mvn clean package

Note, that this command starts series of test and some of them
expect additional services available. 
Please, see [testing.md](Running Tests) for more details.
To skip the tests append `-DskipTests` option

    mvn clean package -DskipTests

Now the `war` file is available under `target` directory,
named as `eiffel-intelligence-<VERSION>.war`.


## Running with Maven Command
To start Eiffel-Intelligence using Maven command, enter

    mvn spring-boot:run

Additional properties can be added to the `mvn` command directly

    mvn spring-boot:run -Dlogging.level.com.ericsson.ei=DEBUG -Dspring.data.mongodb.port=27019

 ## Running with Java command 
Another option is to use `war` and start it with `java` directly.
See [Prerequisities](#prerequisites) how to build the `war` file.

    java -jar eiffel-intelligence-<VERSION>.war

Own configuration can be provided, too

    java -jar eiffel-intelligence-<version>.war --spring.config.location=<path to own application.properties>

Remember to keep the name of the properties file if you are a beginner to
Spring. More advanced Spring user can look [here](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
for more information about using external configuration.

If a few properties need to be overridden, `java`'s option `-D` can be used for this purpose

    java -jar eiffel-intelligence-<VERSION>.war -Dspring.data.mongodb.port=27019

## Running in Tomcat Instance
To run Eiffel Intelligence in Tomcat, the `war` file must be put into the 
`webapp` folder in Tomcat installation folder, also called Catalina home folder:

    <CATALINA HOME>/webapp/

If Eiffel Intelligence should be run without any context-path in the URL address, 
then overwrite `ROOT.war` file in webapp folder with `eiffel-intelligence-<version>.war`:

    cp eiffel-intelligence-<VERSION>.war <CATALINA HOME>/webapp/ROOT.war

Remove `ROOT` folder in webapp folder

    rm -rf <CATALINA HOME>/webapp/ROOT/

Create `config` folder in catalina home, if it doesn't exist. Spring and 
Eiffel Intelligence will look for the `application.properties` configuration file in config folder

    mkdir <CATALINA HOME>/config

Copy the `application.properties`file into the newly created `config` folder

    cp application.properties (catalina home)/config

Start Tomcat and Eiffel Intelligence in background/daemon mode by executing command

    <CATALINA HOME>/bin/catalina.sh start

To run Tomcat and Eiffel Intelligence with logs printed to console

    <CATALINA HOME>/bin/catalina.sh run

## Eiffel Intelligence Configurations and Properties
All available Eiffel Intelligence properties can be found in [application.properties](../src/main/resources/application.properties) example file.

More documentation of each Eiffel Intelligence property and configurations 
can be found in the [Configuration page](configuration.md)
