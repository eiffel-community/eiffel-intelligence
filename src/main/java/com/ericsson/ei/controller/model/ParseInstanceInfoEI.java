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
import com.ericsson.ei.handlers.RmqHandler;
import com.ericsson.ei.subscription.InformSubscriber;
import com.ericsson.ei.subscription.SendMail;
import com.ericsson.ei.subscription.SubscriptionHandler;
import com.ericsson.ei.waitlist.WaitListStorageHandler;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

/**
 * Parsing all classes which contains value annotation in eiffel-intelligence plugin.
 * Needed for generate Json file with information about backend instance.
 */
@Component
public class ParseInstanceInfoEI {
    @Getter
    @Value("${build.version:#{null}}")
    private String applicationPropertiesVersion;

    @Getter
    private String version;

    @Getter
    private String applicationName;

    @Getter
    @Value("${rules.path}")
    private String rulesPath;

    @Getter
    @Value("${testaggregated.enabled:false}")
    private String testRulesEnabled;

    @Getter
    @Autowired
    private List<RmqHandler> rabbitmq;

    @Getter
    @Autowired
    private List<MongoDbValues> mongodb;

    @Getter
    @Autowired
    private List<ThreadsValue> threads;

    @Getter
    @Autowired
    private List<SendMail> email;

    @Getter
    @Autowired
    private List<MailServerValues> mailServerValues;

    @Getter
    @Autowired
    private LdapValues ldap;

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
    private InformSubscriber informSubscriber;

    @Getter
    @Autowired
    private ERQueryService erUrl;

    @PostConstruct
    public void init() throws IOException {
        Properties properties = new Properties();
        properties.load(ParseInstanceInfoEI.class.getResourceAsStream("/default-application.properties"));
        version = properties.getProperty("version");
        applicationName = properties.getProperty("artifactId");
    }

    @Component
    private class MailServerValues {
        @Getter
        @Value("${spring.mail.host}")
        private String host;

        @Getter
        @Value("${spring.mail.port}")
        private String port;

        @Getter
        @Value("${spring.mail.username}")
        private String username;

        @Getter
        @Value("${spring.mail.properties.mail.smtp.auth}")
        private String smtpAuth;

        @Getter
        @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
        private String startTls;
    }

    @Component
    private class MongoDbValues {
        @Getter
        @Value("${spring.data.mongodb.host}")
        private String host;

        @Getter
        @Value("${spring.data.mongodb.port}")
        private String port;

        @Getter
        @Value("${spring.data.mongodb.database}")
        private String database;
    }

    @Component
    private class LdapValues {
        @Getter
        @Value("${ldap.enabled}")
        private String enabled;

        @Getter
        @Value("${ldap.url}")
        private String url;

        @Getter
        @Value("${ldap.base.dn}")
        private String baseDn;

        @Getter
        @Value("${ldap.user.filter}")
        private String filter;

        @Getter
        @Value("${ldap.username}")
        private String username;
    }

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
