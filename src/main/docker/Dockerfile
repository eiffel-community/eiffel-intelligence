FROM fabric8/java-jboss-openjdk8-jdk:1.2
MAINTAINER Eiffel-Community
USER root
ENV JAVA_APP_DIR=/deployments
ENV JAVA_APP_JAR=eiffel-intelligence.war
EXPOSE 8091 8778 9779

# For build war file inside conatianer
#RUN yum install -y maven
#RUN mkdir temp
#COPY .  temp
#RUN ls temp
#RUN pwd
#RUN mvn package -DskipTests -f temp/pom.xml
#RUN cp temp/target/eiffel-intelligence-.*.war /deployments/eiffel-intelligence.war

# Create image with existing war file. User need to execute 'mvn package -DskipTest' before 'docker build'
COPY ./target/eiffel-intelligence-*.war /deployments/eiffel-intelligence.war

CMD /deployments/run-java.sh


