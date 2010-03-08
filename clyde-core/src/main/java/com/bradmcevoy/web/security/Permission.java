package com.bradmcevoy.web.security;

import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.security.PermissionRecipient.Role;

/**
 * Transient instances of this class represent a granted permission. Note that
 * these instances DO NOT get persisted. Instead, they are constructed as needed
 * by the relationship name and the permitted resoruce
 *
 * @author brad
 */
public class Permission extends VfsCommon {


    /**
     * the role this permission grants
     */
    private final Role role;

    /**
     * the user the permission is granted to
     */
    private final PermissionRecipient grantee;

    /**
     * the resource which permission has been granted on
     */
    private final BaseResource granted;

    public Permission( Role role, PermissionRecipient grantee, BaseResource granted ) {
        this.role = role;
        this.grantee = grantee;
        this.granted = granted;
    }

    public Role getRole() {
        return role;
    }

    public PermissionRecipient getGrantee() {
        return grantee;
    }

    public BaseResource getGranted() {
        return granted;
    }

    @Override
    public String toString() {
        return "role: " + role.name() + " granted to: " + grantee.getNameNode().getName() + " on resource: " + granted.getName();
    }


    
}
