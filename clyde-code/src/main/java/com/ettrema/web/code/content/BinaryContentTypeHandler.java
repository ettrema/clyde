package com.ettrema.web.code.content;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.BinaryFile;
import com.ettrema.web.code.ContentTypeHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author brad
 */
public class BinaryContentTypeHandler implements ContentTypeHandler {
    
    public void generateContent( OutputStream out, GetableResource wrapped ) throws IOException {
        BinaryFile bf = (BinaryFile) wrapped;
        bf.sendContent( out, null, null, null );
    }

    public boolean supports( Resource r ) {
        return r instanceof BinaryFile;
    }

    public void replaceContent( InputStream in, Long contentLength, GetableResource r ) {
        BinaryFile bf = (BinaryFile) r;
        bf.setContent( in );
    }

}
