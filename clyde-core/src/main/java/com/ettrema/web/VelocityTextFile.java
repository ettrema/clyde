package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.ettrema.web.component.CommonComponent;
import com.ettrema.web.security.CurrentUserService;
import com.ettrema.web.velocity.VelocityInterpreter;
import java.io.*;
import java.util.Map;
import org.apache.velocity.VelocityContext;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class VelocityTextFile extends File implements SimpleEditPage.SimpleEditable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TextFile.class );
    private static final long serialVersionUID = 1L;
    private String template;

    public VelocityTextFile( String contentType, Folder parent, String name ) {
        super( contentType, parent, name );
    }

    public VelocityTextFile( Folder parent, String name ) {
        super( "text", parent, name );
    }

    @Override
    public String getDefaultContentType() {
        // since binary files can represent many different content types
        // we try to infer from the file name
        return ContentTypeUtil.getContentTypeString( getName() );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new TextFile( parent, newName );
    }

    @Override
    public boolean is( String type ) {
        if( type == null ) return false;
        if( super.is( type ) ) return true;
        if( type.equals( "text" ) ) return true;
        String ct = getContentType( null );
        if( ct == null ) return false;
        return ct.contains( type );
    }

    @Override
    public String getContent() {
        return template;
    }

    @Override
    public void setContent( String content ) {
        this.template = content;
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        if( template == null ) {
            log.debug( "no content for: " + this.getPath() );
        } else {
            log.trace( "send content size: " + template.length() );
            IUser user = _(CurrentUserService.class).getOnBehalfOf();
            VelocityContext vc = CommonComponent.velocityContext(this, this, null, getPath(), user);
            VelocityInterpreter.evalToStream(template, vc, out);
        }
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public PostableResource getEditPage() {
        return new SimpleEditPage( this );
    }

    @Override
    protected BaseResource copyInstance( Folder parent, String newName ) {
        BaseResource newRes = super.copyInstance( parent, newName );
        ( (VelocityTextFile) newRes ).setContent( template );
        return newRes;
    }

    @Override
    void setContent( InputStream in ) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamUtils.readTo( in, out );
            setContent( out.toString() );
        } catch( ReadingException | WritingException ex ) {
            throw new RuntimeException( ex );
        }

    }


    @Override
    public boolean isIndexable() {
        return false;
    }
}
