package com.bradmcevoy.video;

import com.bradmcevoy.io.FileUtils;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.FlashFile;
import com.bradmcevoy.web.Folder;
import com.ettrema.vfs.OutputStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author brad
 */
public class FlashService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( FlashService.class );

    private int thumbHeight = 60;
    private int thumbWidth = 80;

    public FlashService() {
    }

    public void generateThumb(FlashFile f) {
        Folder thumbs = f.getThumbsFolder( true );
        String thumbName = f.getName() + ".jpg";
        BaseResource r = thumbs.childRes( thumbName );
        if( r != null ) r.delete();
        BinaryFile thumb = new BinaryFile( thumbs, thumbName );
        thumb.save();

        InputStream in = null;
        try {
            in = f.getInputStream();
            if( in == null ) {
                log.warn( "No inputstream for: " + f.getHref() );
            } else {
                final FFMPEGConverter c = new FFMPEGConverter( in, "flv" );
                try {
                    thumb.useOutputStream( new OutputStreamWriter<Long>() {

                        @Override
                        public Long writeTo( final OutputStream out ) {
                            log.debug( "using outputstream for conversion" );
                            return c.generateThumb( thumbHeight, thumbWidth, out, "jpeg" );
                        }
                    } );
                } finally {
                    c.close();
                }
            }
        } finally {
            FileUtils.close( in );
        }
    }

    public int getThumbHeight() {
        return thumbHeight;
    }

    public void setThumbHeight( int thumbHeight ) {
        this.thumbHeight = thumbHeight;
    }

    public int getThumbWidth() {
        return thumbWidth;
    }

    public void setThumbWidth( int thumbWidth ) {
        this.thumbWidth = thumbWidth;
    }
}
