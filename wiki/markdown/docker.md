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


`docker run -p 8034:8091 --expose 8091 -e server.port=8091 -e logging.level.root=DEBUG -e logging.level.org.springframework.web=DEBUG -e logging.level.com.ericsson.ei=DEBUG eiffel-intelligence-backend:0.1`

# Some info of all flags to this command


## Eiffel Intelligence Spring Properties


<B>"-e server.port=8091"</B> - Is the Spring property setting for Eiffel-Intelligence applications web port.


<B>"-e logging.level.root=DEBUG -e logging.level.org.springframework.web=DEBUG -e 
logging.level.com.ericsson.ei=DEBUG"</B> - These Spring properties set the logging level for the Eiffel-Intelligence applications. 


It is possible to set all Spring available properties via docker envrionment "-e" flag. See the application.properties file for all available Eiffel-Intelligence Spring properties:


[application.properties](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/main/resources/application.properties)


## Docker flags


<B>"--expose 8091"</B> - this Docker flag tells that containers internal port shall be exposed to outside of the Docker Host. This flag do not set which port that should be allocated outside Docker Host on the actual server/machine.


<B>"-p 8034:8091"</B> - this Docker flag is mapping the containers external port 8034 to the internal exposed port 8091. Port 8034 will be allocated outside Docker host and user will be able to access the containers service via port 8034.


When Eiffel-Intelligence container is running on your local Docker host, Eiffel-Intelligence should be reachable with address "localhost:8091/\<Rest End-Point\>" or "\<docker host ip\>:8091/\<Rest End-Point\>"
