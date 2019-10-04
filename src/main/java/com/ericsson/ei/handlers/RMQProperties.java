/*
   Copyright 2019 Ericsson AB.
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
package com.ericsson.ei.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Component
public class RMQProperties {
    @Getter
    @Setter
    @Value("${rabbitmq.queue.durable}")
    private Boolean queueDurable;

    @Getter
    @Setter
    @Value("${rabbitmq.host}")
    private String host;

    @Getter
    @Setter
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Getter
    @Setter
    @Value("${rabbitmq.port}")
    private Integer port;

    @Getter
    @Setter
    @Value("${rabbitmq.tlsVersion}")
    private String tlsVersion;

    @Getter
    @Setter
    @JsonIgnore
    @Value("${rabbitmq.user}")
    private String user;

    @Getter
    @Setter
    @JsonIgnore
    @Value("${rabbitmq.password}")
    private String password;

    @Getter
    @Setter
    @Value("${rabbitmq.domainId}")
    private String domainId;

    @Getter
    @Setter
    @Value("${rabbitmq.componentName}")
    private String componentName;

    @Getter
    @Setter
    @Value("${rabbitmq.waitlist.queue.suffix}")
    private String waitlistSuffix;

    @Getter
    @Setter
    @Value("${rabbitmq.binding.key}")
    private String bindingKey;

    @Getter
    @Setter
    @Value("${rabbitmq.consumerName}")
    private String consumerName;

    public String getQueueName() {
        final String durableName = this.queueDurable ? "durable" : "transient";
        return this.domainId + "." + this.componentName + "." + this.consumerName + "." + durableName;
    }

    public String getWaitlistQueueName() {
        final String durableName = this.queueDurable ? "durable" : "transient";
        return this.domainId + "." + this.componentName + "." + this.consumerName + "." + durableName + "."
                + this.waitlistSuffix;
    }
}
