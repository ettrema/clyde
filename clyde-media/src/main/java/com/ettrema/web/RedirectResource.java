package com.ettrema.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author brad
 */
public class RedirectResource implements GetableResource{

    private final String href;
    private final String realm;

    public RedirectResource( String href, String realm ) {
        this.href = href;
        this.realm = realm;
    }


    public void sendContent( OutputStream out, Range range, Map<String, String> map, String string ) throws IOException, NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException( "Not supported." );
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    public String getContentType( String string ) {
        return null;
    }

    public Long getContentLength() {
        return null;
    }

    public String getUniqueId() {
        return null;
    }

    public String getName() {
        return "redirector:" + href;
    }

    public Object authenticate( String string, String string1 ) {
        return string;
    }

    public boolean authorise( Request rqst, Method method, Auth auth ) {
        return true;
    }

    public String getRealm() {
        return realm;
    }

    public Date getModifiedDate() {
        return null;
    }

    public String checkRedirect( Request rqst ) {
        return href;
    }

}
