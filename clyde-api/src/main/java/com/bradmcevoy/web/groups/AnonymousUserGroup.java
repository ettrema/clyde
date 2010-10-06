package com.bradmcevoy.web.groups;

import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.security.SystemUserGroup;

/**
 *
 * @author brad
 */
public class AnonymousUserGroup implements SystemUserGroup {

    public String getSubjectName() {
        return "Anonymous";
    }

    public boolean isInGroup( IUser user ) {
        return true;
    }

}
