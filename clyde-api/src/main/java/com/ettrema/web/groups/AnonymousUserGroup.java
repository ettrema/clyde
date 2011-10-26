package com.ettrema.web.groups;

import com.ettrema.web.security.Subject;
import com.ettrema.web.security.SystemUserGroup;

/**
 * Represents anyone or anything, regardless of whether or not they are logged in
 * (which means the name is a bit of a mis-nomer, it doesnt *only* apply to
 * anonymous users)
 *
 * @author brad
 */
public class AnonymousUserGroup implements SystemUserGroup {

    public String getSubjectName() {
        return "Anonymous";
    }

    public boolean isInGroup( Subject user ) {
        return true;
    }

    public boolean isOrContains(Subject s) {
        return true;
    }



}
