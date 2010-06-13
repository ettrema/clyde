package com.ettrema.secure;

import com.amazon.thirdparty.Base64.OutputStream;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.vfs.OutputStreamWriter;
import com.bradmcevoy.vfs.RelationalNameNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import org.jasypt.util.binary.BasicBinaryEncryptor;

/**
 *
 */
public class SecureBinaryFile {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SecureBinaryFile.class);
    private static final long serialVersionUID = 1L;

    private static final int BUF_LENGTH = 1024;

    transient RelationalNameNode nameNode;
    BasicBinaryEncryptor binaryEncryptor = new BasicBinaryEncryptor();

    public void store(final InputStream in) throws IOException {
        nameNode.writeToBinaryOutputStream(new OutputStreamWriter<Long>() {

            public Long writeTo(java.io.OutputStream out) {
                byte[] buf = new byte[BUF_LENGTH];
                int len;
                long totalLength = 0;
                try {
                    while ((len = in.read(buf)) != -1) {
                        totalLength += len;
                        byte[] bufToEncrypt;
                        if( len != BUF_LENGTH ) {
                            bufToEncrypt = Arrays.copyOf(buf, len);
                        } else {
                            bufToEncrypt = buf;
                        }
                        byte[] encrypted = binaryEncryptor.encrypt(bufToEncrypt);
                        out.write(encrypted);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return totalLength;
            }
        });
    }

    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
//        try {
//            if (log.isDebugEnabled()) {
//                log.debug("sendContent");
//            }
//            InputStream in = nameNode.getBinaryContent();

//
//        } catch (ReadingException readingException) {
//            log.error("exception reading data", readingException);
//        } catch (WritingException writingException) {
//            log.error("exception writing data", writingException);
//        } catch (Throwable e) {
//            log.error("Exception sending content", e);
//        }
    }
}
