package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.User;
import java.util.Arrays;
import java.util.List;

/**
 *  Checks that the current user is defined within the specified host.
 *
 *  If so they are always granted access. If not, this authoriser has no opinion
 *
 * @author brad
 */
public class RootDomainAuthoriser implements ClydeAuthoriser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RootDomainAuthoriser.class );
    private final List<String> adminHosts;

    public RootDomainAuthoriser( String adminHost ) {
        this.adminHosts = Arrays.asList( adminHost );
    }

    public RootDomainAuthoriser( List<String> adminHost ) {
        this.adminHosts = adminHost;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method, Auth auth ) {
        if( auth == null ) {
            return null;
        } else {
            User user;
            if( auth.getTag() instanceof User ) {
                user = (User) auth.getTag();
            } else {
                user = null;
            }
            if( user != null ) {
                for( String allowedHost : adminHosts ) {
                    if( allowedHost.equals( user.getHost().getName() ) ) {
                        log.debug( "access granted to: " + user.getPath() );
                        return true;
                    }
                }
            }
            return null;
        }
    }
}
