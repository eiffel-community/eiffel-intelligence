## A: Build Eiffel Intelligence Docker image based on Eiffel Intelligence from an Artifactory, e.g. Jitpack:
cd (git root dir)
docker build -t eiffel-intelligence-backend:1.0.1 --build-arg URL=https://jitpack.io/com/github/eiffel-community/eiffel-intelligence/1.0.1/eiffel-intelligence-1.0.1.war -f src/main/docker/Dockerfile .



## B: Build Eiffel Intelligence based on local source code changes
1. Build Eiffel-Intelligence service artifact:
cd (git root dir)
mvn package -DskipTests

2. Build Eiffel Intelligence Docker image:
cd (git root dir)/
export EIFFEL_WAR=$(ls target/*.war)
docker build -t eiffel-intelligence-backend --build-arg URL=./${EIFFEL_WAR} -f src/main/docker/Dockerfile .


## Use docker-compose to set up environment for EI testing

The docker-compose file in this directory can be used to set up the proper
environment for running the integration tests on EI backend. It also includes
an Eiffel Intelligence backend pre-configured.

Standing in the root directory, run the below command to set up environment:

  docker-compose -f src/main/docker/docker-compose.yml up -d

