package com.ericsson.ei.config;


import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLogger.class);

    @Autowired
    private Environment environment;

    @PostConstruct
    public void print() {
        logConfiguration();
    }

    private void logConfiguration() {
        LOGGER.debug("EI Backend started with following configurations:\n"
                + "server.port: " + environment.getProperty("server.port") + "\n"
                + "server.session.timeout: " + environment.getProperty("server.session.timeout") + "\n"
                + "rules.path: " + environment.getProperty("rules.path") + "\n"
                + "rabbitmq.host: " + environment.getProperty("rabbitmq.host") + "\n"
                + "rabbitmq.port: " + environment.getProperty("rabbitmq.port") + "\n"
                + "rabbitmq.user: " + environment.getProperty("rabbitmq.user") + "\n"
                + "rabbitmq.tls.version: " + environment.getProperty("rabbitmq.tls.version") + "\n"
                + "rabbitmq.exchange.name: " + environment.getProperty("rabbitmq.exchange.name") + "\n"
                + "rabbitmq.domain.id: " + environment.getProperty("rabbitmq.domain.id") + "\n"
                + "rabbitmq.component.name: " + environment.getProperty("rabbitmq.component.name") + "\n"
                + "rabbitmq.consumer.name: " + environment.getProperty("rabbitmq.consumer.name") + "\n"
                + "rabbitmq.queue.durable: " + environment.getProperty("rabbitmq.queue.durable") + "\n"
                + "rabbitmq.binding.key: " + environment.getProperty("rabbitmq.binding.key") + "\n"
                + "rabbitmq.wait.list.queue.suffix: " + environment.getProperty("rabbitmq.wait.list.queue.suffix") + "\n"
                + "rules.replacement.marker: " + environment.getProperty("rules.replacement.marker") + "\n"
                + "spring.data.mongodb.host: " + environment.getProperty("spring.data.mongodb.host") + "\n"
                + "spring.data.mongodb.port: " + environment.getProperty("spring.data.mongodb.port") + "\n"
                + "spring.data.mongodb.username: " + environment.getProperty("spring.data.mongodb.username") + "\n"
                + "spring.data.mongodb.database: " + environment.getProperty("spring.data.mongodb.database") + "\n"
                + "sessions.collection.name: " + environment.getProperty("sessions.collection.name") + "\n"
                + "aggregations.collection.name: " + environment.getProperty("aggregations.collection.name") + "\n"
                + "aggregations.collection.ttl: " + environment.getProperty("aggregations.collection.ttl") + "\n"
                + "event.object.map.collection.name: " + environment.getProperty("event.object.map.collection.name") + "\n"
                + "wait.list.collection.name: " + environment.getProperty("wait.list.collection.name") + "\n"
                + "wait.list.collection.ttlValue: " + environment.getProperty("wait.list.collection.ttlValue") + "\n"
                + "wait.list.resend.initial.delay: " + environment.getProperty("wait.list.resend.initial.delay") + "\n"
                + "wait.list.resend.fixed.rate: " + environment.getProperty("wait.list.resend.fixed.rate") + "\n"
                + "subscriptions.collection.name: " + environment.getProperty("subscriptions.collection.name") + "\n"
                + "subscriptions.repeat.handler.collection.name: " + environment.getProperty("subscriptions.repeat.handler.collection.name") + "\n"
                + "test.aggregation.enabled: " + environment.getProperty("test.aggregation.enabled") + "\n"
                + "threads.corePoolSize: " + environment.getProperty("threads.corePoolSize") + "\n"
                + "threads.queueCapacity: " + environment.getProperty("threads.queueCapacity") + "\n"
                + "threads.maxPoolSize: " + environment.getProperty("threads.maxPoolSize") + "\n"
                + "failed.notifications.collection.name: " + environment.getProperty("failed.notifications.collection.name") + "\n"
                + "email.sender: " + environment.getProperty("email.sender") + "\n"
                + "email.subject: " + environment.getProperty("email.subject") + "\n"
                + "notification.retry: " + environment.getProperty("notification.retry") + "\n"
                + "failed.notifications.collection.ttl: " + environment.getProperty("failed.notifications.collection.ttl") + "\n"
                + "spring.mail.host: " + environment.getProperty("spring.mail.host") + "\n"
                + "spring.mail.port: " + environment.getProperty("spring.mail.port") + "\n"
                + "spring.mail.username: " + environment.getProperty("spring.mail.username") + "\n"
                + "spring.mail.properties.mail.smtp.auth: " + environment.getProperty("spring.mail.properties.mail.smtp.auth") + "\n"
                + "spring.mail.properties.mail.smtp.starttls.enable: " + environment.getProperty("spring.mail.properties.mail.smtp.starttls.enable") + "\n"
                + "er.url: " + environment.getProperty("er.url") + "\n"
                + "ldap.enabled: " + environment.getProperty("ldap.enabled") + "\n"
                + "ldap.server.list: " + environment.getProperty("ldap.server.list") + "\n"
                + "logging.level.root: " + environment.getProperty("logging.level.root") + "\n"
                + "logging.level.org.springframework.web: " + environment.getProperty("logging.level.org.springframework.web") + "\n"
                + "logging.level.com.ericsson.ei: " + environment.getProperty("logging.level.com.ericsson.ei") + "\n"
                + "\nThese properties are only some of the configurations, more configurations could have been provided.\n");
    }
}
