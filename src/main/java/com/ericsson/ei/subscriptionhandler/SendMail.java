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
package com.ericsson.ei.subscriptionhandler;

import javax.annotation.PostConstruct;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

/**
 * This class represents the mechanism to send e-mail notification to the
 * recipient of the Subscription Object.
 * 
 * @author xjibbal
 *
 */

@Component
public class SendMail {

    static Logger log = (Logger) LoggerFactory.getLogger(SendMail.class);

    @Getter
    @Value("${email.sender}")
    private String sender;

    @Getter
    @Value("${email.subject}")
    private String subject;
    
    @Autowired
    private MailSender mailSender;

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * This method takes two arguments i.e receiver mail-id and aggregatedObject
     * and send mail to the receiver with aggregatedObject as the body.
     * 
     * @param receiver
     * @param aggregatedObject
     */
    public void sendMail(String receiver, String aggregatedObject) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(sender);
        message.setTo(receiver);
        message.setSubject(subject);
        message.setText(aggregatedObject);
        mailSender.send(message);
    }

    @PostConstruct
    public void display() {
        log.info("Email Sender : " + sender);
        log.info("Email Subject : " + subject);
    }
}