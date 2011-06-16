package com.bradmcevoy.utils;

import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.security.PermissionRecipient.Role;

/**
 * Determines the necessary roles for creating and editing resources
 *
 * @author brad
 */
public interface AuthoringPermissionService {
    /**
     * Determine the necessary role for creating a resource of the given template
     * in the given folder
     * 
     * @param folder - the folder in which a new resource may be created, if allowed
     * @param template - the template of the resource to create
     * @return - the necessary role
     */
    Role getCreateRole(Folder folder, ITemplate template);

    /**
     * Determine the necessary role for edting the given resource;
     *
     * @param res - the resource which may be edited if allowed
     * @return - the necessary role
     */
    Role getEditRole(Templatable res);
}
