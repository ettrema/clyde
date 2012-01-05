package com.ettrema.web.security;

import com.bradmcevoy.http.AuthenticationHandler;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;

/**
 * Supports authentication from form parameters.
 *
 * Note that this will not by itself result in a persistent login. It should
 * be used with cookie authentication handler, or some equivalent session
 * based handler
 *
 * @author brad
 */
public class FormAuthenticationHandler implements AuthenticationHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FormAuthenticationHandler.class );
    private String userNameParam = "_loginUserName";
    private String passwordParam = "_loginPassword";

    public boolean supports( Resource r, Request request ) {
        // We will support it if its a form POST and a username param is present
        boolean b = isLogin( request );
        if( log.isTraceEnabled() ) {
            log.trace( "supports: " + b );
        }
        return b;
    }

    /**
     * The authentication result is written to a request attribute called "loginResult".
     *
     * Its value is "true" if login succeeded and "false" if not. Note that a
     * successful login does not ensure that that authorisation will succeed.
     *
     * If rendering a login page based on authentication and authorisation you should also look at the
     * "authReason" attribute set by the LoginResponseHandler which gives the
     * reason for an authorisation failure
     *
     * @param resource
     * @param request
     * @return
     */
    public Object authenticate( Resource resource, Request request ) {
        String userName = request.getParams().get( userNameParam );
        String pwd = request.getParams().get( passwordParam );
        log.trace( "attempt to login with: " + userName );
        Object o = resource.authenticate( userName, pwd );
        // set a request attribute that can be used when rendering
        if( o == null ) {
            request.getAttributes().put( "loginResult", Boolean.FALSE);
        } else {
            request.getAttributes().put( "loginResult", Boolean.TRUE);
        }
        return o;
    }

    public String getChallenge( Resource resource, Request request ) {
        // doesnt do http challenge
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean isCompatible( Resource resource ) {
        // never issue challenge
        return false;
    }

    private boolean isLogin( Request request ) {
        return ( request.getMethod() == Method.POST && request.getParams().get( userNameParam ) != null );
    }

    public String getUserNameParam() {
        return userNameParam;
    }

    public void setUserNameParam( String userNameParam ) {
        this.userNameParam = userNameParam;
    }

    public String getPasswordParam() {
        return passwordParam;
    }

    public void setPasswordParam( String passwordParam ) {
        this.passwordParam = passwordParam;
    }
}
