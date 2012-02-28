package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DateUtils.DateParseException;
import com.bradmcevoy.http.Resource;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.utils.JDomUtils;
import com.ettrema.web.ClydeStandardMessage;
import com.ettrema.web.Folder;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import com.ettrema.web.component.InitUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author brad
 */
public class MessageMetaHandler implements MetaHandler<ClydeStandardMessage> {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MessageMetaHandler.class);
    
    public static final String ALIAS = "message";
    private final FolderMetaHandler folderMetaHandler;

    public MessageMetaHandler(FolderMetaHandler baseResourceMetaHandler) {
        this.folderMetaHandler = baseResourceMetaHandler;
    }

    @Override
    public Class getInstanceType() {
        return ClydeStandardMessage.class;
    }

    @Override
    public boolean supports(Resource r) {
        return r instanceof ClydeStandardMessage;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public Element toXml(ClydeStandardMessage r) {
        Element el = new Element(ALIAS, CodeMeta.NS);
        populateXml(el, r);
        return el;
    }

    @Override
    public ClydeStandardMessage createFromXml(CollectionResource parent, Element d, String name) {
        ClydeStandardMessage f = new ClydeStandardMessage((Folder) parent, name);
        updateFromXml(f, d);
        return f;
    }

    public void populateXml(Element el, ClydeStandardMessage page) {
        JDomUtils.setChildText(el, "subject", page.getSubject(), CodeMeta.NS);
        setXml(el, "from", page.getFrom());
        setXml(el, "replyTo", page.getReplyTo());
        setXml(el, "to", page.getTo());
        setXml(el, "cc", page.getCc());
        setXml(el, "bcc", page.getBcc());
        JDomUtils.setChildText(el, "text", page.getText(), CodeMeta.NS);
        JDomUtils.setChildText(el, "html", page.getHtml(), CodeMeta.NS); // can't be sure it will be valid XML
        InitUtils.set(el, "encoding", page.getEncoding());
        InitUtils.set(el, "language", page.getLanguage());
        InitUtils.set(el, "size", page.getSize());
        InitUtils.set(el, "disposition", page.getDisposition());
        InitUtils.set(el, "received", page.getMessageDate());
        if (page.getHeaders() != null && !page.getHeaders().isEmpty()) {
            Element elHeaders = new Element("headers", CodeMeta.NS);
            el.addContent(elHeaders);
            for (Entry<String, String> entry : page.getHeaders().entrySet()) {
                Element elH = new Element("header", CodeMeta.NS);
                elHeaders.addContent(elH);
                elH.setAttribute("name", entry.getKey());
                elH.setText(entry.getValue());
            }
        }
        folderMetaHandler.populateXml(el, page);
    }

    private void _updateFromXml(ClydeStandardMessage page, Element el) {        
        page.setSubject(JDomUtils.valueOf(el, "subject", CodeMeta.NS));
        page.setFrom(getAddress(el, "from"));
        page.setReplyTo(getAddress(el, "replyTo"));
        page.setTo(getAddresses(el, "to"));
        page.setCc(getAddresses(el, "cc"));
        page.setBcc(getAddresses(el, "bcc"));
        page.setText( JDomUtils.valueOf(el, "text", CodeMeta.NS) );
        page.setHtml( JDomUtils.valueOf(el, "html", CodeMeta.NS) );
        page.setEncoding(InitUtils.getValue(el, "encoding"));
        page.setLanguage(InitUtils.getValue(el, "language"));
        page.setSize(InitUtils.getInt(el, "size"));
        page.setDisposition(InitUtils.getValue(el, "disposition"));
        try {
            page.setMessageDate(InitUtils.getDate(el, "received"));
        } catch (DateParseException ex) {
            log.warn("Invalid date", ex);
        }
        Element elHeaders = el.getChild("headers", CodeMeta.NS);
        if( elHeaders != null ) {
            Map<String,String> headers = new HashMap<>();
            for(Element elH : JDomUtils.children(elHeaders)) {
                String name = elH.getAttributeValue("name");
                String value = elH.getText();
                headers.put(name, value);
            }
            page.setHeaders(headers);
        }
    }

    @Override
    public void updateFromXml(ClydeStandardMessage r, Element d) {
        folderMetaHandler.updateFromXml(r, d);
        _updateFromXml(r, d);        
        r.save();
    }

    @Override
    public void applyOverrideFromXml(ClydeStandardMessage r, Element d) {
        folderMetaHandler.applyOverrideFromXml(r, d);
        r.save();
    }

    private void setXml(Element el, String name, MailboxAddress m) {
        if( m == null ) {
            return ;
        }
        Element elAddress = new Element(name, CodeMeta.NS);
        el.addContent(elAddress);
        elAddress.setText(m.toString());
    }

    private MailboxAddress getAddress(Element el, String from) {
        Element elAddress = el.getChild(from, CodeMeta.NS);
        if (elAddress == null) {
            return null;
        }
        String s = elAddress.getText();
        if (s == null || s.length() == 0) {
            return null;
        }
        return MailboxAddress.parse(s);
    }

    private void setXml(Element el, String name, List<MailboxAddress> addresses) {
        if( addresses == null ) {
            return ;
        }
        Element elAddresses = new Element(name, CodeMeta.NS);
        el.addContent(elAddresses);
        for (MailboxAddress a : addresses) {
            setXml(el, "address", a);
        }
    }

    private List<MailboxAddress> getAddresses(Element el, String from) {
        Element elAddresses = el.getChild(from, CodeMeta.NS);
        if (elAddresses == null) {
            return null;
        }
        List<Element> addressElements = JDomUtils.children(elAddresses);
        if (addressElements == null || addressElements.isEmpty()) {
            return null;
        }
        List<MailboxAddress> list = new ArrayList<>();
        for (Element elAddress : addressElements) {
            String s = elAddress.getText();
            MailboxAddress a = MailboxAddress.parse(s);
            list.add(a);
        }
        return list;
    }
}
