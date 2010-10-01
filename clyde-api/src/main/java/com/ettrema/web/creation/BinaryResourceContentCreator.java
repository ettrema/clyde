package com.ettrema.web.creation;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.security.CurrentUserService;
import com.ettrema.web.resources.BinaryResource;
import com.ettrema.web.resources.FolderResource;
import java.io.InputStream;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class BinaryResourceContentCreator implements ContentCreator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( BinaryResourceContentCreator.class );

    @Override
    public boolean accepts( String contentType ) {
        return true;
    }

    @Override
    public Resource createResource( CollectionResource folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        log.debug( "create binary file with content type: " + ct );
        BinaryResource file = new BinaryResource( ct, (FolderResource) folder, newName);
        IUser creator = _(CurrentUserService.class).getOnBehalfOf();

        if( creator instanceof IUser){
            file.setCreator( creator );
        }
        file.save();
        if( in != null ) {
            file.setContent( in );
        }
        log.debug( "content type: " + file.getContentType( null ) );
        return file;
    }
}
