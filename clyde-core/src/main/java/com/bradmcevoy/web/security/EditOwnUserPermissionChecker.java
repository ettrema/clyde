package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.util.UUID;

/**
 * Allows editing of a user's own user page
 *
 * @author brad
 */
public class EditOwnUserPermissionChecker implements PermissionChecker {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( EditOwnUserPermissionChecker.class );
    private final PermissionChecker wrapped;

    public EditOwnUserPermissionChecker( PermissionChecker wrapped ) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean hasRole( Role role, Resource r, Auth auth ) {
        User user = null;
        if( auth != null ) user = (User) auth.getTag();

        if( user != null ) {
            if( isResourceOwnPath( user, r ) ) {
                if( role.equals( Role.AUTHOR )) {
                    return true;
                }
            }
        }
        return wrapped.hasRole( role, r, auth );
    }

    private boolean isResourceOwnPath( User user, Resource r ) {
        if( r instanceof User ) {
            User userRequested = (User) r;
            UUID nn = userRequested.getNameNodeId();
            return ( nn != null && nn.equals( user.getNameNodeId() ) );
        } else {
            return false;
        }
    }
}
