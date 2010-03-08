package com.bradmcevoy.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class PluggableResourceCreator implements ResourceCreator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PluggableResourceCreator.class );
    private List<Creator> creators = new ArrayList<Creator>();
    Creator defaultCreator = new BinaryFileCreator();

    public PluggableResourceCreator() {
        log.debug( "creating PluggableResourceCreator" );
        addCreator( new PageCreator() );
        addCreator( new FolderCreator() );
        addCreator( new TextFileCreator() );
        addCreator( new ObjectCreaator() );
    }

    @Override
    public void addCreator( Creator creator ) {
        log.debug( "add creator: " + creator.getClass() );
        creators.add( creator );
    }

    @Override
    public BaseResource createResource( Folder folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        log.debug( "createResource: " + ct + " - " + newName );
        for( Creator c : creators ) {
            if( c.accepts( ct ) ) {
                BaseResource res = c.createResource( folder, ct, in, newName );
                if( res != null ) {
                    log.debug( "   creator: " + c.getClass() + " - " + res.getClass() );
                    return res;
                }
            }
        }
        log.debug( "no creator found. using default" );
        return defaultCreator.createResource( folder, ct, in, newName );
    }
}
