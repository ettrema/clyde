package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.security.PermissionRecipient.Role;

/**
 *
 * @author brad
 */
public class PermissionsAuthoriser implements ClydeAuthoriser{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PermissionsAuthoriser.class );

    private final PermissionChecker permissionChecker;

    public PermissionsAuthoriser( PermissionChecker permissionChecker ) {
        this.permissionChecker = permissionChecker;
    }


    @Override
    public String getName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method ) {
//        if( request.getAuthorization() == null ) {
//            log.debug( "no one logged in, so no opinion");
//            return null; // no opinion
//        }
        Role requiredRole = findRole(method);
        if( requiredRole == null ) {
//            log.debug( "no required role, ignoring");
            return null;
        } else {
//            log.debug( "requires: " + requiredRole);
            Boolean bb = permissionChecker.hasRole( requiredRole, resource, request.getAuthorization() );
            if( bb != null && !bb.booleanValue()) {
                log.warn( "denying access due to permissionChecker: " + permissionChecker.getClass() + " for role: " + requiredRole.name());
            }
            return bb;
        }
    }

    private Role findRole( Method method ) {
        Method m = method;

        if( isMethod( method, new Method[] {Method.PROPPATCH, Method.COPY, Method.DELETE, Method.MOVE, Method.LOCK, Method.PUT, Method.UNLOCK, Method.MKCOL}) ) return Role.AUTHOR;
        // allow post for viewers, so they can add comments etc. actual permissions must be
        // checked by components
        if( isMethod( method, new Method[] {Method.PROPFIND, Method.GET, Method.HEAD, Method.OPTIONS, Method.POST})) return Role.VIEWER;
        throw new RuntimeException( "Unhandled method in permissionsauthoriser: " + m );
    }

    private boolean isMethod(Method m, Method[] methods) {
        for( Method m1 : methods ) {
            if( m1.equals( m)) return true;
        }
        return false;
    }

}
