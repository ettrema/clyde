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
        if( auth != null ) user = (User) auth.getTag();

        if(isSysAdmin(user)) {
            return true;
        } else {
            return wrapped.hasRole( role, r, auth );
        }
    }

    private boolean isSysAdmin( User user ) {
        if( user == null ) return false;

        String s = user.getName() + "@" + user.getHost().getName();
        return sysAdmins.contains( s );
    }
}
