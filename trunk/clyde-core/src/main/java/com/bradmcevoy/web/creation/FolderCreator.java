package com.bradmcevoy.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.Template;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

/**
 *
 * @author brad
 */
public class FolderCreator implements Creator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FolderCreator.class );

    public static ITemplate findNewFolderTemplate(Folder parent ){
        List<Template> allowedTemplates = parent.getAllowedTemplates();
        ITemplate template = null;
        if( !CollectionUtils.isEmpty( allowedTemplates ) ) {
            for( ITemplate t :allowedTemplates) {
                if( t.canCreateFolder() ) {
                    template = t;
                    break;
                }
            }
        }
        if( template == null ) {
            // this might locate a folder from the parent web
            template = parent.getTemplate( "folder" );
        }
        return template;
    }
    
    @Override
    public boolean accepts( String ct ) {
        return ct.contains( "folder" );
    }

    @Override
    public BaseResource createResource( Folder parent, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        ITemplate template = findNewFolderTemplate(parent);
        Folder f;
        if( template != null ) {
            log.debug( "using template: " + template.getName() );
            f = template.createFolderFromTemplate( parent, newName );
        } else {
            log.debug( "no template called folder" );
            f = new Folder( parent, newName );
        }
        f.save();
        return f;


    }
}
