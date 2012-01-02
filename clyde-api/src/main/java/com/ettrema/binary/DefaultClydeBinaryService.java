package com.ettrema.binary;

import com.ettrema.vfs.OutputStreamWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;


/**
 *
 */
public class DefaultClydeBinaryService implements ClydeBinaryService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( DefaultClydeBinaryService.class );

    @Override
    public int setContent( BinaryContainer file, InputStream in ) {
        log.debug( "setContent: " + file.getName() );
        CheckedInputStream cin = new CheckedInputStream( in, new CRC32() );
        int contentLength = (int) file.getNameNode().setBinaryContent( cin );
        if( contentLength == 0 ) {
            log.warn( "zero size file: " + file.getId() );
        }
        long crc = cin.getChecksum().getValue();
        file.setLocalCrc( crc );
        log.debug( "setContent: new contentLength: " + contentLength + " crc=" + crc );
        return contentLength;
    }

    @Override
    public int writeToOutputStream( BinaryContainer file, OutputStreamWriter<Long> writer ) {
        long l = file.getNameNode().writeToBinaryOutputStream( writer );
        int i = (int) l;
        return i;
    }

    @Override
    public InputStream readInputStream( BinaryContainer file, String versionNum ) {
        if( log.isTraceEnabled() ) {
            log.trace( "readInputStream: " + file.getName() + " ver: " + versionNum );
        }
        return file.getNameNode().getBinaryContent();
    }

    @Override
    public long getContentLength( BinaryContainer file, String versionNum ) {
        Long ll = file.getLocalContentLength();
        log.debug( "getContentLength: " + ll );
        if( ll == null ) return 0;
        return ll.longValue();
    }

    @Override
    public long getCrc( BinaryContainer file, String versionNum ) {
        long crc = file.getLocalCrc();
        log.debug( "crc: " + crc );
        return crc;
    }

    @Override
    public List<VersionDescriptor> getVersions( BinaryContainer file ) {
        DefaultVersionDescriptor d = new DefaultVersionDescriptor();
        d.setContentLength( file.getLocalContentLength() );
        d.setCrc( file.getLocalCrc() );
        d.setUserName( "" );
        d.setVersionNum( "0" );
        List<VersionDescriptor> list = new ArrayList<VersionDescriptor>();
        list.add( d );
        return list;
    }
}

