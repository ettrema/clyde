package com.bradmcevoy.media;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.HtmlImage;
import com.bradmcevoy.web.image.ThumbHrefService;

/**
 *
 * @author brad
 */
public class MediaThumbHrefService implements ThumbHrefService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MediaThumbHrefService.class );

    private String suffix = "_sys_thumb";

    public String getThumbPath( Resource r ) {
        log.trace("getThumbPath");
        if( r instanceof BinaryFile ) {
            return findThumb( (BinaryFile) r );
        } else {
            log.trace("not a known media file");
            return null;
        }
    }

    private String findThumb( BinaryFile bf ) {
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
