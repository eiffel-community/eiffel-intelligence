# Docker

In Eiffel-Intelligence source code a Dockerfile is provided which helps the developer or user to build the local Eiffel-Intellegence source code repository changes to a Docker image.
With the Docker image user can try-out the Eiffel-Intelligence on a Docker Host or in a Kubernetes cluster.

## Requirements
- Docker 


  Linux: https://docs.docker.com/install/linux/docker-ce/ubuntu/

  
  Windows: https://docs.docker.com/docker-for-windows/install/

## Follow these step to build the Docker image.

1. Build the Eiffel-intelligence war file: 
`mvn package -DskipTests` 


This will produce a war file in the "target" folder.


2. Build the Docker image with the war file that was produced from previous step: 


`docker build -t eiffel-intelligence:0.0.19 --build-arg URL=./target/eiffel-intelligence-0.0.19.war -f src/main/docker/Dockerfile .` 


Now docker image has build with tag "eiffel-intelligence-backend:0.1"

## Run Docker image on local Docker Host
To run the produced docker image on the local Docker host, execute this command: 


`docker run -p 8070:8080 --expose 8080 -e server.port=8080 -e logging.level.log.level.root=DEBUG -e logging.level.org.springframework.web=DEBUG -e logging.level.com.ericsson.ei=DEBUG -e spring.data.mongodb.host=eiffel2-mongodb -e spring.data.mongodb.port=27017 eiffel-intelligence:0.0.19`


# Some info of all flags to this command


## Eiffel Intelligence Spring Properties


<B>"-e server.port=8080"</B> - Is the Spring property setting for Eiffel-Intelligence applications web port.


<B>"-e logging.level.root=DEBUG -e logging.level.org.springframework.web=DEBUG -e 
logging.level.com.ericsson.ei=DEBUG"</B> - These Spring properties set the logging level for the Eiffel-Intelligence applications. 


It is possible to set all Spring available properties via docker envrionment "-e" flag. See the application.properties file for all available Eiffel-Intelligence Spring properties:


[application.properties](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/main/resources/application.properties)


## Docker flags


<B>"--expose 8080"</B> - this Docker flag tells that containers internal port shall be exposed to outside of the Docker Host. This flag do not set which port that should be allocated outside Docker Host on the actual server/machine.


<B>"-p 8070:8080"</B> - this Docker flag is mapping the containers external port 8034 to the internal exposed port 8091. Port 8034 will be allocated outside Docker host and user will be able to access the containers service via port 8034.


When Eiffel-Intelligence container is running on your local Docker host, Eiffel-Intelligence should be reachable with address "localhost:8070/\<Rest End-Point\>" or "\<docker host ip\>:8070/\<Rest End-Point\>"


Another option to configure Eiffel-Intelligence is with a provided application properties file, which can be made in two ways:
1. Put application.properties file in Tomcat Catalina config folder and run Eiffe-Intelligence:
`docker run -p 8070:8080 --expose 8080 --volume /path/to/application.properties:/usr/local/tomcat/application.properties eiffel-intelligence:0.0.19`

2. Put application.properties file in a different folder in container and tell EI where the application.properties is located:
`docker run -p 8070:8080 --expose 8080 --volume /path/to/application.properties:/tmp/application.properties -e spring.config.location=/tmp/application.properties eiffel-intelligence:0.0.19`

