package com.bradmcevoy.web.groups;

import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.security.SystemUserGroup;

/**
 *
 * @author brad
 */
public class AuthenticatedUserGroup implements SystemUserGroup {

    public String getSubjectName() {
        return "Authenticated";
    }

    public boolean isInGroup( IUser user ) {
        return user != null;
    }

}
