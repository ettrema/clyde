package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.security.PermissionRecipient.Role;

/**
 * Transient instances of this class represent a granted permission. Note that
 * these instances DO NOT get persisted. Instead, they are constructed as needed
 * by the relationship name and the permitted resoruce
 *
 * @author brad
 */
public class Permission {


    /**
     * the role this permission grants
     */
    private final Role role;

    /**
     * the user the permission is granted to
     */
    private final Subject grantee;

    /**
     * the resource which permission has been granted on
     */
    private final Resource granted;

    public Permission( Role role, Subject grantee, Resource granted ) {
        this.role = role;
        this.grantee = grantee;
        this.granted = granted;
    }

    public Role getRole() {
        return role;
    }

    public Subject getGrantee() {
        return grantee;
    }

    public Resource getGranted() {
        return granted;
    }

    @Override
    public String toString() {
        return "role: " + role.name() + " granted to: " + grantee.getSubjectName() + " on resource: " + granted.getName();
    }    
}
