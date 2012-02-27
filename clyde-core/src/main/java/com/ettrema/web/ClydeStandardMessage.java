package com.ettrema.web;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.property.BeanPropertyAccess;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.web.mail.MessageHelper;
import com.ettrema.web.component.InitUtils;
import com.ettrema.mail.Attachment;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.MessageResource;
import com.ettrema.mail.StandardMessage;
import com.ettrema.mail.StandardMessageFactoryImpl;
import com.ettrema.web.security.BeanProperty;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.jdom.Element;

/**
 *
 */
@BeanPropertyResource(value="clyde", enableByDefault=false)
public class ClydeStandardMessage extends Folder implements StandardMessage, MessageResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeStandardMessage.class);
    private static final long serialVersionUID = 1L;
    private String subject;
    MailboxAddress from;
    MailboxAddress replyTo;
    List<MailboxAddress> to;
    List<MailboxAddress> cc;
    List<MailboxAddress> bcc;
    String text;
    String html;
    String encoding;
    String language;
    int size;
    Map<String, String> headers;
    String disposition;
    private boolean hasBeenSaved;
    private boolean hasBeenRead;

    public ClydeStandardMessage(Folder parentFolder, String newName) {
        super(parentFolder, newName);
        this.setTemplateName("email");
    }

    @Override
    public boolean is(String type) {
        if ("email".equals(type) || "message".equals(type)) {
            return true;
        }
        return super.is(type);
    }

    @Override
    public void save() {
        hasBeenSaved = true;
        super.save();
    }

    @Override
    public void deleteMessage() {
        try {
            this.delete();
        } catch (NotAuthorizedException | ConflictException | BadRequestException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        InitUtils.setString(e2, "subject", subject);
        if (from != null) {
            InitUtils.setString(e2, "from", from.toString());
        }
        if (replyTo != null) {
            InitUtils.setString(e2, "replyTo", replyTo.toString());
        }
        InitUtils.setString(e2, "encoding", encoding);
        InitUtils.setString(e2, "language", language);
        InitUtils.set(e2, "size", size);
        InitUtils.setString(e2, "disposition", disposition);

        Element elText = new Element("text");
        e2.addContent(elText);
        elText.setText(text);

        Element elHtml = new Element("html");
        e2.addContent(elHtml);
        elHtml.setText(html);

        if (headers != null) {
            Element elHeaders = new Element("headers");
            e2.addContent(elHeaders);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                Element elHeader = new Element("header");
                elHeaders.addContent(elHeader);
                elHeader.setAttribute("name", entry.getKey());
                elHeader.setText(entry.getValue());
            }
        }
        populateAddressList(e2, to, "to");
        populateAddressList(e2, cc, "cc");
        populateAddressList(e2, bcc, "bcc");


    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public MailboxAddress getFrom() {
        return from;
    }

    @Override
    public List<Attachment> getAttachments() {
        List<Attachment> list = new ArrayList<>();
        for (Templatable res : this.children("attachment")) {
            if (res instanceof EmailAttachment) {
                EmailAttachment bf = (EmailAttachment) res;
                list.add(bf);
            }
        }

        return list;
    }

    @Override
    public void setFrom(MailboxAddress from) {
        this.from = from;
    }

    @Override
    public MailboxAddress getReplyTo() {
        return replyTo;
    }

    @Override
    public void setReplyTo(MailboxAddress replyTo) {
        this.replyTo = replyTo;
    }

    @BeanProperty()
    @Override    
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @BeanProperty()
    @Override
    public String getHtml() {
        return html;
    }

    @Override
    public void setHtml(String html) {
        this.html = html;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String s) {
        this.text = s;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void setSize(int i) {
        this.size = i;
    }

    @Override
    public void setDisposition(String s) {
        this.disposition = s;
    }

    @Override
    public String getDisposition() {
        return this.disposition;
    }

    @Override
    public void setEncoding(String s) {
        this.encoding = s;
    }

    @BeanProperty()
    @Override
    public String getEncoding() {
        return this.encoding;
    }

    @Override
    public void setContentLanguage(String s) {
        this.language = s;
    }

    @Override
    public String getContentLanguage() {
        return this.language;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public List<MailboxAddress> getTo() {
        return to;
    }

    @Override
    public void setTo(List<MailboxAddress> to) {
        this.to = to;
    }

    @Override
    public List<MailboxAddress> getCc() {
        return this.cc;
    }

    @Override
    public void setCc(List<MailboxAddress> cc) {
        this.cc = cc;
    }

    @Override
    public List<MailboxAddress> getBcc() {
        return bcc;
    }

    @Override
    public void setBcc(List<MailboxAddress> bcc) {
        this.bcc = bcc;
    }

    @Override
    public void setAttachedMessages(List<StandardMessage> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StandardMessage instantiateAttachedMessage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<StandardMessage> getAttachedMessages() {
        return null;
    }

    @Override
    public void addAttachment(String name, String ct, String contentId, InputStream in) {
        log.debug("adding attachment: name:" + name + " ct:" + ct);
        if (isNew()) {
            if (!hasBeenSaved) {  // TODO: this should be moved to baseresource
                save();
            }
        }
        EmailAttachment att = new EmailAttachment(this, ct, name, contentId);
        att.save();
        att.setContent(in);
        att.save();
    }

    @Override
    public void writeTo(OutputStream out) {
        StandardMessageFactoryImpl factory = new StandardMessageFactoryImpl();
        MimeMessage mm = new MimeMessage((Session) null);
        factory.toMimeMessage(this, mm);
        try {
            mm.writeTo(out);
        } catch (IOException ex) {
            log.warn("IOException writing message to client. Client probably closed connection", ex);
        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String formatHtml(String html) {
        return MessageHelper.formatHtml(html, this.getChildren());
    }

    private void populate(Element el, List<MailboxAddress> to) {
        for (MailboxAddress add : to) {
            Element elAdd = new Element("address");
            el.addContent(elAdd);
            elAdd.setAttribute("user", add.user);
            elAdd.setAttribute("domain", add.domain);
        }
    }

    private void populateAddressList(Element e2, List<MailboxAddress> list, String name) {
        if( list == null) {
            return ;
        }        
        Element el = new Element(name);
        e2.addContent(el);
        populate(el, list);
    }

    /**
     * builds html suitable for displaying this message. This will be html, if
     * there is any, otherwise the text
     *
     * @return
     */
    public String getBody() {
        if (html != null && html.length() > 0) {
            String s = formatHtml(html);
            log.debug("html: " + s);
            return s;
        } else {
            return text;
        }
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
    /**
     * Has the message been read
     * 
     * @return 
     */
    @BeanPropertyAccess(true)
    public boolean getRead() {
       return hasBeenRead;
    }
    @BeanPropertyAccess(true)
    public boolean isRead() {
        return getRead();
    }
    
    public void setRead(boolean b) {
        this.hasBeenRead = b;
    }
}
