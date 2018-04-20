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

import com.ericsson.ei.exception.SubscriptionValidationException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the mechanism to send e-mail notification to the
 * recipient of the Subscription Object.
 *
 * @author xjibbal
 */

@Component
public class SendMail {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SendMail.class);

    @Getter
    @Value("${email.sender}")
    private String sender;

    @Getter
    @Value("${email.subject}")
    private String subject;

    @Autowired
    private JavaMailSender emailSender;

    private SubscriptionValidator subscriptionValidator = new SubscriptionValidator();

    public void setMailSender(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    /**
     * This method takes two arguments i.e receiver mail-id and aggregatedObject and
     * send mail to the receiver with aggregatedObject as the body.
     *
     * @param receiver
     * @param mapNotificationMessage
     */
    public void sendMail(String receiver, String mapNotificationMessage) throws MessagingException {
        Set<String> extEmails = new HashSet<>();
        try {
            extEmails = extractEmails(receiver);
        } catch (SubscriptionValidationException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        String[] to = extEmails.toArray(new String[0]);
        try {
            helper.setFrom(sender);
            helper.setSubject(subject);
            helper.setText(mapNotificationMessage);
            helper.setTo(to);
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        emailSender.send(message);
    }

    /**
     * This method takes string of comma separated email addresses and return the
     * Set of validated email addresses
     *
     * @param contents
     */
    public Set<String> extractEmails(String contents) throws SubscriptionValidationException {
        Set<String> emailAdd = new HashSet<>();
        String[] addresses = contents.split(",");
        for (String add : addresses) {
            subscriptionValidator.validateEmail(add.trim());
            emailAdd.add(add);
        }
        return emailAdd;
    }

    @PostConstruct
    public void display() {
        LOGGER.debug("Email Sender : " + sender);
        LOGGER.debug("Email Subject : " + subject);
    }
}
