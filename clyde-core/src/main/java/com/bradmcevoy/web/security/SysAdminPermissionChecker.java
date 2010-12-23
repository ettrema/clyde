package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.util.List;

/**
 * PermissionChecker which grants all roles to a designated list of
 * sys admins
 *
 * This wraps another PermissionChecker to allow chaining.
 *
 * The list of users must be strings in the form of:
 *   username@domainname
 *   Eg brad@www.ettrema.com
 *
 * @author brad
 */
public class SysAdminPermissionChecker implements PermissionChecker {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( SysAdminPermissionChecker.class );
    private final PermissionChecker wrapped;
    private final List<String> sysAdmins;

    public SysAdminPermissionChecker( PermissionChecker wrapped, List<String> sysAdmins ) {
        this.wrapped = wrapped;
        this.sysAdmins = sysAdmins;
    }

    @Override
    public boolean hasRole( Role role, Resource r, Auth auth ) {
        User user = null;
        if( auth != null ) {
            if( auth.getTag() instanceof User ) {
                user = (User) auth.getTag();
            }
        }

        if( isSysAdmin( user ) ) {
            log.trace( "hasRole: is sys admin" );
            return true;
        } else {
            log.trace( "hasRole: not sys admin so delegate" );
            return wrapped.hasRole( role, r, auth );
        }
    }

    private boolean isSysAdmin( User user ) {
        if( user == null ) {
            log.trace("isSysAdmin: no current user, so not sysadmin");
            return false;
        }

        String s = user.getName() + "@" + user.getHost().getName();
        boolean b = sysAdmins.contains( s );
        if( log.isTraceEnabled()) {
            log.trace("isSysAdmin: " + s + " = " + b);
        }
        return b;
    }
}
