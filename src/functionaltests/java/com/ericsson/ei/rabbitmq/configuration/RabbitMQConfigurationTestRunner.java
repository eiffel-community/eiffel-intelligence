package com.ericsson.ei.rabbitmq.configuration;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;



@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/rabbitMQConfiguration.feature", glue = {
        "com.ericsson.ei.rabbitmq.configuration" }, plugin = { "html:target/cucumber-reports/RabbitMQTestConfigurationRunner" })
public class RabbitMQConfigurationTestRunner {

}
