package com.bradmcevoy.web.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bradmcevoy.binary.BinaryContainer;
import com.bradmcevoy.binary.ClydeBinaryService;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import org.apache.commons.io.IOUtils;

import static com.ettrema.context.RequestContext._;

/**
 * Represents any binary file in the repository. Typically images and videos
 *
 * @author brad
 */
public class BinaryResource extends AbstractClydeResource implements BinaryContainer, Serializable {

    private static final Logger log = LoggerFactory.getLogger( BinaryResource.class );

    private static final long serialVersionUID = 1L;

    /**
     * Persist a CRC value of the binary data
     */
    private long localCrc;

    /**
     * Persist the content length
     */
    private long localContentLength;

    private String contentType;


    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        InputStream in = _( ClydeBinaryService.class ).readInputStream( this, null );
        if( in != null ) {
            IOUtils.copy( in, out );
        } else {
            log.warn("null inputstream returned");
        }
    }

    public void setContent(InputStream inputStream) {
        localContentLength = _( ClydeBinaryService.class ).setContent( this, inputStream );

    }

    public String getContentType( String accepts ) {
        return contentType;
    }

    public Long getContentLength() {
        return _( ClydeBinaryService.class ).getContentLength( this, null );
    }

    public void setLocalCrc( long crc ) {
        this.localCrc = crc;
    }

    public long getLocalCrc() {
        return localCrc;
    }

    public Long getLocalContentLength() {
        return localContentLength;
    }
}
