/*
   Copyright 2017 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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

/**
 * Parsing all classes which contains value annotation in eiffel-intelligence plugin.
 * Needed for generate Json file with information about backend instance.
 */
@Component
public class ParseInstanceInfoEI {
    @Getter
    @Value("${spring.application.name}")
    private String applicationName;

    @Getter
    @Value("${build.version}")
    private String version;

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
