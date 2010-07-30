package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public class SecurePropFindAuthoriser implements ClydeAuthoriser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( SecureReadAuthoriser.class );

    @Override
    public String getName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method ) {
        if( method.equals( Request.Method.PROPFIND)) {
            boolean isLoggedIn = request.getAuthorization() != null && request.getAuthorization().getTag() != null;
            if( !isLoggedIn ) {
                log.debug( "not logged in, so disallowing PROPFIND access");
                return false;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


}
