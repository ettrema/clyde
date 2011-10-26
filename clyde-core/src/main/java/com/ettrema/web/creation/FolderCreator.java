package com.ettrema.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.ettrema.utils.LogUtils;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.ITemplate;
import com.ettrema.web.Template;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author brad
 */
public class FolderCreator implements Creator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FolderCreator.class );

    public static ITemplate findNewFolderTemplate(Folder parent ){
		LogUtils.trace(log, "findNewFolderTemplate: folder name: ", parent.getName());
		ITemplate folderTemplate = parent.getTemplate( "folder" );
		if( folderTemplate != null ) {
			log.trace("findNewFolderTemplate: found a template called 'folder'");
			return folderTemplate;
		}
		List<Template> templateSpecs = parent.getAllowedTemplates();
		LogUtils.trace(log, "findNewFolderTemplate: got template specs", templateSpecs.size());
		for( Template t : templateSpecs ) {
			if( t.represents("folder")) {
				LogUtils.trace(log, "findNewFolderTemplate: Found template for folder", t.getName());
				return t;
			}
		}
		log.trace("findNewFolderTemplate: couldnt find a folder template");
		return null;
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
