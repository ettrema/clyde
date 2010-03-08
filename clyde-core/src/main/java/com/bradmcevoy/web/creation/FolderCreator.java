package com.bradmcevoy.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public class FolderCreator implements Creator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FolderCreator.class );

    @Override
    public boolean accepts(String ct) {
        return ct.contains("folder");
    }

    @Override
    public BaseResource createResource(Folder parent, String ct, InputStream in, String newName) throws ReadingException, WritingException {
        ITemplate template = parent.getTemplate( "folder");
        Folder f;
        if( template != null ) {
            log.debug("using template: " + template.getName());
            f = template.createFolderFromTemplate( parent, newName );
        } else {
            log.debug( "no template called folder");
            f = new Folder(parent, newName);
        }
        f.save();
        return f;

    }
}
