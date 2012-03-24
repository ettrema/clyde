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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author brad
 */
public class VelocityTextFileContentHandler implements ContentTypeHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( VelocityTextFileContentHandler.class );
    
    private static final String CHARSET = "charset=";

    @Override
    public boolean supports(Resource r) {
        return r instanceof VelocityTextFile;
    }

    @Override
    public void generateContent(OutputStream out, GetableResource wrapped) throws IOException {
        VelocityTextFile tf = (VelocityTextFile) wrapped;
        out.write(tf.getContent().getBytes());
    }

    @Override
    public void replaceContent(InputStream in, Long contentLength, GetableResource wrapped) {
        try {
            VelocityTextFile tf = (VelocityTextFile) wrapped;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo(in, bout);
            String charSet = "UTF-8";
            Request req = _(CurrentRequestService.class).request();
            if (req != null) {
                String s = req.getContentTypeHeader();
                charSet = getCharset(s, charSet);
            }
            String content;
            try {
                content = bout.toString(charSet);
            } catch (UnsupportedEncodingException ex) {
                log.warn("Unsupported charset:" + charSet, ex);
                content = bout.toString();
            }
            tf.setContent(content);
            tf.save();
        } catch (ReadingException | WritingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getCharset(String contentType, String defaultCharset) {
        String charset = null;
        if (contentType != null && contentType.contains(CHARSET)) {
            charset = contentType.substring(contentType.indexOf(CHARSET) + CHARSET.length());
            charset = charset.trim();
        }
        if (charset == null || charset.length() == 0) {
            charset = defaultCharset;
        }
        return charset;
    }
}
