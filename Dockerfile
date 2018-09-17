FROM fabric8/java-jboss-openjdk8-jdk:1.2
MAINTAINER Eiffel-Community
USER root
ARG EIFFEL_INTELLIGENCE_VERSION
RUN echo ${EIFFEL_INTELLIGENCE_VERSION}
ENV JAVA_APP_DIR=/deployments
ENV JAVA_APP_JAR=eiffel-intelligence-${EIFFEL_INTELLIGENCE_VERSION}.war
EXPOSE 8091 8778 9779
ADD https://jitpack.io/com/github/ericsson/eiffel-intelligence/${EIFFEL_INTELLIGENCE_VERSION}/eiffel-intelligence-${EIFFEL_INTELLIGENCE_VERSION}.war /deployments/eiffel-intelligence-${EIFFEL_INTELLIGENCE_VERSION}.war

