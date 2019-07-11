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
package com.ericsson.ei.notifications;

import java.util.HashSet;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ericsson.ei.exception.NotificationFailureException;
import com.ericsson.ei.exception.SubscriptionValidationException;

import lombok.Getter;

/**
 * This class represents the mechanism to send e-mail notification to the recipient of the
 * Subscription Object.
 *
 * @author xjibbal
 */

@Component
public class EmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSender.class);

    @Getter
    @Value("${email.sender}")
    private String sender;

    @Getter
    @Value("${email.subject}")
    private String subject;

    @Autowired
    private JavaMailSender emailSender;

    public void setEmailSender(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    /**
     * This method sends mail to the given receivers mail address(es) with the given email subject
     * and body from the mapNotificationMessage.
     *
     * @param receivers              Who to send the mail to
     * @param mapNotificationMessage A String to be used as the body of the email
     * @param emailSubject           The subject of the email to send
     * @throws NotificationFailureException
     */
    public void sendEmail(MimeMessage message) throws NotificationFailureException {
        try {
            emailSender.send(message);
        } catch (MailException e) {
            LOGGER.error("", e);
            throw new NotificationFailureException("Failed to send notification email!");
        }
    }

    /**
     *
     * */
    public MimeMessage prepareEmailMessage(String receivers, String mapNotificationMessage,
            String emailSubject) {
        Set<String> emails = new HashSet<>();
        emails = extractEmails(receivers);
        String[] to = emails.toArray(new String[0]);
        MimeMessage message = prepareEmail(mapNotificationMessage, emailSubject, to);
        return message;
    }

    /**
     * This method creates a MimeMessageHelper and prepares the email to send
     *
     * @param mapNotificationMessage A String to be used by the body of the email
     * @param emailSubject           The subject of the email to send
     * @param receivers              Who to send the email to
     */
    private MimeMessage prepareEmail(String mapNotificationMessage, String emailSubject,
            String[] receivers) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(sender);
            helper.setSubject(getSubject(emailSubject));
            helper.setText(mapNotificationMessage);
            helper.setTo(receivers);
        } catch (MessagingException e) {
            LOGGER.error("Failed to create and send e-mail.", e);
        }
        return message;
    }

    /**
     * This method takes a string of comma separated email addresses and puts them in a Set of email
     * addresses to return.
     *
     * @param receivers A string containing one or more comma separated email addresses
     * @return emailAdd
     */
    public Set<String> extractEmails(String receivers) {
        Set<String> emailAdd = new HashSet<>();
        String[] addresses = receivers.split(",");
        for (String add : addresses) {
            emailAdd.add(add);
        }
        return emailAdd;
    }

    /**
     * This method takes the user provided email subject and if it is not empty, returns it.
     * Otherwise, it returns the default subject.
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
