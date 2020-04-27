package com.ericsson.ei.rabbitmq.configuration;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/rabbitMQConfiguration.feature", glue = {
        "com.ericsson.ei.rabbitmq.configuration" }, plugin = { "html:target/cucumber-reports/RabbitMQTestConfigurationRunner" })
public class RabbitMQConfigurationTestRunner {

}
