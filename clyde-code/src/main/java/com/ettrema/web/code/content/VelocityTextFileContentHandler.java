package com.ettrema.web.code.content;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.ettrema.utils.CurrentRequestService;
import com.ettrema.web.VelocityTextFile;
import com.ettrema.web.code.ContentTypeHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.ettrema.context.RequestContext._;
import java.io.*;

/**
 *
 * @author brad
 */
public class VelocityTextFileContentHandler implements ContentTypeHandler{

    private static final String CHARSET = "charset=";
    
    @Override
    public boolean supports( Resource r ) {
        return r instanceof VelocityTextFile;
    }

    @Override
    public void generateContent( OutputStream out, GetableResource wrapped ) throws IOException {
        VelocityTextFile tf = (VelocityTextFile) wrapped;
        out.write(tf.getContent().getBytes());
    }

    @Override
    public void replaceContent( InputStream in, Long contentLength, GetableResource wrapped ) {
        try {
            VelocityTextFile tf = (VelocityTextFile) wrapped;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo(in, bout);
            String charSet = "UTF-8";
            Request req = _(CurrentRequestService.class).request();
            if( req != null ) {
                String s = req.getContentTypeHeader();
                if( s != null && s.contains(CHARSET)) {
                    s = s.substring(s.indexOf(CHARSET) + CHARSET.length());
                }
                charSet = s;
            }
            System.out.println("charSet: " + charSet);
            String content = bout.toString(charSet);
            tf.setContent(content);
            tf.save();
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (ReadingException | WritingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
