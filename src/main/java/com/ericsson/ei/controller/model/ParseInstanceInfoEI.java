package com.ericsson.ei.controller.model;

import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.subscriptionhandler.InformSubscription;
import com.ericsson.ei.subscriptionhandler.SendMail;
import com.ericsson.ei.subscriptionhandler.SubscriptionHandler;
import com.ericsson.ei.waitlist.WaitListStorageHandler;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParseInstanceInfoEI {
    @Getter
    @Value("${application.name}") private String applicationName;

    @Getter
    @Value("${build.version}") private String version;

    @Getter
    @Autowired
    private List<RmqHandler> rabbitmq;

    @Getter
    @Autowired
    private List<MongoDBHandler> mongodb;

    @Getter
    @Autowired
    private List<ThreadsValue> threads;

    @Getter
    @Autowired
    private List<SendMail> email;

    @Getter
    @Autowired
    private List<WaitListStorageHandler> waitList;

    @Getter
    @Autowired
    private ObjectHandler objectHandler;

    @Getter
    @Autowired
    private SubscriptionHandler subscriptionHandler;

    @Getter
    @Autowired
    private InformSubscription informSubscription;

    @Getter
    @Autowired
    private ERQueryService erUrl;

    @Component
    private class ThreadsValue {
        @Getter
        @Value("${threads.corePoolSize}")
        private int corePoolSize;

        @Getter
        @Value("${threads.queueCapacity}")
        private int queueCapacity;

        @Getter
        @Value("${threads.maxPoolSize}")
        private int maxPoolSize;
    }
}
