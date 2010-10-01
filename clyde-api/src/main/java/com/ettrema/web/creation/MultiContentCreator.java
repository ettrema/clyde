package com.ettrema.web.creation;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author brad
 */
public class MultiContentCreator implements ContentCreator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MultiContentCreator.class );

    private List<ContentCreator> creators = new ArrayList<ContentCreator>();

    public MultiContentCreator() {
    }

    @Override
    public Resource createResource( CollectionResource folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        log.debug( "createResource: " + ct + " - " + newName );
        for( ContentCreator c : creators ) {
            if( c.accepts( ct ) ) {
                Resource res = c.createResource( folder, ct, in, newName );
                return res;
            }
        }
        return null;
    }

    public List<ContentCreator> getCreators() {
        return creators;
    }

    public void setCreators( List<ContentCreator> creators ) {
        this.creators = creators;
    }

    public boolean accepts( String contentType ) {
        return true;
    }
}
