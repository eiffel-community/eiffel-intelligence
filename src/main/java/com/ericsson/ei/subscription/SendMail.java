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
package com.ericsson.ei.subscription;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ericsson.ei.exception.SubscriptionValidationException;

import lombok.Getter;

/**
 * This class represents the mechanism to send e-mail notification to the recipient of the
 * Subscription Object.
 *
 * @author xjibbal
 */

@Component
public class SendMail {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMail.class);

    @Getter
    @Value("${email.sender}")
    private String sender;

    @Getter
    @Value("${email.subject}")
    private String subject;

    @Autowired
    private JavaMailSender emailSender;

    @PostConstruct
    public void display() {
        LOGGER.debug("Email Sender : " + sender);
        LOGGER.debug("Email Subject : " + subject);
    }

    public void setMailSender(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    /**
     * This method sends mail to the given receiver mail address(es) with the
     * given email subject and body from the mapNotificationMessage.
     *
     * @param receiver
     *     Who to send the mail to
     * @param mapNotificationMessage
     *     A String to be used as the body of the email
     * @param emailSubject
     *     The subject of the email to send
     */
    public void sendMail(String receiver, String mapNotificationMessage, String emailSubject)
            throws MessagingException {
        Set<String> extEmails = new HashSet<>();
        try {
            extEmails = extractAndValidateEmails(receiver);
        } catch (SubscriptionValidationException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        String[] to = extEmails.toArray(new String[0]);
        try {
            helper.setFrom(sender);
            helper.setSubject(getSubject(emailSubject));
            helper.setText(mapNotificationMessage);
            helper.setTo(to);
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        emailSender.send(message);
    }

    /**
     * This method takes a string of comma separated email addresses and
     * validates each of them before putting them in a Set of validated email
     * addresses to return.
     *
     * @param receivers
     *     A string containing one or more comma separated email addresses
     * @return emailAdd
     * @throws SubscriptionValidationException
     */
    public Set<String> extractAndValidateEmails(String receivers) throws SubscriptionValidationException {
        Set<String> emailAdd = new HashSet<>();
        String[] addresses = receivers.split(",");
        for (String add : addresses) {
            SubscriptionValidator.validateEmail(add.trim());
            emailAdd.add(add);
        }
        return emailAdd;
    }

    /**
     * This method takes the user provided email subject and if it is not empty,
     * returns it. Otherwise, it returns the default subject.
     *
     * @param emailSubject
     * @return emailSubject
     */
    private String getSubject(String emailSubject) {
        if (emailSubject.isEmpty()) {
            return subject;
        }
        return emailSubject;
    }
}
