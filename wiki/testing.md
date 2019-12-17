# Running Tests

Eiffel Intelligence backend has unit tests, functional tests and at the moment
of writing this a system tests. This page is intended for developers used with
Maven so we assume the reader has some knowledge about Java development with Maven.

## Running Unit Tests and Functional Tests

All unit tests and functional tests are run automatically when EI is compiled with

    mvn clean install

If you only want to run specific unit tests:

    mvn -DsomeModule.test.includes="**/QueryServiceTest.java, **/ERQueryServiceTest.java" test

If you only want to run specific functional tests:

    mvn -DsomeModule.test.includes="**/QueryAggregatedObjectsTestRunner.java, **/TestScalingAndFailoverRunner.java" test

To exclude some tests and run all the others:

    mvn -DsomeModule.test.excludes="**/QueryAggregatedObjectsTestRunner.java, **/TestScalingAndFailoverRunner.java" test

## Running Integration Test

To run the integration test an instance of RabbitMQ, MongoDB and Event
Repository must be up, running and accessible from the machine where the test
is started, often localhost.

The default port for:

    RabbitMQ is 5672
    MongoDB is 27017
    EventRepository is 8080

If the above applications are started at the default ports then the system test
can be started with.

    mvn verify -DskipUnitTests

If the ports above are different then you need to pass them as Java options to
Maven and eventually the host to the instances if they are not started on localhost

    mvn verify -DskipUnitTests -Drabbitmq.host=<rabbitmq host if other than localhost> -Drabbitmq.port=<rabbitMq Port> -Dspring.data.mongodb.uri=<mongodb uri if other than localhost:27017> -Devent.repository.url=<url to search endpoint of ER instance>
