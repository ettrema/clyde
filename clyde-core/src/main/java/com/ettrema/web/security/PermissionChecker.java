package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.security.PermissionRecipient.Role;

/**
 * Interface to check user permissions on resources
 *
 * This could possibly be moved to milton at some point
 *
 * @author brad
 */
public interface PermissionChecker {
    /**
     * checks to see if the given user should be allowed the requested role
     * on the requested resource
     *
     * @param role
     * @param resource
     * @param auth
     * @return
     */
    public boolean hasRole( Role role, Resource r, Auth auth );
}
