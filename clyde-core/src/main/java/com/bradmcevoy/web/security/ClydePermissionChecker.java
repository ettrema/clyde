package com.bradmcevoy.web.security;

import com.bradmcevoy.web.IUser;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.PermissionRecipient.Role;

import static  com.ettrema.context.RequestContext._;

/**
 * Implementation of PermissionChecker which works for Clyde resources
 * 
 * Uses the permissions() method on BaseResource to check for permissions
 *
 * @author brad
 */
public class ClydePermissionChecker implements PermissionChecker {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ClydePermissionChecker.class );
    private final boolean allowAnonymous;

    /**
     *
     * @param allowAnonymous - true to allow anonymous access when the ANONYMOUS role
     * is required
     */
    public ClydePermissionChecker( boolean allowAnonymous ) {
        this.allowAnonymous = allowAnonymous;
    }

    public ClydePermissionChecker() {
        allowAnonymous = true;
    }

    @Override
    public boolean hasRole( Role role, Resource r, Auth auth ) {
        log.debug( "hasRole: " + role );
        if( allowAnonymous && role.equals( Role.ANONYMOUS ) ) {
            return true;
        }
        if( role.equals( Role.AUTHENTICATED ) ) {
            return auth != null && auth.getTag() != null;
        }
        if( r instanceof BaseResource ) {
            BaseResource res = (BaseResource) r;
            IUser iuser = _(CurrentUserService.class).getSecurityContextUser();
            if( iuser == null ) {
                log.trace( "no current user so deny access");
                return false;
            } else if( !(iuser instanceof User) ){
                log.trace("incompatible user type");
                return false;
            }
            return hasRoleRes( (User)iuser, res, role );
        } else if( r instanceof Templatable ) {
            Templatable templatable = (Templatable) r;
            boolean b = hasRole( role, templatable.getParent(), auth );
            if( !b ) {
                log.warn( "user does not have role: " + role + " on resource: " + templatable.getHref() );
            }
            return b;
        } else {
            log.warn( "ClydePermissionChecker cannot check permission on resource of type: " + r.getClass() + " Saying no to be safe" );
            return false;
        }
    }

    private boolean hasRoleRes( User user, BaseResource res, Role role ) {
        if( res == null ) {
            log.trace("resource is null");
            return false;
        }

        Permissions ps = res.permissions();
        if( ps != null ) {
            if( ps.allows( user, role ) ) {
                return true;
            }
        }
        if( res instanceof Host ) {
            log.trace("reached host, no permissions found");
            return false;
        } else {
            return hasRoleRes( user, res.getParent(), role );
        }
    }


}
