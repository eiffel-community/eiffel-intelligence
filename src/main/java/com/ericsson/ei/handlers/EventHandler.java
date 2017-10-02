package com.ericsson.ei.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Component
public class EventHandler {
    @Value("${threads.corePoolSize}") private int corePoolSize;
    @Value("${threads.queueCapacity}") private int queueCapacity;
    @Value("${threads.maxPoolSize}") private int maxPoolSize;

    private static Logger log = LoggerFactory.getLogger(EventHandler.class);

    @Autowired
    RulesHandler rulesHandler;

    @Autowired
    IdRulesHandler idRulesHandler;

    public void eventReceived(String event) {
        RulesObject eventRules = rulesHandler.getRulesForEvent(event);
        idRulesHandler.runIdRules(eventRules, event);
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("EventHandler-");
        executor.initialize();
        return executor;
    }

    @Async
    public void eventReceived(byte[] message) {
        log.info("Thread id " + Thread.currentThread().getId() + " spawned");
        String actualMessage = new String(message);
        log.info("Event received <" + actualMessage + ">");
        eventReceived(actualMessage);
        if (System.getProperty("flow.test") == "true" || System.getProperty("trafficGenerated.test") == "true") {
            String countStr = System.getProperty("eiffel.intelligence.processedEventsCount");
            int count = Integer.parseInt(countStr);
            count++;
            System.setProperty("eiffel.intelligence.processedEventsCount", "" + count);
        }
    }
}
