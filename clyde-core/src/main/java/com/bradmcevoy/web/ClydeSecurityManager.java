package com.bradmcevoy.web;

import com.bradmcevoy.context.RootContext;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 *
 * @author brad
 */
public class ClydeSecurityManager implements com.bradmcevoy.http.SecurityManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ClydeSecurityManager.class );
    private final ResourceFactory resourceFactory;
    private final RootContext rootContext;
    private final HostFinder hostFinder = new HostFinder();

    public ClydeSecurityManager( ResourceFactory resourceFactory, RootContext rootContext ) {
        this.resourceFactory = resourceFactory;
        this.rootContext = rootContext;
    }

    @Override
    public Object authenticate( String user, String password ) {
        NameAndAuthority na = NameAndAuthority.parse( user );
        String shost = na.authority;
        if( na.authority != null ) {
            NameAndAuthority naDom = NameAndAuthority.parse( na.authority);
            //if(naDom.authority)
        }
        Host host = findHost( na.authority );
        if( host == null ) {
            log.debug( "authenticate: host not found: " + na.authority );
            return null;
        } else {
            return host.doAuthenticate( na.name, password );
        }
    }

    @Override
    public Object authenticate( DigestResponse digestRequest ) {
        NameAndAuthority na = NameAndAuthority.parse( digestRequest.getUser() );
        String shost = na.authority;
        if( na.authority != null ) {
            NameAndAuthority naDom = NameAndAuthority.parse( na.authority);
            //if(naDom.authority)
        }
        Host host = findHost( na.authority );
        if( host == null ) {
            log.debug( "authenticate: host not found: " + na.authority );
            return null;
        } else {
            return host.doAuthenticate( na.name, digestRequest );
        }

    }



    @Override
    public boolean authorise( Request req, Method method, Auth auth, Resource resource ) {
        return resource.authorise( req, method, auth );
    }

    public String getRealm() {
        return null;
    }

    public String getRealm(String host) {
        return host;
    }

    public Host findHost( String authority ) {
        return hostFinder.getHost( authority );
    }
}
