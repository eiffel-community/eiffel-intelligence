package com.ericsson.ei.notifications;

import com.ericsson.ei.App;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class EmailSenderTest {

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    public void testExtractEmails() throws Exception {
        Set<String> extRec = new HashSet<>();
        String receivers = "asdf.hklm@ericsson.se, affda"
            + ".fddfd@ericsson.com, sasasa.dfdfdf@fdad.com, abcd.defg@gmail.com";
        extRec = (emailSender.extractEmails(receivers));
        assertEquals(String.valueOf(extRec.toArray().length), "4");
    }

/*    @Test
    public void testPrepareEmailMessage() throws MessagingException {
        String mapNotificiationMessage = "";
        String recipient = "test@example.com";
        String emailSubject = "Subscription was triggered!";

        MimeMessage message = emailSender.prepareEmailMessage(recipient,
            mapNotificiationMessage, emailSubject);

        System.out.println("new message:: " + message.toString());

        assertTrue(message.getSender().equals("noreply@ericsson.com"));
        assertTrue(message.getSubject().equals(emailSubject));
        assertTrue(message.getAllRecipients().equals(recipient));
    }*/

}
