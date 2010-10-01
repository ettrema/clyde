package com.ettrema.web.resources;

import com.bradmcevoy.binary.BinaryContainer;
import com.bradmcevoy.binary.ClydeBinaryService;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.utils.CurrentRequestService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class BinaryResource extends AbstractContentResource implements BinaryContainer {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( BinaryResource.class );
    private static final long serialVersionUID = 1L;

    private int contentLength;
    private long crc;

    public BinaryResource( String contentType, FolderResource parentFolder, String newName ) {
        super( contentType, parentFolder, newName );
    }

    @Override
    protected AbstractContentResource copyInstance( FolderResource newParent ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public InputStream getInputStream() throws BadRequestException {
        ClydeBinaryService svc = _( ClydeBinaryService.class );
        if( svc == null ) {
            throw new RuntimeException( "Missing from context: " + ClydeBinaryService.class.getCanonicalName() );
        }

        return svc.readInputStream( this, null );
    }


    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        try {
            InputStream in = getInputStream();

            long bytes = StreamUtils.readTo( in, out, true, false );

            out.flush();
            if( log.isDebugEnabled() ) {
                if( bytes > 0 ) {
                    log.debug( "sent bytes: " + bytes );
                }
            }
        } catch( ReadingException e ) {
            throw new RuntimeException( e );
        } catch( WritingException e ) {
            throw new RuntimeException( e );
        } catch( Throwable e ) {
            throw new RuntimeException( e );
        }
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    public long getLocalCrc() {
        return crc;
    }

    public void setLocalCrc( long value ) {
        this.crc = value;
    }

    public long getCrc() {
        ClydeBinaryService svc = _( ClydeBinaryService.class );
        if( svc == null ) {
            throw new RuntimeException( "Missing from context: " + ClydeBinaryService.class.getCanonicalName() );
        }

        String versionNum = _(CurrentRequestService.class).request().getParams().get( "_version" );
        return svc.getCrc( this, versionNum );
    }

    @Override
    public Long getContentLength() {
        ClydeBinaryService svc = _( ClydeBinaryService.class );
        if( svc == null ) {
            throw new RuntimeException( "Missing from context: " + ClydeBinaryService.class.getCanonicalName() );
        }

        String versionNum = null;
        Map<String, String> ps = _(CurrentRequestService.class).request().getParams();
        if( ps != null ) {
            versionNum = ps.get( "_version" );
        }
        return svc.getContentLength( this, versionNum );
    }

    public Long getLocalContentLength() {
        int i = contentLength;
        return (long) i;
    }

    /**
     * Set the binary content by providing an inputstream to for the underlying
     * persistence mechanism to read.
     *
     *
     * @param in
     */
    public void setContent( InputStream in ) {
        ClydeBinaryService svc = _( ClydeBinaryService.class );
        if( svc == null ) {
            throw new RuntimeException( "Missing from context: " + ClydeBinaryService.class.getCanonicalName() );
        }
        this.contentLength = svc.setContent( this, in );
        if( log.isTraceEnabled() ) {
            log.trace( "setContent: contentLength: " + this.contentLength );
        }
        save(); // This required to save content length. thought this should be happening elsewhere but didnt seem to be
    }
}
