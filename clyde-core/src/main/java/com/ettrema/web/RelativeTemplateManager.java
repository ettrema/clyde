package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;

/**
 * If the templateName contains a '/' then apply relative processing. The templateName
 * is parsed as a path and the path is evaluated from the given folder (which will
 * be a Web initially)
 *
 * The path may contain '..' components which will navigate up one folder
 *
 * The path may contain '.' components which means the current folder.
 *
 * Absolute paths (ie beginning with '/') are not supported
 *
 * If the templateName does contain any '/' characters the lookup is delegated
 * to the wrapped templateManager
 *
 * Examples
 *
 * normal  -> apply usual processing using wrapped TemplateManager
 * ../otherTemplate -> look for template "otherTemplate" in the parent web
 * themes/default/normal -> look for template "normal" in /themes/default from the root of the current web
 *
 * @author brad
 */
public class RelativeTemplateManager implements TemplateManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RelativeTemplateManager.class );

    private final TemplateManager templateManager;

    public RelativeTemplateManager( TemplateManager templateManager ) {
        this.templateManager = templateManager;
    }

    @Override
    public ITemplate lookup( String templateName, Folder web ) {
        if( templateName == null ) {
            log.debug( "template name is null");
            return null;
        }
        if( log.isTraceEnabled() ) {
            log.trace( "lookup: " + templateName + " web:" + web.getName());
        }
        if( templateName.contains( "/") ) {
            Path path = Path.path( templateName );
            ITemplate t = lookup( path.getParts(), web, 0 );
            if( log.isDebugEnabled()) {
                if( t == null ) {
                    log.debug("Didnt find template: " + templateName);
                } else {
                    log.debug( "got template: " + t.getHref());
                }
            }
            return t;
        } else {
            return templateManager.lookup( templateName, web );
        }
    }

    private ITemplate lookup( final String[] parts, final Folder folder, final int partNum ) {
        String part = parts[partNum];
        if( log.isTraceEnabled() ) {
            log.trace( "lookup: " + part + " web: " + folder.getName());
        }
        if( part.equals( ".." ) ) {
            return lookup( parts, folder.getParent(), partNum + 1 );
        } else if( part.equals( ".")) {
            return lookup( parts, folder, partNum + 1 );
        } else {
            if( partNum == parts.length-1) {
                return templateManager.lookup( part, folder );
            } else {
                BaseResource child = folder.childRes( part );
                if( child == null ) {
                    log.error( "Exception looking up relative tempate. Child: " + part + " not found in folder: " + folder.getHref());
                    return null;
                } else if( child instanceof Folder ) {
                    Folder nextFolder = (Folder) child;
                    return lookup(parts, nextFolder, partNum+1);
                } else {
                    log.error( "Exception looking up relative tempate. Not a folder: " + child.getHref());
                    return null;
                }
            }
        }
    }
}
