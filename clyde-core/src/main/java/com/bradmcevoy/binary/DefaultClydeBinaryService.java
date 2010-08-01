package com.bradmcevoy.binary;

import com.bradmcevoy.web.BinaryFile;
import com.ettrema.vfs.OutputStreamWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 *
 */
public class DefaultClydeBinaryService implements ClydeBinaryService{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( DefaultClydeBinaryService.class );

    public int setContent(BinaryFile file, InputStream in) {
        log.debug("setContent: " + file.getName());
        CheckedInputStream cin = new CheckedInputStream( in, new CRC32() );
        int contentLength = (int) file.getNameNode().setBinaryContent( cin );
        if( contentLength == 0 ) {
            log.warn("zero size file: " + file.getHref());
        }
        long crc = cin.getChecksum().getValue();
        file.setLocalCrc( crc);
        log.debug( "setContent: new contentLength: " + contentLength + " crc=" + crc );
        return contentLength;
    }

    public int writeToOutputStream(BinaryFile file,OutputStreamWriter<Long> writer) {
        long l = file.getNameNode().writeToBinaryOutputStream( writer );
        int i = (int) l;
        return i;
    }

    public InputStream readInputStream(BinaryFile file, String versionNum) {
        return file.getNameNode().getBinaryContent();
    }

    public long getContentLength(BinaryFile file, String versionNum) {
        Long ll = file.getLocalContentLength();
        log.debug( "getContentLength: " + ll);
        if( ll == null ) return 0;
        return ll.longValue();
    }

    public long getCrc(BinaryFile file, String versionNum) {
        return file.getLocalCrc();
    }

    public List<VersionDescriptor> getVersions(BinaryFile file) {
        DefaultVersionDescriptor d = new DefaultVersionDescriptor();
        d.setContentLength(file.getLocalContentLength());
        d.setCrc(file.getLocalCrc());
        d.setUserName("");
        d.setVersionNum("0");
        List<VersionDescriptor> list = new ArrayList<VersionDescriptor>();
        list.add(d);
        return list;
    }

}
