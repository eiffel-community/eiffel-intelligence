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
    Environment env;

    @PostConstruct
    public void print() {
        logConfiguration();
    }

    private void logConfiguration() {
        LOGGER.debug("EI Backend started with following configurations:\n"
                + "server.port: " + env.getProperty("server.port") + "\n"
                + "server.session-timeout: " + env.getProperty("server.session-timeout") + "\n"
                + "rules.path: " + env.getProperty("rules.path") + "\n"
                + "rabbitmq.host: " + env.getProperty("rabbitmq.host") + "\n"
                + "rabbitmq.port: " + env.getProperty("rabbitmq.port") + "\n"
                + "rabbitmq.user: " + env.getProperty("rabbitmq.user") + "\n"
                + "rabbitmq.tlsVersion: " + env.getProperty("rabbitmq.tlsVersion") + "\n"
                + "rabbitmq.exchange.name: " + env.getProperty("rabbitmq.exchange.name") + "\n"
                + "rabbitmq.domainId: " + env.getProperty("rabbitmq.domainId") + "\n"
                + "rabbitmq.componentName: " + env.getProperty("rabbitmq.componentName") + "\n"
                + "rabbitmq.consumerName: " + env.getProperty("rabbitmq.consumerName") + "\n"
                + "rabbitmq.queue.durable: " + env.getProperty("rabbitmq.queue.durable") + "\n"
                + "rabbitmq.binding.key: " + env.getProperty("rabbitmq.binding.key") + "\n"
                + "rabbitmq.waitlist.queue.suffix: " + env.getProperty("rabbitmq.waitlist.queue.suffix") + "\n"
                + "mergeidmarker: " + env.getProperty("mergeidmarker") + "\n"
                + "spring.data.mongodb.host: " + env.getProperty("spring.data.mongodb.host") + "\n"
                + "spring.data.mongodb.port: " + env.getProperty("spring.data.mongodb.port") + "\n"
                + "spring.data.mongodb.username: " + env.getProperty("spring.data.mongodb.username") + "\n"
                + "spring.data.mongodb.database: " + env.getProperty("spring.data.mongodb.database") + "\n"
                + "sessions.collection.name: " + env.getProperty("sessions.collection.name") + "\n"
                + "search.query.prefix: " + env.getProperty("search.query.prefix") + "\n"
                + "aggregated.object.name: " + env.getProperty("aggregated.object.name") + "\n"
                + "aggregated.collection.name: " + env.getProperty("aggregated.collection.name") + "\n"
                + "aggregated.collection.ttlValue: " + env.getProperty("aggregated.collection.ttlValue") + "\n"
                + "event_object_map.collection.name: " + env.getProperty("event_object_map.collection.name") + "\n"
                + "waitlist.collection.name: " + env.getProperty("waitlist.collection.name") + "\n"
                + "waitlist.collection.ttlValue: " + env.getProperty("waitlist.collection.ttlValue") + "\n"
                + "waitlist.initialDelayResend: " + env.getProperty("waitlist.initialDelayResend") + "\n"
                + "waitlist.fixedRateResend: " + env.getProperty("waitlist.fixedRateResend") + "\n"
                + "subscription.collection.name: " + env.getProperty("subscription.collection.name") + "\n"
                + "subscription.collection.repeatFlagHandlerName: " + env.getProperty("subscription.collection.repeatFlagHandlerName") + "\n"
                + "testaggregated.enabled: " + env.getProperty("testaggregated.enabled") + "\n"
                + "threads.corePoolSize: " + env.getProperty("threads.corePoolSize") + "\n"
                + "threads.queueCapacity: " + env.getProperty("threads.queueCapacity") + "\n"
                + "threads.maxPoolSize: " + env.getProperty("threads.maxPoolSize") + "\n"
                + "missedNotificationCollectionName: " + env.getProperty("missedNotificationCollectionName") + "\n"
                + "missedNotificationDataBaseName: " + env.getProperty("missedNotificationDataBaseName") + "\n"
                + "email.sender: " + env.getProperty("email.sender") + "\n"
                + "email.subject: " + env.getProperty("email.subject") + "\n"
                + "notification.failAttempt: " + env.getProperty("notification.failAttempt") + "\n"
                + "notification.ttl.value: " + env.getProperty("notification.ttl.value") + "\n"
                + "spring.mail.host: " + env.getProperty("spring.mail.host") + "\n"
                + "spring.mail.port: " + env.getProperty("spring.mail.port") + "\n"
                + "spring.mail.username: " + env.getProperty("spring.mail.username") + "\n"
                + "spring.mail.properties.mail.smtp.auth: " + env.getProperty("spring.mail.properties.mail.smtp.auth") + "\n"
                + "spring.mail.properties.mail.smtp.starttls.enable: " + env.getProperty("spring.mail.properties.mail.smtp.starttls.enable") + "\n"
                + "er.url: " + env.getProperty("er.url") + "\n"
                + "ldap.enabled: " + env.getProperty("ldap.enabled") + "\n"
                + "ldap.url: " + env.getProperty("ldap.url") + "\n"
                + "ldap.base.dn: " + env.getProperty("ldap.base.dn") + "\n"
                + "ldap.username: " + env.getProperty("ldap.username") + "\n"
                + "ldap.user.filter: " + env.getProperty("ldap.user.filter") + "\n"
                + "logging.level.root: " + env.getProperty("logging.level.root") + "\n"
                + "logging.level.org.springframework.web: " + env.getProperty("logging.level.org.springframework.web") + "\n"
                + "logging.level.com.ericsson.ei: " + env.getProperty("logging.level.com.ericsson.ei") + "\n"
                + "\nThese properties are only some of the configurations, more configurations could have been provided.\n");
    }
}
