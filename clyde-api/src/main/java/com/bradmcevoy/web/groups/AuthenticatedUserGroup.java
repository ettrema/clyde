package com.bradmcevoy.web.groups;

import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.security.Subject;
import com.bradmcevoy.web.security.SystemUserGroup;

/**
 *
 * @author brad
 */
public class AuthenticatedUserGroup implements SystemUserGroup {

    public String getSubjectName() {
        return "Authenticated";
    }

    public boolean isInGroup( Subject user ) {
        if( user instanceof IUser ) {
            return user != null;
        } else {
            return false;
        }
    }
}
