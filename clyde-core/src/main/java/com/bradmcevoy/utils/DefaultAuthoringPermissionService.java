package com.bradmcevoy.utils;

import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.TemplateSpecs;
import com.bradmcevoy.web.TemplateSpecs.AllowTemplateSpec;
import com.bradmcevoy.web.security.PermissionRecipient.Role;

/**
 *
 * @author brad
 */
public class DefaultAuthoringPermissionService implements AuthoringPermissionService{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( DefaultAuthoringPermissionService.class );

    public static final Role DEFAULT_CREATE_ROLE = Role.AUTHOR;

    public static final Role DEFAULT_EDIT_ROLE = Role.AUTHOR;

    public Role getCreateRole( Folder folder, ITemplate template ) {
        TemplateSpecs specs = TemplateSpecs.getSpecsToUse( folder );
        if( specs != null ) {
            AllowTemplateSpec spec = specs.findAllowedSpec( folder, template );
            if( spec != null && spec.getCreateNewRole() != null ) {
                return spec.getCreateNewRole();
            }
        }
        return DEFAULT_CREATE_ROLE;
    }

    public Role getEditRole( Templatable res ) {
        Folder folder =  res.getParentFolder();
        ITemplate template = res.getTemplate();
        TemplateSpecs specs = TemplateSpecs.getSpecsToUse( folder );
        if( specs != null ) {
            AllowTemplateSpec spec = specs.findAllowedSpec( folder, template );
            if( spec != null && spec.getEditRole() != null ) {
                return spec.getEditRole();
            }
        }
        return DEFAULT_EDIT_ROLE;
    }

}
