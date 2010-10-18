package com.bradmcevoy.media;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.HtmlImage;
import com.bradmcevoy.web.image.ThumbHrefService;

/**
 * We want a full path from www.shmego.com, not from the user's account
 *
 * Eg http://www.shmego.com/sites/d2.devshmego.com/files/Documents/Drinks%40Clare%20Dec%202004/_sys_heros/PICT0082.JPG
 *
 * But don't include server and protocol:
 *
 * /sites/d2.devshmego.com/files/Documents/Drinks%40Clare%20Dec%202004/_sys_heros/PICT0082.JPG
 *
 * @author brad
 */
public class MediaThumbHrefService implements ThumbHrefService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MediaThumbHrefService.class );

    private boolean prefixWithHost = true;

    private String rootPrefix = "/sites/";

    public String getThumbPath( Resource r, String suffix ) {
        log.trace("getThumbPath");
        if( r instanceof BinaryFile ) {
            return findThumbHref( (BinaryFile) r, suffix );
        } else {
            log.trace("not a known media file");
            return null;
        }
    }

    private String findThumbHref( BinaryFile bf, String suffix  ) {
        HtmlImage thumb = bf.thumb( suffix );
        if( thumb instanceof BinaryFile ) {
            BinaryFile bfThumb = (BinaryFile) thumb;
            String s = bfThumb.getUrl();
            if( prefixWithHost ) {
                Host h = bf.getHost();
                s = h.getName() + s;
            }
            if( rootPrefix != null ) {
                s = rootPrefix + s;
            }
            return s;
        } else {
            log.trace("thumb has not been generated yet");
            return null;
        }
    }
}
