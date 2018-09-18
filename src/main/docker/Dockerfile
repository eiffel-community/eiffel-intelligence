FROM fabric8/java-jboss-openjdk8-jdk:1.2
MAINTAINER Eiffel-Community
USER root
ARG VERSION
RUN echo ${VERSION}
ENV JAVA_APP_DIR=/deployments
ENV JAVA_APP_JAR=eiffel-intelligence-${VERSION}.war
EXPOSE 8091 8778 9779
ADD https://jitpack.io/com/github/eiffel-community/eiffel-intelligence/${VERSION}/eiffel-intelligence-${VERSION}.war /deployments/eiffel-intelligence-${VERSION}.war

