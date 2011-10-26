package com.ettrema.web.groups;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.IUser;
import com.ettrema.web.security.CustomUserGroup;
import com.ettrema.web.security.Subject;
import com.ettrema.web.security.UserGroup;
import java.util.List;

/**
 * Only knows about system groups
 *
 * @author brad
 */
public class SystemGroupService implements GroupService {

    public static final AnonymousUserGroup anonymousUserGroup = new AnonymousUserGroup();

    public static final AuthenticatedUserGroup authenticatedUserGroup = new AuthenticatedUserGroup();

    public List<Subject> getMembers( UserGroup group ) {
        return null;
    }

    public void addToGroup( IUser user, CustomUserGroup group ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public UserGroup getGroup( Resource relativeTo, String name ) {
        if( anonymousUserGroup.getSubjectName().equals( name )) {
            return anonymousUserGroup;
        } else if( authenticatedUserGroup.getSubjectName().equals( name )) {
            return authenticatedUserGroup;
        } else {
            return null;
        }
    }

}
