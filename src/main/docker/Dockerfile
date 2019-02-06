FROM tomcat:8.0-jre8
MAINTAINER Eiffel-Community

ARG URL
RUN echo Building Eiffel-Intelligence image based on artifact url: ${URL}

# Create image with existing war file. User need to execute 'mvn package -DskipTest' before 'docker build'
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
ADD ./src/main/docker/health-check.sh  /eiffel/health-check.sh
ADD ./src/main/docker/start-service.sh /eiffel/start-service.sh
ADD ${URL} /usr/local/tomcat/webapps/ROOT.war


CMD ["/eiffel/start-service.sh"]
