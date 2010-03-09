
package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.vfs.OutputStreamWriter;
import com.bradmcevoy.web.stats.CountingOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.xhtmlrenderer.util.XRRuntimeException;
import org.xml.sax.SAXException;

public class Pdf extends BinaryFile{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Pdf.class);
    
    private static final long serialVersionUID = 1L;
    

    public Pdf( Folder parentFolder, String newName) {
        super("application/pdf",parentFolder,newName);
    }
    
    public void loadUrl( final String sPath ) {
        Path path = Path.path(sPath);
        loadUrl(path);
    }

    
    public void loadUrl( final Path path ) {
        log.debug("loadUrl: " + path);
        final Templatable ct = this.find(path);
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        Map<String,String> params = new HashMap<String, String>();
        try {
            ct.sendContent(outContent, null, params, null);
        } catch(BadRequestException e) {
            throw new RuntimeException(e);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NotAuthorizedException e) {
            throw new RuntimeException(e);
        }
        final InputStream in = new ByteArrayInputStream(outContent.toByteArray());
        final MyTextRenderer renderer = new MyTextRenderer();        
        this.useOutputStream(new OutputStreamWriter<Long>() {

            public Long writeTo(OutputStream outToPersistence) {
                final CountingOutputStream cout = new CountingOutputStream(outToPersistence);
                try {
                    renderer.setDocument(in, ct.getHref());
                } catch(SAXException e) {
                    log.error("exception processing page: " + e.getClass(), e);
                    throw new RuntimeException(e);
                } catch( XRRuntimeException e) {
                    Throwable e2 = e.getCause();
                    if( e2 instanceof TransformerException ) {
                        TransformerException te = (TransformerException) e2;
                        log.error(te.getMessageAndLocation());
                        SourceLocator loc = te.getLocator();                        
                        if( loc != null ) {
                            log.error("Error at: " + loc.getLineNumber() + " - " + loc.getLineNumber() + " identifier: " + loc.getPublicId() + "/" + loc.getSystemId(), e2);                        
                        } else {
                            log.error("no locator");
                        }
                                
                    }
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                renderer.layout();
                try {
                    renderer.createPDF(cout);
                } catch (Exception ex) {
                    throw new RuntimeException("Exception processing: " + path, ex);
                }
                try {
                    cout.flush();
                    outToPersistence.flush();
                    cout.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                int i = cout.getCount();
                log.debug("generated pdf of size: " + i);
                return (long)i;
            }
        });
        
    }

}
