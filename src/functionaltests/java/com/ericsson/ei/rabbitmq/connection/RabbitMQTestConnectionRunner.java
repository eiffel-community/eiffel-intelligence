package com.ericsson.ei.rabbitmq.connection;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/rabbitMQTestConnection.feature", glue = {
        "com.ericsson.ei.rabbitmq.connection" }, plugin = { "html:target/cucumber-reports/RabbitMQTestConnectionRunner" })
public class RabbitMQTestConnectionRunner {

}
