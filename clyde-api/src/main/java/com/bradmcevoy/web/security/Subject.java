package com.bradmcevoy.web.security;

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
public interface Subject {
    String getSubjectName();
}
