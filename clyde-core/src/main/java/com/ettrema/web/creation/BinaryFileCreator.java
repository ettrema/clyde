package com.ettrema.web.creation;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.ettrema.web.BaseResource;
import com.ettrema.web.BinaryFile;
import com.ettrema.web.Folder;
import com.ettrema.web.IUser;
import com.ettrema.web.User;
import com.ettrema.web.security.CurrentUserService;
import java.io.InputStream;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class BinaryFileCreator implements Creator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( BinaryFileCreator.class );

    @Override
    public boolean accepts( String contentType ) {
        return true;
    }

    @Override
    public BaseResource createResource( Folder folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        log.debug( "create binary file with content type: " + ct );
        BinaryFile file = new BinaryFile( ct, folder, newName );
        IUser creator = _(CurrentUserService.class).getOnBehalfOf();

        if( creator instanceof User){
            file.setCreator( (User)creator );
        }
        file.save();
        if( in != null ) {
            file.setContent( in );
        }
        log.debug( "content type: " + file.getContentType( null ) );
        return file;
    }
}
