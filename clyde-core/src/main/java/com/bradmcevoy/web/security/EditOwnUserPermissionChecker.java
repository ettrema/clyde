package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.IUser;
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
        IUser user = null;
        if( auth != null ) {
            if( auth.getTag() instanceof IUser ) {
                user = (User) auth.getTag();
            }
        }

        if( user != null ) {
            if( isResourceOwnPath( user, r ) ) {
                if( role.equals( Role.AUTHOR ) || role.equals( Role.VIEWER ) ) {
                    return true;
                }
            }
        }
        boolean b = wrapped.hasRole( role, r, auth );
        if( !b ) {
            if( log.isDebugEnabled() ) {
                log.debug( "denying access because wrapped permission checker said so: " + wrapped.getClass() );
            }
        }
        return b;
    }

    private boolean isResourceOwnPath( IUser user, Resource r ) {
        User persisted = findUserFrom( r );
        if( persisted != null ) {
            UUID nn = persisted.getNameNodeId();
            return ( nn != null && nn.equals( user.getNameNodeId() ) );
        } else {
            return false;
        }
    }

    private User findUserFrom( Resource r ) {
        if( r instanceof User ) {
            return (User) r;
        } else if( r instanceof Host ) {
            return null;
        } else if( r instanceof CommonTemplated ) {
            CommonTemplated ct = (CommonTemplated) r;
            return findUserFrom( ct.getParentFolder() );
        } else {
            return null;
        }
    }
}
