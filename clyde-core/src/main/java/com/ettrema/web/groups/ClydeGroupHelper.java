package com.ettrema.web.groups;

import com.ettrema.web.Group;
import com.ettrema.web.IUser;
import com.ettrema.web.security.CustomUserGroup;
import com.ettrema.web.security.Subject;
import com.ettrema.web.security.UserGroup;
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
    
    /**
     * Get all explicitly stated groups for a user. Does not include automatic
     * groups like Authenticated
     * 
     * 
     * @param user
     * @return 
     */
    List<Group> getGroups(IUser user);
}
