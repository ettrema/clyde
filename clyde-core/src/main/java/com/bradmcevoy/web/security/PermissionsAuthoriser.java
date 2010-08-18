package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.utils.AuthoringPermissionService;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.security.PermissionRecipient.Role;

/**
 *
 * @author brad
 */
public class PermissionsAuthoriser implements ClydeAuthoriser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PermissionsAuthoriser.class );
    private final PermissionChecker permissionChecker;
    private final AuthoringPermissionService authoringPermissionService;

    /**
     *
     * @param permissionChecker - used to check if a user has the required permission
     * @param authoringPermissionService - used to determine what permission is required for a certain action
     */
    public PermissionsAuthoriser( PermissionChecker permissionChecker, AuthoringPermissionService authoringPermissionService ) {
        this.permissionChecker = permissionChecker;
        this.authoringPermissionService = authoringPermissionService;
    }

    @Override
    public String getName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method ) {
        Role requiredRole = findRole( resource, method );
        if( requiredRole == null ) {
            return null;
        } else {
            Boolean bb = permissionChecker.hasRole( requiredRole, resource, request.getAuthorization() );
            if( bb != null && !bb.booleanValue() ) {
                log.warn( "denying access due to permissionChecker: " + permissionChecker.getClass() + " for role: " + requiredRole.name() );
            }
            return bb;
        }
    }

    private Role findRole( Resource resource, Method method ) {
        if( resource instanceof Templatable ) {
            boolean isEdit = isMethod( method, new Method[]{Method.PROPPATCH, Method.COPY, Method.DELETE, Method.MOVE, Method.LOCK, Method.PUT, Method.UNLOCK, Method.MKCOL} );
            Templatable t = (Templatable) resource;
            if( isEdit ) {
                return authoringPermissionService.getEditRole( t );
            } else {
                if( isMethod( method, new Method[]{Method.PROPFIND, Method.GET, Method.HEAD, Method.OPTIONS, Method.POST} ) ) {
                    return Role.VIEWER;
                }
                throw new RuntimeException( "Unhandled method in permissionsauthoriser: " + method );
            }
        } else {
            Method m = method;

            if( isMethod( method, new Method[]{Method.PROPPATCH, Method.COPY, Method.DELETE, Method.MOVE, Method.LOCK, Method.PUT, Method.UNLOCK, Method.MKCOL} ) )
                return Role.AUTHOR;
            // allow post for viewers, so they can add comments etc. actual permissions must be
            // checked by components
            if( isMethod( method, new Method[]{Method.PROPFIND, Method.GET, Method.HEAD, Method.OPTIONS, Method.POST} ) )
                return Role.VIEWER;
            throw new RuntimeException( "Unhandled method in permissionsauthoriser: " + m );
        }
    }

    private boolean isMethod( Method m, Method[] methods ) {
        for( Method m1 : methods ) {
            if( m1.equals( m ) ) return true;
        }
        return false;
    }
}
