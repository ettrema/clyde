package com.bradmcevoy.media;

import com.bradmcevoy.io.FileUtils;
import com.bradmcevoy.video.FFMPEGConverter;
import com.bradmcevoy.web.FlashFile;
import com.bradmcevoy.web.VideoFile;
import com.ettrema.vfs.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author brad
 */
public class SynchronousStreamingVideoGenerator implements StreamingVideoGenerator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( SynchronousStreamingVideoGenerator.class );

    public void generateStreamingVideo( final VideoFile source ) {
        log.debug( "generateStreaming: " + source.getConvertedHeight() + source.getConvertedWidth() );
        if( source.isTrash()) {
            log.debug( "not generating as in trash: " + source.getPath());
            return ;
        }
        InputStream in = null;
        FFMPEGConverter converter = null;
        try {
            FlashFile flash = new FlashFile( source.getThumbsFolder( true ), source.getName() + ".flv" );
            flash.save();
            in = source.getInputStream();
            final String inputType = FileUtils.getExtension( source.getName() );

            converter = new FFMPEGConverter( in, inputType );
            final FFMPEGConverter c = converter;
            flash.useOutputStream( new OutputStreamWriter<Long>() {

                @Override
                public Long writeTo( final OutputStream out ) {
                    log.debug( "using outputstream for conversion" );
                    return c.convert( out, "flv", source.getConvertedHeight(), source.getConvertedWidth() );
                }
            } );


        } finally {
            close( in );
            close( converter );
        }
        log.debug( "finished generateStreaming" );
    }

    private static void close(InputStream in) {
        if( in == null ) return ;
        try {
            in.close();
        } catch( IOException ex ) {
            log.error("exception closing inputstrea", ex);
        }
    }

    private static void close( FFMPEGConverter converter ) {
        if( converter == null ) return ;
        try{
            converter.close();
        } catch(Exception e) {
            log.error("exception closing converter", e);
        }
    }
}
