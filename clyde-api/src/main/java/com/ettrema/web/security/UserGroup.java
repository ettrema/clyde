package com.ettrema.web.security;

/**
 * Represents a group of users for a security purpose
 *
 * @author brad
 */
public interface UserGroup extends Subject {
    boolean isInGroup( Subject subject );
}
