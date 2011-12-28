package com.ettrema.web.security;

import com.ettrema.http.acl.Principal;

/**
 * A security Subject is anything which can be the subject of a security rule
 *
 * This will typically be
 *  - users
 *  - user groups
 *  - system defined user groups (eg everyone, anonymous)
 *
 * @author brad
 */
public interface Subject extends Principal {
    /**
     * The identifying name of this resource, within its collection (if there is one)
     *
     * This will be the filename for persisted users and groups, and will be
     * a system wide identifier for system groups (eg ANONYMOUS)
     *
     * @return
     */
    String getSubjectName();

    /**
     * Returns true if the given subject is logically the same as this subject,
     * or if this subject is a group which contains the given subject
     *
     *
     */
    boolean isOrContains(Subject s);
}
