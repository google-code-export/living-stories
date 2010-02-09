/**
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.livingstories.server.util;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Utility class for sending email alerts
 */
public class AlertSender {
  public static void sendEmail(List<String> recipients, String subject, String msgBody) {
    Session session = Session.getDefaultInstance(new Properties(), null);

    try {
      Message msg = new MimeMessage(session);
      // Note that the 'from' field may only be set to the currently logged in user,
      // or to an administrator email addreess.
      msg.setFrom(new InternetAddress("living-stories-noreply@google.com", "Living Stories"));
      for (String recipient : recipients) {
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
      }
      msg.setSubject(subject);
      
      Multipart mp = new MimeMultipart();
      MimeBodyPart htmlPart = new MimeBodyPart();
      htmlPart.setContent(msgBody, "text/html");
      mp.addBodyPart(htmlPart);
      msg.setContent(mp);
      
      Transport.send(msg);
    } catch (UnsupportedEncodingException ignored) {
    } catch (AddressException ignored) {
    } catch (MessagingException ignored) {
    }
  }
}
