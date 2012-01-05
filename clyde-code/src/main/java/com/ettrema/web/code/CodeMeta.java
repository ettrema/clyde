package com.ettrema.web.code;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.BufferingOutputStream;
import com.ettrema.web.code.content.CodeUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.MyXmlOutputter;

/**
 *
 * @author brad
 */
public class CodeMeta extends AbstractCodeResource<Resource> implements GetableResource, ReplaceableResource, DeletableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CodeMeta.class );
    public static final Namespace NS = Namespace.getNamespace( "c", "http://clyde.ettrema.com/ns/core" );
    private final MetaHandler metaHandler;
    private final CollectionResource parent;

    public CodeMeta( CodeResourceFactory rf, MetaHandler metaHandler, String name, Resource wrapped, CollectionResource parent ) {
        super( rf, name, wrapped );
        this.metaHandler = metaHandler;
        this.parent = parent;
    }

	@Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        DocType dt = new DocType( "c:meta", "http://clyde.ettrema.com/dtd/core.dtd" );
        Element elRoot = new Element( "meta", NS );
        Document doc = new Document( elRoot, dt );
        Element el = metaHandler.toXml( wrapped );
        elRoot.addContent( el );
        Format format = Format.getPrettyFormat();
        format.setIndent( "\t" );
        MyXmlOutputter op = new MyXmlOutputter( format );
        op.output( doc, out );

    }

	@Override
    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

	@Override
    public String getContentType( String accepts ) {
        return "application/xml";
    }

	@Override
    public Long getContentLength() {
        return null;
    }

	@Override
    public void replaceContent( InputStream in, Long length ) throws NotAuthorizedException, ConflictException, BadRequestException {
        log.trace( "replaceContent: " + wrapped.getClass() );
        try {
            Document doc = rf.getMetaParser().parse( in );
            Element elItem = rf.getMetaParser().getItemElement( doc );
            MetaHandler<? extends Resource> actualMetaHandler = rf.getMetaHandler( elItem );
            if( actualMetaHandler == null ) {
                throw new RuntimeException("Couldnt find a meta handler for: " + elItem.getName());
            }
            if( actualMetaHandler != metaHandler ) {
                if( metaHandler == null ) {
                    log.trace( "type has changed from: (none) -> " + actualMetaHandler.getClass() );
                } else {
                    log.trace( "type has changed from: " + metaHandler.getClass() + " -> " + actualMetaHandler.getClass() );
                }
                String name = wrapped.getName();
                BufferingOutputStream bufferedContent = bufferContent();
                delete();
                wrapped = actualMetaHandler.createFromXml( parent, elItem, name );
                if( bufferedContent.getSize() > 0 ) {
                    log.trace( "restoring buffered content of size: " + bufferedContent.getSize() );
                    restoreBufferedContent( bufferedContent );
                } else {
                    log.trace( "buffered content is empty, will not restore" );
                }
            } else {
                metaHandler.updateFromXml( wrapped, elItem );
            }

            CodeUtils.commit();
        } catch( JDOMException ex ) {
            throw new RuntimeException( ex );
        }
    }

    private BufferingOutputStream bufferContent() {
        if( wrapped instanceof GetableResource ) {
            log.trace( "bufferContent: Resource is getable: " + wrapped );
            BufferingOutputStream out = new BufferingOutputStream( 50000 );
            try {
                CodeContentPage contentPage = new CodeContentPage( rf, wrapped.getName(), (GetableResource) this.wrapped );
                generateContent( contentPage, out );
            } finally {
                IOUtils.closeQuietly( out );
            }
            log.trace( "generated content of size: " + out.getSize() );
            return out;
        } else {
            log.trace( "bufferContent: resource is not getable: " + wrapped );
            return null;
        }
    }

    private void generateContent( CodeContentPage contentPage, final BufferingOutputStream out ) {
        log.trace( "generate content: " + contentPage.getName() );
        try {
            contentPage.sendContent( out, null, null, null );
            log.trace( "content size: " + out.getSize() );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } catch( NotAuthorizedException ex ) {
            throw new RuntimeException( ex );
        } catch( BadRequestException ex ) {
            throw new RuntimeException( ex );
        }
    }

    private void restoreBufferedContent( BufferingOutputStream buffered ) {
        if( buffered == null ) {
            return;
        }
        InputStream in = null;
        try {
            in = buffered.getInputStream();
            CodeContentPage contentPage = new CodeContentPage( rf, wrapped.getName(), (GetableResource) this.wrapped );
            contentPage.replaceContent( in, buffered.getSize() );

        } finally {
            IOUtils.closeQuietly( in );
        }
    }
}
