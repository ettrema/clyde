package com.ettrema.web.code.content;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.TextFile;
import com.ettrema.web.code.ContentTypeHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author brad
 */
public class TextFileContentHandler implements ContentTypeHandler{

    public boolean supports( Resource r ) {
        return r instanceof TextFile;
    }

    public void generateContent( OutputStream out, GetableResource wrapped ) throws IOException {
        TextFile tf = (TextFile) wrapped;
        tf.sendContent( out, null, null, null );
    }

    public void replaceContent( InputStream in, Long contentLength, GetableResource wrapped ) {
        TextFile tf = (TextFile) wrapped;
        tf.replaceContent( in, contentLength );
        tf.save();
    }

}
