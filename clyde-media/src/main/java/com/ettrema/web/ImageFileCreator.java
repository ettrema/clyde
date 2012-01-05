package com.ettrema.web;

import com.ettrema.web.creation.*;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import com.ettrema.web.security.CurrentUserService;
import java.io.InputStream;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class ImageFileCreator implements Creator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ImageFileCreator.class );

    @Override
    public boolean accepts( String ct ) {
        return ct.contains( "image" );
    }

    @Override
    public BaseResource createResource( Folder folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        log.debug( "createResource");
        ImageFile image = new ImageFile( ct, folder, newName );
        IUser creator = _( CurrentUserService.class ).getOnBehalfOf();
        if( creator instanceof User ) {
            log.debug( "setCreator: " + creator.getName() );
            image.setCreator( (User) creator );
        } else {
            log.debug( "no current user");
        }
        image.save();
        if( in != null ) {
            image.setContent( in );
        }
        return image;
    }
}
