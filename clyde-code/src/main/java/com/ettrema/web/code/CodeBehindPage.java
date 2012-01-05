package com.ettrema.web.code;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.utils.XmlUtils2;
import com.ettrema.vfs.VfsCommon;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Replaceable;
import com.ettrema.web.security.ClydeAuthoriser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.HtmlXmlOutputter;

/**
 *
 * @author brad
 */
public class CodeBehindPage extends VfsCommon implements GetableResource, Replaceable, DigestResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CodeBehindPage.class );

    public static boolean isCodeBehind( Path path ) {
        if (path == null || path.getName() == null) {
            return false;
        }
        log.debug( "isCodeBehind: " + path.getName());
        return path.getName().endsWith( ".code.xml" );
    }
    public final BaseResource res;

    public static Path getPagePath( Path path ) {
        String nm = path.getName().replace( ".code.xml", "" );
        return path.getParent().child( nm );
    }

    public CodeBehindPage( BaseResource res ) {
        this.res = res;
    }

    @Override
    public void replaceContent( InputStream in, Long contentLength ) {
        log.trace( "replaceContent" );
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamUtils.readTo( in, out );
            XmlUtils2 x = new XmlUtils2();
            String s = out.toString();
            Document doc = x.getJDomDocument( s );
            Element el = doc.getRootElement();
            el = (Element) el.getChildren().get( 0 );
            updateFromXml( el );
            res.save();
            commit();
        } catch( JDOMException ex ) {
            throw new RuntimeException( ex );
        } catch( ReadingException ex ) {
            throw new RuntimeException( ex );
        } catch( WritingException ex ) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public String getUniqueId() {
        return res.getUniqueId() + "_code.xml";
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        Document doc = new Document( new Element( "res" ) );
        toXml( doc.getRootElement() );

        Format format = Format.getPrettyFormat();
        HtmlXmlOutputter outputter = new HtmlXmlOutputter( format );
        try {
            outputter.output( doc, out );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    @Override
    public String getName() {
        return res.getName() + ".source";
    }

    @Override
    public Object authenticate( String user, String password ) {
        return res.authenticate( user, password );
    }

    @Override
    public Object authenticate( DigestResponse digestRequest ) {
        return res.authenticate( digestRequest );
    }

    public boolean isDigestAllowed() {
        return true;
    }

    @Override
    public boolean authorise( Request request, Request.Method method, Auth auth ) {
        ClydeAuthoriser authoriser = requestContext().get( ClydeAuthoriser.class );
        return authoriser.authorise( this, request, method, auth );
    }

    @Override
    public String getRealm() {
        return res.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        Date dt = res.getModifiedDate();
        return dt;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public String getContentType( String accepts ) {
        return "text/xml";
    }

    @Override
    public String checkRedirect( Request request ) {
        return null;
    }

    public Date getCreateDate() {
        return res.getCreateDate();
    }

    private void updateFromXml( Element el ) {
        res.loadFromXml( el );  // values are ignored if not present
    }

    private void toXml( Element rootElement ) {
        Element elRes = res.toXml( rootElement );
        Element child = elRes.getChild( "componentValues" );
        if( child != null ) {
            elRes.removeContent( child );
        }
    }
}
