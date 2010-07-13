package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;

/**
 *
 * @author brad
 */
public class RelativeTemplateManager implements TemplateManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RelativeTemplateManager.class );

    private final TemplateManager templateManager;

    public RelativeTemplateManager( TemplateManager templateManager ) {
        this.templateManager = templateManager;
    }

    public ITemplate lookup( String templateName, Web web ) {
        log.debug( "lookup: " + templateName + " web:" + web.getName());
        if( templateName.startsWith( ".." ) ) {
            Path path = Path.path( templateName );
            return lookup( path.getParts(), web, 0 );
        } else {
            return templateManager.lookup( templateName, web );
        }
    }

    private ITemplate lookup( String[] parts, Web web, int partNum ) {
        String part = parts[partNum];
        log.debug( "lookup: " + part + " web: " + web.getName());
        if( part.equals( ".." ) ) {
            return lookup( parts, web.getParentWeb(), partNum + 1 );
        } else {
            return templateManager.lookup( part, web );
        }
    }
}
