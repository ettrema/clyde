package com.bradmcevoy.web;

import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 * @author brad
 */
public interface ISubPage extends Templatable{
    /**
     * subpages may require a logged in user, despite the folder's permissions
     *
     * @return
     */
    boolean isSecure();

    Resource getChildResource( String childName );

    String getContentType( String accepts );
    
    String getDefaultContentType();

    /**
     * This is called by the wrapped sub page.
     * 
     * @param requestedPage
     * @param out
     * @param range
     * @param params
     * @param contentType
     * @throws IOException
     * @throws NotAuthorizedException
     * @throws BadRequestException
     */
    void sendContent( WrappedSubPage requestedPage, OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException;
}
