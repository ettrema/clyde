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
public class MusicFileCreator implements Creator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MusicFileCreator.class );

    @Override
    public boolean accepts( String ct ) {
        return ct.contains( "audio" );
    }

    @Override
    public BaseResource createResource( Folder folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        log.debug( "createResource");
        MusicFile song = new MusicFile( ct, folder, newName );
        IUser creator = _( CurrentUserService.class ).getOnBehalfOf();
        if( creator instanceof User ) {
            log.debug( "setCreator: " + creator.getName() );
            song.setCreator( (User) creator );
        } else {
            log.debug( "no current user");
        }
        song.save();
        if( in != null ) {
            song.setContent( in );
        }
        return song;
    }
}
