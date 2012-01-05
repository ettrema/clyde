package com.ettrema.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.ettrema.mail.MailServer;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.Page;
import com.ettrema.web.RenderContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.jdom.Element;

public class EmailCommand extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmailCommand.class);
    private static final long serialVersionUID = 1L;
    private TemplateInput template;
    private EmailInput from;
    private EmailInput to;
    private Text subject;
    private Text confirmationUrl;
    private TemplateInput replyToTemplate;

    public EmailCommand(Addressable container, String name) {
        super(container, name);
        initNullFields();
    }

    public EmailCommand(Addressable container, Element el) {
        super(container, el);
        InitUtils.initComponentFields(el, this);
        initNullFields();
    }

    private void initNullFields() {
        if (template == null) {
            template = new TemplateInput(this, "template");
        }
        if (from == null) {
            from = new EmailInput(this, "from");
        }
        if (to == null) {
            to = new EmailInput(this, "to");
        }
        if (subject == null) {
            subject = new Text(this, "subject");
        }
        if (confirmationUrl == null) {
            confirmationUrl = new Text(this, "confirmationUrl");
        }
        if (replyToTemplate == null) {
            replyToTemplate = new TemplateInput(this, "replyToTemplate");
        }
    }

    public TemplateInput getTemplate() {
        return template;
    }

    public EmailInput getFrom() {
        return from;
    }

    public EmailInput getTo() {
        return to;
    }

    public Text getSubject() {
        return subject;
    }

    public Text getConfirmationUrl() {
        return confirmationUrl;
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element e2 = super.toXml(container, el);
        template.toXml(container, e2);
        from.toXml(container, e2);
        to.toXml(container, e2);
        subject.toXml(container, e2);
        confirmationUrl.toXml(container, e2);
        if (replyToTemplate != null) {
            replyToTemplate.toXml(container, e2);
        }
        return e2;
    }

    @Override
    public String renderEdit(RenderContext rc) {
        StringBuilder sb = new StringBuilder();
        sb.append(template.renderEdit(rc));
        sb.append("<br/>");
        return sb.toString();
    }

    public String getEmailBody(RenderContext rc) {
        return template.render(rc);
    }

    public String getReplyTo(RenderContext rc) {
        return replyToTemplate.render(rc);
    }

    public TemplateInput getReplyToTemplate() {
        return replyToTemplate;
    }

    
    

    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        String s = parameters.get(this.getName());
        if (s == null) {
            return null; // not this command
        }
        if (!validate(rc)) {
            log.debug("validation failed");
            return null;
        }
        return doProcess(rc, parameters, files);
    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {

        try {
            send(rc, files);
            return confirmationUrl.getValue();
        } catch (MessagingException ex) {
            log.error("exception sending email", ex);
            return null;
        }
    }

    @Override
    public boolean validate(RenderContext rc) {
        if (this.container instanceof CommonTemplated) {
            return ComponentUtils.validatePage((CommonTemplated) this.container, rc);
        } else {
            log.warn("container is not a CommonTemplated. cant validate");
            return false;
        }
    }

    public void send(RenderContext rc, Map<String, FileItem> files) throws MessagingException {
        String emailBody = getEmailBody(rc);

        javax.mail.Session mailSess = null;
        MimeMessage message = new MimeMessage(mailSess);
        message.setSubject(this.subject.getValue());
        javax.mail.Address toAdd = new InternetAddress(this.to.getValue());
        javax.mail.Address fromAdd = new InternetAddress(this.from.getValue());
        message.setFrom(fromAdd);
        log.debug("message from: " + fromAdd.toString());
        message.addRecipient(Message.RecipientType.TO, toAdd);

        String replyTo = getReplyTo(rc);
        if (replyTo != null) {
            replyTo = replyTo.trim();
            if (replyTo.length() > 0) {
                javax.mail.Address rt = new InternetAddress(replyTo);
                javax.mail.Address[] replyToArr = new Address[1];
                replyToArr[0] = rt;
                message.setReplyTo(replyToArr);
            }
        }

        Multipart multipart = new MimeMultipart();
        BodyPart body = new MimeBodyPart();
        body.setText(emailBody);
        body.setDisposition(Part.INLINE);
        multipart.addBodyPart(body);

        if (files != null) {
            for (FileItem f : files.values()) {
                MimeBodyPart mbp2 = new MimeBodyPart();
                mbp2.setDisposition(Part.ATTACHMENT);
                DataSource fds = new FileItemDataSource(f);
                mbp2.setDataHandler(new DataHandler(fds));
                mbp2.setFileName(fds.getName());
                multipart.addBodyPart(mbp2);
            }
        }

        message.setContent(multipart);


        log.debug("Sending message to: " + toAdd.toString());
        MailServer mailServer = requestContext().get(MailServer.class);
        mailServer.getMailSender().sendMail(message);
    }

    @Override
    public Path getPath() {
        return container.getPath().child(name);
    }

    public String getAllFields() {
        StringBuilder sb = new StringBuilder();
        Addressable parent = this.getContainer();
        if (parent instanceof Page) {
            Page page = (Page) parent;
            for (Component c : page.getComponents().values()) {
                sb.append(c.getName()).append(':').append(c.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public static class FileItemDataSource implements DataSource {

        private final FileItem fileItem;

        public FileItemDataSource(FileItem fileItem) {
            this.fileItem = fileItem;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return fileItem.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return fileItem.getOutputStream();
        }

        @Override
        public String getContentType() {
            return fileItem.getContentType();
        }

        @Override
        public String getName() {
            return fileItem.getName();
        }
    }
}
