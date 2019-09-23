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
                + "server.session-timeout: " + environment.getProperty("server.session-timeout") + "\n"
                + "rules.path: " + environment.getProperty("rules.path") + "\n"
                + "rabbitmq.host: " + environment.getProperty("rabbitmq.host") + "\n"
                + "rabbitmq.port: " + environment.getProperty("rabbitmq.port") + "\n"
                + "rabbitmq.user: " + environment.getProperty("rabbitmq.user") + "\n"
                + "rabbitmq.tlsVersion: " + environment.getProperty("rabbitmq.tlsVersion") + "\n"
                + "rabbitmq.exchange.name: " + environment.getProperty("rabbitmq.exchange.name") + "\n"
                + "rabbitmq.domainId: " + environment.getProperty("rabbitmq.domainId") + "\n"
                + "rabbitmq.componentName: " + environment.getProperty("rabbitmq.componentName") + "\n"
                + "rabbitmq.consumerName: " + environment.getProperty("rabbitmq.consumerName") + "\n"
                + "rabbitmq.queue.durable: " + environment.getProperty("rabbitmq.queue.durable") + "\n"
                + "rabbitmq.binding.key: " + environment.getProperty("rabbitmq.binding.key") + "\n"
                + "rabbitmq.waitlist.queue.suffix: " + environment.getProperty("rabbitmq.waitlist.queue.suffix") + "\n"
                + "mergeidmarker: " + environment.getProperty("mergeidmarker") + "\n"
                + "spring.data.mongodb.host: " + environment.getProperty("spring.data.mongodb.host") + "\n"
                + "spring.data.mongodb.port: " + environment.getProperty("spring.data.mongodb.port") + "\n"
                + "spring.data.mongodb.username: " + environment.getProperty("spring.data.mongodb.username") + "\n"
                + "spring.data.mongodb.database: " + environment.getProperty("spring.data.mongodb.database") + "\n"
                + "sessions.collection.name: " + environment.getProperty("sessions.collection.name") + "\n"
                + "aggregated.collection.name: " + environment.getProperty("aggregated.collection.name") + "\n"
                + "aggregated.collection.ttlValue: " + environment.getProperty("aggregated.collection.ttlValue") + "\n"
                + "event_object_map.collection.name: " + environment.getProperty("event_object_map.collection.name") + "\n"
                + "waitlist.collection.name: " + environment.getProperty("waitlist.collection.name") + "\n"
                + "waitlist.collection.ttlValue: " + environment.getProperty("waitlist.collection.ttlValue") + "\n"
                + "waitlist.initialDelayResend: " + environment.getProperty("waitlist.initialDelayResend") + "\n"
                + "waitlist.fixedRateResend: " + environment.getProperty("waitlist.fixedRateResend") + "\n"
                + "subscription.collection.name: " + environment.getProperty("subscription.collection.name") + "\n"
                + "subscription.collection.repeatFlagHandlerName: " + environment.getProperty("subscription.collection.repeatFlagHandlerName") + "\n"
                + "testaggregated.enabled: " + environment.getProperty("testaggregated.enabled") + "\n"
                + "threads.corePoolSize: " + environment.getProperty("threads.corePoolSize") + "\n"
                + "threads.queueCapacity: " + environment.getProperty("threads.queueCapacity") + "\n"
                + "threads.maxPoolSize: " + environment.getProperty("threads.maxPoolSize") + "\n"
                + "failedNotificationCollectionName: " + environment.getProperty("failedNotificationCollectionName") + "\n"
                + "failedNotificationDataBaseName: " + environment.getProperty("failedNotificationDataBaseName") + "\n"
                + "email.sender: " + environment.getProperty("email.sender") + "\n"
                + "email.subject: " + environment.getProperty("email.subject") + "\n"
                + "notification.failAttempt: " + environment.getProperty("notification.failAttempt") + "\n"
                + "notification.ttl.value: " + environment.getProperty("notification.ttl.value") + "\n"
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
