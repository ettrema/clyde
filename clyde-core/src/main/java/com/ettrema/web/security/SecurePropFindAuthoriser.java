package com.ettrema.web.security;

import com.bradmcevoy.http.Auth;
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
    public Boolean authorise( Resource resource, Request request, Method method, Auth auth ) {
        // Note that we deliberately look at the method on the request, rather then
        // the given method.
        // This is because the AJAX gateway will simulate a PROPFIND with a GET, but we do not
        // want the normal PROPFIND restriction to apply in this case.
        // eg simulated PROPFIND's should be allowed if GET is allowed
        if( request.getMethod().equals( Request.Method.PROPFIND)) {
            boolean isLoggedIn = auth != null && auth.getTag() != null;
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
