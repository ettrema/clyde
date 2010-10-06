package com.bradmcevoy.web.security;

import com.bradmcevoy.web.IUser;

/**
 * Represents a group of users for a security purpose
 *
 * @author brad
 */
public interface UserGroup extends Subject {
    boolean isInGroup( IUser user );
}
