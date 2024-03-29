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

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.handlers.RMQProperties;
import com.ericsson.ei.mongo.MongoUri;
import com.ericsson.ei.notifications.EmailSender;
import com.ericsson.ei.notifications.InformSubscriber;
import com.ericsson.ei.subscription.SubscriptionHandler;
import com.ericsson.ei.utils.SafeLdapServer;
import com.ericsson.ei.waitlist.WaitListStorageHandler;

import lombok.Getter;

/**
 * Parsing all classes which contains value annotation in Eiffel Intelligence. Needed for
 * generating JSON file with information about backend instance.
 */
@Component
public class ParseInstanceInfoEI {
    @Autowired
    private Environment environment;

    @Getter
    private String version;

    @Getter
    private String applicationName;

    @Getter
    @Value("${rules.path}")
    private String rulesPath;

    @Getter
    @Value("${test.aggregation.enabled:false}")
    private String testAggregationEnabled;

    @Getter
    @Autowired
    private List<RMQProperties> rmqProperties;

    @Getter
    @Autowired
    @Lazy
    private List<MongoDbValues> mongodb;

    @Getter
    @Autowired
    @Lazy
    private List<ThreadsValue> threads;

    @Getter
    @Autowired
    private List<EmailSender> email;

    @Getter
    @Autowired
    @Lazy
    private List<MailServerValues> mailServerValues;

    @Getter
    @Autowired
    @Lazy
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
    private ERQueryService eventRepository;

    @PostConstruct
    public void init() throws IOException {
        Properties properties = new Properties();
        properties.load(
                ParseInstanceInfoEI.class.getResourceAsStream("/default-application.properties"));
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
        private String uri;

        @Getter
        @Value("${spring.data.mongodb.database}")
        private String database;

        @PostConstruct
        public void init() throws IOException {
            final String unsafeUri = environment.getProperty("spring.data.mongodb.uri");
            uri = MongoUri.getUriWithHiddenPassword(unsafeUri);
        }

    }

    @Component
    private class LdapValues {
    	public LdapValues() {
			// TODO Auto-generated constructor stub
		}
        @Getter
        @Value("${ldap.enabled}")
        private String ldapEnabled;

        @Getter
        private String ldapServerList;

        /**
         * Extracts ldap.server.list content and creates a new safe to display ldap server list.
         *
         * @throws IOException
         */
        @PostConstruct
        public void init() throws IOException {
            final String ldapServers = environment.getProperty("ldap.server.list");
            final JSONArray serverList = SafeLdapServer.createLdapSettingsArray(ldapServers);
            ldapServerList = serverList.toString(2);
        }

    }

    @Component
    private class ThreadsValue {
        @Getter
        @Value("${threads.core.pool.size}")
        private int corePoolSize;

        @Getter
        @Value("${threads.queue.capacity}")
        private int queueCapacity;

        @Getter
        @Value("${threads.max.pool.size}")
        private int maxPoolSize;
    }
}
