package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.SourcePage;
import com.bradmcevoy.web.User;
import com.ettrema.console.Console;
import java.util.Arrays;
import java.util.List;

/**
 *  Checks that the current user is defined within the specified host.
 *
 *  Usually that host is the root.
 *
 * @author brad
 */
public class AdminOnlySourcePageAuthoriser implements ClydeAuthoriser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( AdminOnlySourcePageAuthoriser.class );
    private final List<String> adminHosts;

    public AdminOnlySourcePageAuthoriser( String adminHost ) {
        this.adminHosts = Arrays.asList( adminHost );
    }

    public AdminOnlySourcePageAuthoriser( List<String> adminHost ) {
        this.adminHosts = adminHost;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public Boolean authorise( Resource resource, Request request ) {
        log.debug( "authorise: " + resource.getName() + "(" + resource.getClass() + ")" );
        if( resource instanceof SourcePage || resource instanceof Console ) {
            Auth auth = request.getAuthorization();
            if( auth == null || auth.getTag() == null ) {
                return false;
            } else {
                User user;
                if( auth.getTag() instanceof User ) {
                    user = (User) auth.getTag();
                } else {
                    return false;
                }
                for( String adminHost : adminHosts ) {
                    if( adminHost.equals( user.getHost().getName() ) ) {
                        log.debug( "access granted to: " + user.getPath() );
                        return true;
                    }
                }
                return false;
            }
        } else {
            return null;
        }
    }
}
