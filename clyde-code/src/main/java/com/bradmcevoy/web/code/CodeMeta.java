package com.bradmcevoy.web.code;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.code.content.CodeUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
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
public class CodeMeta extends AbstractCodeResource<Resource> implements GetableResource, ReplaceableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CodeMeta.class );
    private final MetaHandler metaHandler;
    public static final Namespace NS = Namespace.getNamespace( "c", "http://clyde.ettrema.com/ns/core" );

    public CodeMeta( CodeResourceFactory rf, MetaHandler metaHandler, String name, Resource wrapped ) {
        super( rf, name, wrapped );
        this.metaHandler = metaHandler;
    }

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

    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    public String getContentType( String accepts ) {
        return "text/xml";
    }

    public Long getContentLength() {
        return null;
    }

    public void replaceContent( InputStream in, Long length ) {
        log.trace( "replaceContent: " + wrapped.getClass() );
        try {
            Document doc = rf.getMetaParser().parse( in );
            Element elItem = rf.getMetaParser().getItemElement( doc );
            metaHandler.updateFromXml( wrapped, elItem );
            CodeUtils.commit();
        } catch( JDOMException ex ) {
            throw new RuntimeException( ex );
        }
    }
}
