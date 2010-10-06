package com.bradmcevoy.binary;

import com.bradmcevoy.http.Resource;
import com.ettrema.vfs.NameNode;
import java.util.UUID;

/**
 *
 * @author brad
 */
public interface BinaryContainer extends Resource {


    NameNode getNameNode();

    /**
     * Set the CRC directly on the resource
     *
     * @param crc
     */
    void setLocalCrc( long crc );

    long getLocalCrc();

    UUID getId();

    Long getLocalContentLength();



}
