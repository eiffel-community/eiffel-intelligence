package com.ericsson.ei.rabbitmq;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/rabbitMQConfiguration.feature", glue = {
        "com.ericsson.ei.rabbitmq" }, plugin = { "html:target/cucumber-reports/RabbitMQConfigurationTestRunner" })
public class RabbitMQConfigurationTestRunner {

}
