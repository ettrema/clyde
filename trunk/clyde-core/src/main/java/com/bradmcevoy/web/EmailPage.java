package com.bradmcevoy.web;

import java.io.IOException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

public class EmailPage extends Page {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmailPage.class);
    private static final long serialVersionUID = 1L;
    private String body;
    private String subject;

    public EmailPage(Folder parentFolder, String name) {
        super(parentFolder, name);
    }

    public void setMimeMessage(MimeMessage msg) {
        Object oBody;
        try {
            oBody = msg.getContent();
            if (oBody instanceof String) {
                this.body = (String) oBody;
            } else if (oBody instanceof Multipart) {
                Multipart mp = (Multipart) oBody;
                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart bp = mp.getBodyPart(i);
                    if (bp.getContentType().equals("text/plain")) {
                        body += bp.getContent().toString();
                    }
                }
            }
            if (msg.getContentType() != null && msg.getContentType().equals("text/plain")) {
                this.body = (String) msg.getContent();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
