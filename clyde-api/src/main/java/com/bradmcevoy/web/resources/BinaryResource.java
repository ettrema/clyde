package com.bradmcevoy.web.resources;

import com.bradmcevoy.binary.BinaryContainer;
import com.bradmcevoy.binary.ClydeBinaryService;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.vfs.DataNode;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static com.ettrema.context.RequestContext._;

/**
 * Represents any binary file in the repository. Typically images and videos
 *
 * @author brad
 */
public class BinaryResource extends AbstractClydeResource implements CopyableResource, DeletableResource, GetableResource, MoveableResource, DataNode, BinaryContainer {

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
        _( ClydeBinaryService.class ).readInputStream( this, null );
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
