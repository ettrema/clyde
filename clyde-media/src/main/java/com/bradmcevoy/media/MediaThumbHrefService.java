package com.bradmcevoy.media;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.FlashFile;
import com.bradmcevoy.web.HtmlImage;
import com.bradmcevoy.web.ImageFile;
import com.bradmcevoy.web.VideoFile;
import com.bradmcevoy.web.image.ThumbHrefService;

/**
 *
 * @author brad
 */
public class MediaThumbHrefService implements ThumbHrefService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MediaThumbHrefService.class );

    private String suffix = "_sys_thumbs";

    public String getThumbPath( Resource r ) {
        log.trace("getThumbPath");
        if( r instanceof ImageFile ) {   
            return findThumb( (ImageFile) r );
        } else if( r instanceof FlashFile ) {
            FlashFile ff = (FlashFile) r;
            return findThumb( ff );
        } else if( r instanceof VideoFile ) {
            VideoFile vf = (VideoFile) r;
            return findThumb( vf );
        } else {
            log.trace("not a known media file");
            return null;
        }
    }

    private String findThumb( ImageFile imageFile ) {
        return findThumbForBinary(imageFile);
    }

    private String findThumb( FlashFile ff ) {
        return findThumbForBinary(ff);
    }

    private String findThumb( VideoFile vf ) {
        FlashFile ff = vf.getStreamingVideo();
        if( ff == null ) {
            log.trace("no flash file has been generated yet");
            return null;
        } else {
            return findThumbForBinary( ff );
        }
    }

    private String findThumbForBinary( BinaryFile bf ) {
        HtmlImage thumb = bf.thumb( suffix );
        if( thumb instanceof BinaryFile ) {
            BinaryFile bfThumb = (BinaryFile) thumb;
            return bfThumb.getUrl();
        } else {
            log.trace("thumb has not been generated yet");
            return null;
        }
    }
}
