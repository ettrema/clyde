package com.ettrema.web.code.content;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.code.ContentTypeHandler;
import com.ettrema.web.csv.CsvPage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author brad
 */
public class CsvPageContentTypeHandler implements ContentTypeHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CsvPageContentTypeHandler.class );

    @Override
    public boolean supports( Resource r ) {
        return r instanceof CsvPage;
    }

    @Override
    public void generateContent( OutputStream out, GetableResource wrapped ) throws IOException {
        CsvPage page = (CsvPage) wrapped;
        try {
            page.sendContent(out, null, null, "text/csv");
        } catch (NotAuthorizedException | BadRequestException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void replaceContent( InputStream in, Long contentLength, GetableResource wrapped ) {
        log.trace( "replaceContent" );
        CsvPage page = (CsvPage) wrapped;
        try {
            page.replaceContent(in, contentLength);
        } catch (NotAuthorizedException | BadRequestException ex) {
            throw new RuntimeException(ex);
        }


    }
}
