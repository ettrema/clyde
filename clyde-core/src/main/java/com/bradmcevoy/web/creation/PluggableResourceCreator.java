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
        //addCreator( new PageCreator() );
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
                    log.debug( "   creator: " + c.getClass() + " - " + res.getClass() + " ct:" + res.getContentType( null) );
                    return res;
                }
            }
        }
        log.debug( "no creator found. using default" );
        return defaultCreator.createResource( folder, ct, in, newName );
    }

    public void setExtraCreators(List<Creator> creators) {
        for( Creator c : creators ) {
            log.debug( "add creator to head: " + c.getClass() );
            this.creators.add(0, c); // Add to head of list, becuase we want these to be preferred to standard ones
        }
    }
}
