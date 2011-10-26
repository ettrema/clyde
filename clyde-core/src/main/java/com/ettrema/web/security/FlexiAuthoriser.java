package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class FlexiAuthoriser implements ClydeAuthoriser{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FlexiAuthoriser.class );

    private final Class theClass;

    private final List<Request.Method> methods;

    private final boolean allow;

    private final Role role;

    private final PermissionChecker permissionChecker;

    public FlexiAuthoriser( Class theClass, List<String> sMethods, boolean allow, String sRole, PermissionChecker permissionChecker ) {
        this.theClass = theClass;
        this.allow = allow;
        this.permissionChecker = permissionChecker;
        this.methods = new ArrayList<Method>();
        this.role = Role.valueOf( sRole );
        for( String s : sMethods ) {
            Request.Method m = Request.Method.valueOf( s );
            this.methods.add( m );
        }
    }

    /**
     * Using this constructor will mean that roles are never checked. Users
     * will always be allowed or denied for matching requests. Unless, of course,
     * some other authoriser has previously allowed it..
     *
     * @param theClass
     * @param sMethods
     * @param allow
     */
    public FlexiAuthoriser( Class theClass, List<String> sMethods, boolean allow ) {
        this.theClass = theClass;
        this.allow = allow;
        this.methods = new ArrayList<Method>();
        this.role = null;
        this.permissionChecker = null;
        for( String s : sMethods ) {
            Request.Method m = Request.Method.valueOf( s );
            this.methods.add( m );
        }
    }



    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method, Auth auth) {
        if( resource.getClass().isAssignableFrom( theClass )) {
            for( Request.Method m : methods ) {
                if( m.equals( method )) {
                    log.debug( "Found matching request");
                    if( role == null || permissionChecker.hasRole( role, resource, auth)) {
                        log.debug( "user is of type");
                        return allow;
                    }
                }
            }
        }
        return null;
    }

}
