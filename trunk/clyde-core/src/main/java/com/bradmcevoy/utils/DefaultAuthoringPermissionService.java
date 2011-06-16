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
public class DefaultAuthoringPermissionService implements AuthoringPermissionService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( DefaultAuthoringPermissionService.class );
    public static final Role DEFAULT_CREATE_ROLE = Role.AUTHOR;
    public static final Role DEFAULT_EDIT_ROLE = Role.AUTHOR;

    public Role getCreateRole( Folder folder, ITemplate template ) {
        TemplateSpecs specs = TemplateSpecs.getSpecsToUse( folder );
        if( specs != null ) {
            AllowTemplateSpec spec = specs.findAllowedSpec( folder, template );
            if( spec != null && spec.getCreateNewRole() != null ) {
                Role r = spec.getCreateNewRole();
                log.trace( "create role: " + r );
                return r;
            } else {
                log.trace( "no template specs or no create role specified" );
            }
        } else {
            log.trace( "no specs for create" );
        }
        return DEFAULT_CREATE_ROLE;
    }

    public Role getEditRole( Templatable res ) {
        if( log.isTraceEnabled() ) {
            log.trace( "getEditRole: " + res.getHref() + " - " + res.getClass() );
        }
        Folder folder = res.getParentFolder();
        ITemplate template = res.getTemplate();
        TemplateSpecs specs = TemplateSpecs.getSpecsToUse( folder );
        if( specs != null ) {
            if( log.isTraceEnabled() ) {
                log.trace( " found allowed template specs: " + specs );
            }
            AllowTemplateSpec spec = specs.findAllowedSpec( folder, template );
            if( spec != null && spec.getEditRole() != null ) {
                Role r = spec.getEditRole();
                log.trace( "edit role: " + r );
                return r;
            } else {
                if( log.isTraceEnabled() ) {
                    log.trace( "no template spec edit role specified for folder: " + folder.getUrl() );
                }
            }
        } else {
            log.trace( "no specs for edit" );
        }
        return DEFAULT_EDIT_ROLE;
    }
}
