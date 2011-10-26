package com.ettrema.web.groups;

import com.ettrema.web.IUser;
import com.ettrema.web.security.Subject;
import com.ettrema.web.security.SystemUserGroup;

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

    public boolean isOrContains(Subject s) {
        return isInGroup(s);
    }


}
