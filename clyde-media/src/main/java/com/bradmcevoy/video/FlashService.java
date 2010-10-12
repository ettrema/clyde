package com.bradmcevoy.video;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.FileUtils;
import com.bradmcevoy.media.MediaLogService;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.FlashFile;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Thumb;
import com.bradmcevoy.web.VideoFile;
import com.ettrema.vfs.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author brad
 */
public class FlashService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FlashService.class );
    private MediaLogService mediaLogService;
    /**
     * This is the thumb folder that flash videos will be generated in
     */
    private String flashThumbSuffix = "_sys_flash";
    private String ffmpegProcess = "ffmpeg";

    /**
     * return the number of artefacts genereated
     *
     * @param source
     * @return
     */
    public int generateStreamingVideo( final VideoFile source ) {
        log.debug( "generateStreaming: " + source.getConvertedHeight() + source.getConvertedWidth() );
        if( source.isTrash() ) {
            log.debug( "not generating as in trash: " + source.getPath() );
            return 0;
        }
        InputStream in = null;
        FFMPEGConverter converter = null;

        try {
            Folder thumbs = source.getParent().thumbs( flashThumbSuffix, true );
            FlashFile flash = new FlashFile( thumbs, getFlashFileNameForVideo( source ) );
            flash.save();
            in = source.getInputStream();
            final String inputType = FileUtils.getExtension( source.getName() );

            converter = new FFMPEGConverter( ffmpegProcess, in, inputType );

            if( converter.getSourceLength() > 0 ) {
                final FFMPEGConverter c = converter;
                flash.useOutputStream( new OutputStreamWriter<Long>() {

                    @Override
                    public Long writeTo( final OutputStream out ) {
                        log.debug( "using outputstream for conversion" );
                        return c.convert( out, "flv", source.getConvertedHeight(), source.getConvertedWidth() );
                    }
                } );
            } else {
                log.warn( "Cant generate streaming video because source file length is zero" );
            }
            log.debug( "finished generateStreaming" );

            int numThumbs = generateThumbs( flash, source.getParent(), converter );

            return 1 + numThumbs;
        } finally {
            close( in );
            close( converter );

        }
    }

    public String getFlashFileNameForVideo( VideoFile source ) {
        return source.getName() + ".flv";
    }

    private static void close( InputStream in ) {
        if( in == null ) return;
        try {
            in.close();
        } catch( IOException ex ) {
            log.error( "exception closing inputstrea", ex );
        }
    }

    private static void close( FFMPEGConverter converter ) {
        if( converter == null ) return;
        try {
            converter.close();
        } catch( Exception e ) {
            log.error( "exception closing converter", e );
        }
    }

    public int generateThumbs( FlashFile f ) {
        return generateThumbs( f, f.getParent() );
    }

    private int generateThumbs( FlashFile f, Folder thumbsParent ) {
        FFMPEGConverter c = null;
        InputStream in = null;
        try {
            in = f.getInputStream();
            if( in == null ) {
                log.warn( "No inputstream for: " + f.getHref() );
                return 0;
            }
            c = new FFMPEGConverter( ffmpegProcess, in, "flv" );
            return generateThumbs( f, thumbsParent, c );
        } finally {
            close( c );
            FileUtils.close( in );
        }
    }

    private void delete( BaseResource r ) {
        try {
            r.delete();
        } catch( NotAuthorizedException ex ) {
            throw new RuntimeException( ex );
        } catch( ConflictException ex ) {
            throw new RuntimeException( ex );
        } catch( BadRequestException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public MediaLogService getMediaLogService() {
        return mediaLogService;
    }

    public void setMediaLogService( MediaLogService mediaLogService ) {
        this.mediaLogService = mediaLogService;
    }

    public String getFlashThumbSuffix() {
        return flashThumbSuffix;
    }

    public void setFlashThumbSuffix( String flashThumbSuffix ) {
        this.flashThumbSuffix = flashThumbSuffix;
    }

    public String getFfmpegProcess() {
        return ffmpegProcess;
    }

    public void setFfmpegProcess( String ffmpegProcess ) {
        this.ffmpegProcess = ffmpegProcess;
    }

    public String getThumbName( FlashFile f ) {
        return f.getName() + ".jpg";
    }

    public String getThumbName( VideoFile f ) {
        return f.getName() + ".jpg";
    }

    private int generateThumbs( FlashFile f, Folder thumbsParent, final FFMPEGConverter c ) {
        String thumbName = getThumbName(f );
        try {
            List<Thumb> thumbSpecs = Thumb.getThumbSpecs( f.getParent() );
            if( thumbSpecs == null ) {
                log.warn( "no thumb specs from: " + f.getParent().getHref() );
                thumbSpecs = Arrays.asList( new Thumb( "_sys_thumb", 200, 200 ) );
            }
            int count = 0;
            for( Thumb ts : thumbSpecs ) {
                count++;
                final Thumb thumbSpec = ts;
                Folder thumbs = thumbsParent.thumbs( thumbSpec.getSuffix(), true );
                BaseResource r = thumbs.childRes( thumbName );
                if( r != null ) {
                    delete( r );
                }

                BinaryFile thumb = new BinaryFile( thumbs, thumbName );
                thumb.save();


                thumb.useOutputStream( new OutputStreamWriter<Long>() {

                    @Override
                    public Long writeTo( final OutputStream out ) {
                        log.debug( "using outputstream for conversion" );
                        return c.generateThumb( thumbSpec.getHeight(), thumbSpec.getWidth(), out, "jpeg" );
                    }
                } );
            }
            return count;
        } finally {
            c.close();
        }

    }
}
