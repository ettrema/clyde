package com.bradmcevoy.web.security;

import com.bradmcevoy.web.IUser;
import com.ettrema.vfs.RelationalNameNode;
import java.util.UUID;

/**
 *  Interface to identify classes which can be associated with permissions
 * 
 *  Eg User and Invitation
 *
 * @author brad
 */
public interface PermissionRecipient {
    /**
     * All roles which can be granted
     *
     * Roles are defined discretely, and are not necessarily additive. For
     * example, it is permissible for an Adminstrator to be denied access of
     * an Author.
     *
     * Components do not need to query for all Roles they intend to allow. Instead,
     * they should just query for the least secure Role, and assume that a higher privildeged
     * user can grant themselves access if desired.
     *
     * The SYSADMIN role is special as, by definition, it applies to the entire
     * environment and not any particular resource. It should not normally be
     * controlled through runtime software, but should be part of machine configuration
     *
     * However, it is convenient to be able to query for this role through a
     * consistent resource-centric API.
     *
     */
    public enum Role {
        /**
         * someone who has physical control of the machine. implies complete control
         */
        SYSADMIN,

        /**
         * someone who has ownership of the resource in question. implies complete
         * control for the resource and all of its children, but not of things
         * which affect the whole environment.
         */
        OWNER,

        /**
         * someone who has been given responsibility to administer the resource. Eg
         * a website admin. implies access to grant permissions to other users
         *
         * an admin might be contrained by settings defined by the owner
         */
        ADMINISTRATOR,

        /**
         * someone given access to create and manage content. implies create, edit
         * and delete
         */
        AUTHOR,

        /**
         * someone given access to view resources. this may permit adding or changing
         * some resources in some circumstances, so doesnt necessarily correlate to
         * read only access.
         */
        VIEWER,

        /**
         * More of a group then a role, represents any logged in user. Here for convenience
         * , probably should be refactored out with groups
         */
        AUTHENTICATED,

        /**
         * Special role which represents any user whether logged in or not
         */
        ANONYMOUS;

    }

    /**
     * The ID of this resource.
     *
     * @return
     */
    public UUID getNameNodeId();

    /**
     *  The name node, used to create relationships to the resources being
     * granted permission
     *
     * @return
     */
    public RelationalNameNode getNameNode();

    /**
     * The actual user this object refers to. This object might be a user, or
     * it might be some thing which delegates to a user
     *
     * @return
     */
    public IUser getUser();
}
