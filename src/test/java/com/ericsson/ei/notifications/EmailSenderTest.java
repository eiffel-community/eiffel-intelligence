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
package com.ericsson.ei.notifications;

import static org.mockito.Mockito.doThrow;

import javax.mail.internet.MimeMessage;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.ericsson.ei.exception.NotificationFailureException;

@RunWith(MockitoJUnitRunner.class)
public class EmailSenderTest {

    @Mock
    JavaMailSender javaMailSender;

    @Mock
    MimeMessage message;

    @Mock
    MimeMessageHelper mimeMessageHelper;

    @InjectMocks
    private EmailSender emailSender;

    @Test(expected = NotificationFailureException.class)
    public void sendEmailThrowsException() throws Exception {
        doThrow(new MailSendException("")).when(javaMailSender).send(message);
        emailSender.sendEmail(message);
    }

    @Test
    public void testDefaultValues() {
        if (emailSender.getSender() != null && emailSender.getSubject() != null) {
            assertEquals("noreply@ericsson.com", emailSender.getSender());
            assertEquals("Email Subscription Notification", emailSender.getSubject());
        } else if (emailSender.getSender() != null && emailSender.getSubject() == null) {
            assertEquals("noreply@ericsson.com", emailSender.getSender());
        } else if (emailSender.getSender() == null && emailSender.getSubject() != null) {
            assertEquals("Email Subscription Notification", emailSender.getSubject());
        }
    }

    /*
     * prepareEmail is not possible to test due too much hidden Spring mumbo jumbo doing things in
     * background.
     */

}
