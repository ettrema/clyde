package com.ettrema.web.code.content;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.utils.JDomUtils;
import com.bradmcevoy.utils.XmlUtils2;
import com.ettrema.web.Page;
import com.ettrema.web.code.ContentTypeHandler;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.InitUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.MyXmlOutputter;

/**
 *
 * @author brad
 */
public class PageContentTypeHandler implements ContentTypeHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PageContentTypeHandler.class );

    public boolean supports( Resource r ) {
        return r instanceof Page;
    }

    public void generateContent( OutputStream out, GetableResource wrapped ) throws IOException {
        Page page = (Page) wrapped;
        // <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
        DocType dt = new DocType( "html", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" );
        Element elRoot = new Element( "html" );
        org.jdom.Document doc = new Document( elRoot , dt );
        ComponentValue body = page.getValues().get( "body" );
        ComponentValue title = page.getValues().get( "title" );
        Element elHead = new Element( "head" );
        Element elBody = new Element( "body" );
        elRoot.addContent( elHead );
        elRoot.addContent( elBody );
        Element elTitle = new Element( "title" );
        elHead.addContent( elTitle );
        if( title != null ) {
            CodeUtils.appendValue( page, title, elTitle );
        }
        if( body != null ) {
            CodeUtils.appendValue( page, body, elBody );
        }
        Format format = Format.getPrettyFormat();
        format.setIndent( "\t" );
        MyXmlOutputter op = new MyXmlOutputter( format );
        op.output( doc, out );

    }

    public void replaceContent( InputStream in, Long contentLength, GetableResource wrapped ) {
        log.trace( "replaceContent" );
        Page page = (Page) wrapped;
        try {
            String title;
            XmlUtils2 xmlUtils2 = new XmlUtils2();
            Document doc = xmlUtils2.getJDomDocument( in );
            Element elRoot = doc.getRootElement();
            if( !elRoot.getName().equals( "html" ) ) {
                throw new RuntimeException( "Document is not an html doc" );
            }
            Element elHead = JDomUtils.getChild( elRoot, "head" );
            if( elHead != null ) {
                title = InitUtils.getValueOf( elHead, "title" );
            } else {
                log.trace( "no head element found" );
                title = null;
                if( log.isTraceEnabled() ) {
                    String xml = xmlUtils2.getXml( doc );
                    log.trace( "processing: " + xml );
                }
            }

            String body = InitUtils.getValueOf( elRoot, "body" );
            log.trace( "title: " + title );
            log.trace( "body: " + body );
            CodeUtils.saveValue( page, "body", body );
            CodeUtils.saveValue( page, "title", title );
            page.save();
        } catch( JDOMException ex ) {
            throw new RuntimeException( ex );
        }


    }
}
