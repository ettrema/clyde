package com.ettrema.web.security;

import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.Host;
import com.ettrema.web.NameAndAuthority;
import com.ettrema.web.Templatable;
import com.ettrema.web.User;

/**
 * Authenticator which checks the current host if no authority is given.
 * 
 * If an authority is given then we look for that host from this host upwards,
 * until it is found. If it is not found then authentication is unsuccessful
 *
 * @author brad
 */
public class RecursiveAuthenticator implements ClydeAuthenticator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RecursiveAuthenticator.class );

    @Override
    public User authenticate( Resource r, String user, String password ) {
        //log.debug("authenticate: " + user);
        if( r instanceof Templatable ) {
            Templatable resource = (Templatable) r;
            NameAndAuthority na = NameAndAuthority.parse( user );
            User u = null;
            if( na.authority == null ) {
                Host h = resource.getHost();
                LogUtils.trace(log, "authenticate: no authority given, so use current host", h.getName());                
                u = authenticateRecursive( h, na.name, password );
            } else {
                Host host = findHost( resource, na.authority );
                if( host == null ) {
                    log.warn( "authenticate: host not found: " + na.authority );
                    return null;
                } else {
                    u = host.doAuthenticate( na.name, password );
                    LogUtils.trace(log, "authenticate: authenticating on specified host", host.getName(), "result:", u);                
                }
            }
            if( u == null ) {
                log.warn( "Authentication failed: " + na.name );
            }
            return u;
        } else {
            return (User) r.authenticate(user, password );
        }

    }

    @Override
    public User authenticate( Resource r, DigestResponse digestRequest ) {
        log.debug( "authenticate(digest): " + digestRequest.getUser() );
        if( r instanceof Templatable ) {
            Templatable resource = (Templatable) r;
            NameAndAuthority na = NameAndAuthority.parse( digestRequest.getUser() );
            User u = null;
            if( na.authority == null ) {
                Host h = resource.getHost();
                u = authenticateRecursive( h, na.name, digestRequest );
            } else {
                Host host = findHost( resource, na.authority );
                if( host == null ) {
                    log.debug( "authenticate: host not found: " + na.authority );
                    return null;
                } else {
                    u = host.doAuthenticate( na.name, digestRequest );
                }
            }
            if( u == null ) {
                log.warn( "Authentication failed: " + na.name );
            }
            return u;
        } else if( r instanceof DigestResource ) {
            DigestResource dr = (DigestResource) r;
            return (User) dr.authenticate( digestRequest );
        } else {
            throw new RuntimeException( "Cant authenticate against resource of type: " + r.getClass().getCanonicalName() );
        }
    }

    public Host findHost( Templatable resource, String authority ) {
        Host h = resource.getHost();
        while( h != null && !h.getName().equals( authority ) ) {
            h = h.getParentHost();
        }
        return h;
    }

    private User authenticateRecursive( Host h, String name, String password ) {
        User u = h.doAuthenticate( name, password );
        if( u != null ) {
            if( log.isTraceEnabled() ) {
                LogUtils.trace(log, "authenticateRecursive: authenticated ok on host: ", h.getName(), "user", name, password, "->", u.getPath());
            }
            return u;
        }

        Host hParent = h.getParentHost();
        if( hParent == null ) {
            LogUtils.trace(log, "authenticateRecursive: reached null host for user name: ", name);
            return null;
        } else {
            LogUtils.trace(log, "authenticateRecursive: not found here, delegate to parent host");
            return authenticateRecursive( hParent, name, password );
        }
    }

    private User authenticateRecursive( Host h, String name, DigestResponse digestRequest ) {
        User u = h.doAuthenticate( name, digestRequest );
        if( u != null ) {
            //log.debug( "found user: " + u.getName());
            return u;
        } else {
            //log.warn("failed to login: " + name + " to host: " + h.getName());
        }

        Host hParent = h.getParentHost();
        if( hParent == null ) {
            //log.warn( "authentication failed");
            return null;
        } else {
            return authenticateRecursive( hParent, name, digestRequest );
        }
    }
}

