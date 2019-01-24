A: Build Eiffel-Intelligence Docker image based on Eiffel-Intelligence from an Artifactory, e.g. Jitpack:
cd (git root dir)
docker build -t eiffel-intelligence:0.0.19 --build-arg URL=https://jitpack.io/com/github/eiffel-community/eiffel-intelligence/0.0.19/eiffel-intelligence-0.0.19.war -f src/main/docker/Dockerfile .



B: Build Eiffel-Intelligence based on local Eiffel-Intelligence source code changes
1. Build Eiffel-Intelligence service artiface:
cd (git root dir)
mvn package -DskipTests

2. Build Eiffel-Intelligence Docker image:
cd (git root dir)/
docker build -t eiffel-intelligence:0.0.19 --build-arg URL=./target/eiffel-intelligence-0.0.19.war -f src/main/docker/Dockerfile .
