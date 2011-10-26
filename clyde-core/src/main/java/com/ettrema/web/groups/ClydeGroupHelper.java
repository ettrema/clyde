package com.bradmcevoy.web.groups;

import com.bradmcevoy.web.Group;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.security.CustomUserGroup;
import com.bradmcevoy.web.security.Subject;
import com.bradmcevoy.web.security.UserGroup;
import java.util.List;

/**
 *
 * @author brad
 */
public interface ClydeGroupHelper {
    public List<Subject> getMembers( UserGroup group );

    public void addToGroup( IUser user, CustomUserGroup group );

    /**
     * Called from Group.isInGroup
     *
     * @param user
     * @param group
     * @return
     */
    boolean isInGroup(IUser user, Group group);
}
