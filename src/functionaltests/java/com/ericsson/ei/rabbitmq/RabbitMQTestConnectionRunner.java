package com.ericsson.ei.rabbitmq;

import com.ericsson.ei.utils.BaseRunner;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/functionaltests/resources/features/rabbitMQTestConnection.feature",
        glue = {"com.ericsson.ei.rabbitmq"}, plugin = {
        "html:target/cucumber-reports/RabbitMQTestConnectionRunner"})
public class RabbitMQTestConnectionRunner extends BaseRunner {

}
