package com.ettrema.web.code.content;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.utils.XmlUtils2;
import com.ettrema.utils.JDomUtils;
import com.ettrema.web.Page;
import com.ettrema.web.code.ContentTypeHandler;
import com.ettrema.web.code.content.xml.StaxBuilder;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.InitUtils;
import java.io.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.io.IOUtils;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.MyXmlOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author brad
 */
public class PageContentTypeHandler implements ContentTypeHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PageContentTypeHandler.class);

    @Override
    public boolean supports(Resource r) {
        return r instanceof Page;
    }

    @Override
    public void generateContent(OutputStream out, GetableResource wrapped) throws IOException {
        Page page = (Page) wrapped;
        // <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
        DocType dt = new DocType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
        Element elRoot = new Element("html");
        org.jdom.Document doc = new Document(elRoot, dt);
        ComponentValue body = page.getValues().get("body");
        ComponentValue title = page.getValues().get("title");
        Element elHead = new Element("head");
        Element elBody = new Element("body");
        elRoot.addContent(elHead);
        elRoot.addContent(elBody);
        Element elTitle = new Element("title");
        elHead.addContent(elTitle);
        if (title != null) {
            CodeUtils.appendValue(page, title, elTitle);
        }
        if (body != null) {
            CodeUtils.appendValue(page, body, elBody);
        }
        Format format = Format.getPrettyFormat();
        format.setIndent("\t");
        MyXmlOutputter op = new MyXmlOutputter(format);
        op.output(doc, out);

    }

    @Override
    public void replaceContent(InputStream in, Long contentLength, GetableResource wrapped) {
        log.trace("replaceContent");
        Page page = (Page) wrapped;
        try {
            String title;
            XmlUtils2 xmlUtils2 = new XmlUtils2();

            // hack
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try {
                IOUtils.copy(in, bout);
            } catch (IOException ex) {
                throw new JDOMException("IOException reading data", ex);
            } finally {
                IOUtils.closeQuietly(bout);
            }

            Document doc = getJDomDocument(new ByteArrayInputStream(bout.toByteArray()));
            //Document doc = xmlUtils2.getJDomDocument( in );
            Element elRoot = doc.getRootElement();
            if (!elRoot.getName().equals("html")) {
                throw new RuntimeException("Document is not an html doc");
            }
            Element elHead = JDomUtils.getChild(elRoot, "head");
            if (elHead != null) {
                title = InitUtils.getValueOf(elHead, "title");
            } else {
                log.trace("no head element found");
                title = null;
                if (log.isTraceEnabled()) {
                    String xml = xmlUtils2.getXml(doc);
                    log.trace("processing: " + xml);
                }
            }

            String body = InitUtils.getValueOf(elRoot, "body");
            log.trace("title: " + title);
            log.trace("body: " + body);
            CodeUtils.saveValue(page, "body", body);
            CodeUtils.saveValue(page, "title", title);
            page.save();
        } catch (JDOMException ex) {
            throw new RuntimeException(ex);
        }
    }

    public org.jdom.Document getJDomDocument(InputStream fin) throws JDOMException {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            if (!inputFactory.isPropertySupported(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES)) {
                throw new RuntimeException(":EEEk");
            }
            inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
            inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
            inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
            inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            XMLResolver xMLResolver = new XMLResolver() {

                @Override
                public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException {
                    System.out.println("resolveEntoty: " + systemID);
                    return new ByteArrayInputStream(new byte[0]);
                }
            };
            inputFactory.setProperty(XMLInputFactory.RESOLVER, xMLResolver);
            StaxBuilder staxBuilder = new StaxBuilder();
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(fin);
            return staxBuilder.build(streamReader);

//            SAXBuilder builder = new SAXBuilder();
//            builder.setExpandEntities(false);
//            builder.setEntityResolver(new MyEntityResolver());
//            builder.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
//            //builder.setFeature(  "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
//
//            return builder.build(fin);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
        }
    }

    public class MyEntityResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Path p = Path.path(systemId);
            System.out.println("resolveEntoty: " + p);
            return null;
        }
    }
}
