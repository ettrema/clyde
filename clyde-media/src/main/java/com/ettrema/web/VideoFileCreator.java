package com.ettrema.web;

import com.ettrema.web.creation.*;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.WritingException;
import java.io.InputStream;

/**
 *
 * @author brad
 */
public class VideoFileCreator implements Creator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( VideoFileCreator.class );

    @Override
    public boolean accepts( String ct ) {
        return ct.contains( "video" );
    }

    @Override
    public BaseResource createResource( Folder folder, String ct, InputStream in, String newName ) throws ReadingException, WritingException {
        if( ct.contains( "x-flash-video" ) || ct.equals( "video/x-flv") ) {
            log.trace( "createResource: is flash" );
            FlashFile video = new FlashFile( ct, folder, newName );
            video.save();
            if( in != null ) {
                video.setContent( in );
            }
            return video;
        } else {
            if( log.isTraceEnabled() ) {
                log.trace( "createResource: not flash: " + ct );
            }
            VideoFile video = new VideoFile( ct, folder, newName );
            video.save();
            if( in != null ) {
                video.setContent( in );
            }
            return video;
        }
    }
}
