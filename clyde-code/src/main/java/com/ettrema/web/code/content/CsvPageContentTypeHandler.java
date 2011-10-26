package com.bradmcevoy.web.code.content;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.code.ContentTypeHandler;
import com.bradmcevoy.web.csv.CsvPage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author brad
 */
public class CsvPageContentTypeHandler implements ContentTypeHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CsvPageContentTypeHandler.class );

    public boolean supports( Resource r ) {
        return r instanceof CsvPage;
    }

    public void generateContent( OutputStream out, GetableResource wrapped ) throws IOException {
        CsvPage page = (CsvPage) wrapped;
        page.sendContent(out, null, null, "text/csv");
    }

    public void replaceContent( InputStream in, Long contentLength, GetableResource wrapped ) {
        log.trace( "replaceContent" );
        CsvPage page = (CsvPage) wrapped;
        page.replaceContent(in, contentLength);


    }
}
