package com.ettrema.web.code.content;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.utils.JDomUtils;
import com.bradmcevoy.utils.XmlUtils2;
import com.ettrema.web.Template;
import com.ettrema.web.code.ContentTypeHandler;
import com.ettrema.web.component.ComponentValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 *
 * @author brad
 */
public class TemplateContentTypeHandler implements ContentTypeHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TemplateContentTypeHandler.class );
    private final PageContentTypeHandler pageContentTypeHandler;

    public TemplateContentTypeHandler( PageContentTypeHandler pageContentTypeHandler ) {
        this.pageContentTypeHandler = pageContentTypeHandler;
    }

    public boolean supports( Resource r ) {
        return r instanceof Template;
    }

    public void generateContent( OutputStream out, GetableResource wrapped ) throws IOException {
        Template template = (Template) wrapped;
        // <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
        DocType dt = new DocType( "html", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" );
        ComponentValue body = template.getValues().get( "body" );
        org.jdom.Document doc;
        if( "root".equals( template.getTemplateName() ) ) {
            if( body != null ) {
                doc = new Document();
                CodeUtils.appendValue( template, body, doc );
                doc.setDocType( dt );
            } else {
                doc = new Document( new Element( "html" ), dt );
            }
            CodeUtils.formatDoc( doc, out );

        } else {
            pageContentTypeHandler.generateContent( out, wrapped );
        }
    }

    public void replaceContent( InputStream in, Long contentLength, GetableResource wrapped ) {
        Template template = (Template) wrapped;

        if( log.isTraceEnabled() ) {
            log.trace( "replaceContent: parent template: " + template.getTemplateName() );
        }

        if( "root".equals( template.getTemplateName() ) ) {
            log.trace("has a root template, so use entire content as the body parameter");
            // parse the doc, and use the entire root element (inclusive) as content
            // of the body param
            XmlUtils2 xmlUtils2 = new XmlUtils2();
            Document doc;
            try {
                doc = xmlUtils2.getJDomDocument( in );
            } catch( JDOMException ex ) {
                throw new RuntimeException( ex );
            }
            String content = JDomUtils.getXml( doc.getRootElement() );

            CodeUtils.saveValue( template, "body", content );
            template.save();
        } else {
            log.trace("parent is not root, so parse content for body and title");
            pageContentTypeHandler.replaceContent( in, contentLength, wrapped );
        }
    }
}
