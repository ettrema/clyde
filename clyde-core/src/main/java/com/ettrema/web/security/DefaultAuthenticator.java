package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.NameAndAuthority;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.User;

/**
 * Authenticator which checks the current host and then parent hosts until
 * a user name is found. If an authority is given then that host is checked
 * directly
 *
 * @author brad
 */
public class DefaultAuthenticator implements ClydeAuthenticator{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( DefaultAuthenticator.class );


    @Override
    public User authenticate( Resource r, String user, String password ) {
        log.debug("authenticate: " + user);
        Templatable resource = (Templatable) r;
        NameAndAuthority na = NameAndAuthority.parse( user );
        Host host = findHost( resource, na.authority );
        if( host == null ) {
            log.debug( "authenticate: host not found: " + na.authority );
            return null;
        } else {
            User u = host.doAuthenticate( na.name, password );
            if( u == null) {
                log.warn("Authentication failed: " + na.name);
            }
            return u;
        }
    }

    @Override
    public User authenticate( Resource r, DigestResponse digestRequest ) {
        log.debug("authenticate(digest): " + digestRequest.getUser());
        Templatable resource = (Templatable) r;
        NameAndAuthority na = NameAndAuthority.parse( digestRequest.getUser() );
        Host host = findHost( resource, na.authority );
        if( host == null ) {
            log.debug( "authenticate: host not found: " + na.authority );
            return null;
        } else {
            User u = host.doAuthenticate( na.name, digestRequest );
            if( u == null) {
                log.warn("Authentication failed: " + na.name);
            }
            return u;
        }

    }


    public Host findHost( Templatable resource,String authority ) {
        Host h = resource.getHost();
        if( authority == null ) {
            return h;
        }
        while( h != null && !h.getName().equals( authority ) ) {
            h = h.getParentHost();
        }
        return h;
    }



}
