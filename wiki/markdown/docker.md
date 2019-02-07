# Docker

In Eiffel-Intelligence source code a Dockerfile is provided which helps the developer or user to build the local Eiffel-Intellegence source code repository changes to a Docker image.
With the Docker image user can try-out the Eiffel-Intelligence on a Docker Host or in a Kubernetes cluster.

## Requirements
- Docker 


  Linux: https://docs.docker.com/install/linux/docker-ce/ubuntu/

  
  Windows: https://docs.docker.com/docker-for-windows/install/

- Docker Compose
  
  Linux and Windows:  https://docs.docker.com/compose/install/

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

MongoDB, RabbitMq and other Eiffel-Intelligence required components need to running and configured via these application properties that is provided to the docker command above. See the application.properties file for all available/required properties:
[application.properties](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/main/resources/application.properties)


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


Another option to configure Eiffel-Intelligence is to provide the application properties file into the container, which can be made in two ways:
1. Put application.properties file in Tomcat Catalina config folder in container and run Eiffe-Intelligence:

`docker run -p 8070:8080 --expose 8080 --volume /path/to/application.properties:/usr/local/tomcat/config/application.properties eiffel-intelligence:0.0.19`

2. Put application.properties file in a different folder in container and tell EI where the application.properties is located in the container:

`docker run -p 8070:8080 --expose 8080 --volume /path/to/application.properties:/tmp/application.properties -e spring.config.location=/tmp/application.properties eiffel-intelligence:0.0.19`


# Run Docker image with provided docker-compose file
This docker-compose file includes these components, [docker-compose.yml](https://github.com/Ericsson/eiffel-intelligence/blob/master/src/main/docker/docker-compose.yml):
- MongoDb
- RabbitMq
- ER
- EI-Backend (Using the local EI-Backend Docker image build from previous steps)

If you have used a different image tag when you build the EI Backend docker image,
then you need to update docker-compose.yml file.

This line need to changed, in ei_backend service section:

"image: eiffel-intelligence:0.0.19"

To:

"image: \<your image tag\>"

Then run following docker-compose command to startup all components:

`docker-compose -f src/main/docker/docker-compose.yml up -d`

It will take some minutes until all components has started. When all components has loaded, you should be able to access EI-Backend Rest-interfaces with address:
http://localhost:8080/\<EI rest-api endpoint\>

Curl command can be used to make request to EI-Back-end rest-api, example for getting all subscriptions:


`curl -X GET http://localhost:8080/subscriptions`

It is also possible to access these address in web-browser and get result present in a Json view in web-browser.

To get the logs from the EI-Backend container/services, example of getting logs from ei_backend service:

`docker-compose -f src/main/docker/docker-compose.yml logs ei_backend`

All service names can be retreived with following command:

`docker-compose -f src/main/docker/docker-compose.yml config --services`

It is also possible to retrieve the logs by only using "docker logs <container_id or container_name>" command:

`docker logs <container_id or container_name>`

Container id can be retrieved with docker command:

`docker ps`
