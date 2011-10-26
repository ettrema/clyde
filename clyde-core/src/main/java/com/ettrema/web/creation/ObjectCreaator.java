package com.ettrema.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import java.io.InputStream;
import java.lang.reflect.Constructor;

/**
 *
 * @author brad
 */
public class ObjectCreaator implements Creator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ObjectCreaator.class);

    @Override
    public boolean accepts( String contentType ) {
        return contentType != null && contentType.startsWith( "javaobject:");
    }

    @Override
    public BaseResource createResource( Folder folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        log.debug( "create: " + ct);
        String cls = ct.substring( ct.indexOf( ":")+1 );
        try {
            Class targetClass = Class.forName( cls );
            Constructor con = targetClass.getConstructor( Folder.class, String.class );
            BaseResource res = (BaseResource) con.newInstance( folder, newName );
            res.save();
            return res;
        } catch( Exception e ) {
            throw new RuntimeException( cls + " - " + newName, e );
        }
    }
}
