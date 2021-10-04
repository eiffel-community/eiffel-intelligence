package com.ericsson.ei.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.data.mongo.MongoIndexedSessionRepository;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.session.web.http.HttpSessionIdResolver;

@EnableMongoHttpSession()
public class HttpSessionConfig {

    @Value("${server.session.timeout}")
    private int maxInactiveIntervalInSeconds;

    @Value("${sessions.collection.name}")
    private String collectionName;

    @Primary
    @Bean
    public MongoIndexedSessionRepository mongoIndexedSessionRepository(MongoOperations mongoOperations) {
        MongoIndexedSessionRepository repository = new MongoIndexedSessionRepository(mongoOperations);
        repository.setMaxInactiveIntervalInSeconds(maxInactiveIntervalInSeconds);
        return repository;
    }

    public static String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new HeaderAndCookieHttpSessionIdResolver();
    }
}