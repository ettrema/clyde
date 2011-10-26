package com.ettrema.web.groups;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.security.UserGroup;

/**
 *
 * @author brad
 */
public interface GroupService {


    /**
     *
     *
     * @param relativeTo - find the group relative to this resource
     * @param name - the name of the group
     * @return - a system user group or a custom user group
     */
    UserGroup getGroup(Resource relativeTo, String name);
}
