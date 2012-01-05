package com.ettrema.thumbs;

import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.ImageFile;

/**
 * Populates a folder with a thumb href based on the contents of the folder
 *
 * deprecated!!!!!
 *
 * @author brad
 */
public class ThumbSelector {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ThumbSelector.class );
    private String primaryThumbSuffix = "thumbs";
    private String secondaryThumbSuffix;


    /**
     * Checks to see if the folder's thumbHref property is set correctly, and sets
     * it if not.
     *
     * @param folder - the folder to check and possibly modify
     * @return - true if the folder is modified
     */
    public boolean checkThumbHref( Folder folder ) {
        log.debug( "checkThumbHref: " + folder.getHref() );
        ImageFile thumb = findThumb(folder, primaryThumbSuffix);
        if( thumb == null && secondaryThumbSuffix != null ) {
            thumb = findThumb(folder, secondaryThumbSuffix);
        }
        if( thumb != null ) {
            String href = thumb.getHref();
            String oldHref = folder.getThumbHref();
            if( oldHref == null || !oldHref.equals( href ) ) {
                folder.setThumbHref( href );
                folder.save();
                return true;
            } else {
                log.debug( "thumb href has not changed" );
                return false;
            }
        }
        log.debug( "no image files in thumbs folder");
        return false;
    }

    private ImageFile findThumb(Folder folder, String thumbSuffix ) {
        Resource res = folder.child( thumbSuffix );
        if( res == null || !( res instanceof Folder ) ) {
            log.debug( "no thumbs folder: " + thumbSuffix );
            return null;
        }
        if( !( res instanceof Folder ) ) {
            log.debug( "thumbs exists, but is not a folder: " + res.getClass() );
            return null;
        }
        Folder thumbs = (Folder) res;
        for( Resource r : thumbs.getChildren() ) {
            if( r instanceof ImageFile ) {
                ImageFile thumb = (ImageFile) r;
                log.debug( "found thumb: " + thumb.getHref() );
                return thumb;
            }
        }
        return null;
    }

    public String getPrimaryThumbSuffix() {
        return primaryThumbSuffix;
    }

    public void setPrimaryThumbSuffix( String primaryThumbSuffix ) {
        this.primaryThumbSuffix = primaryThumbSuffix;
    }

    public String getSecondaryThumbSuffix() {
        return secondaryThumbSuffix;
    }

    public void setSecondaryThumbSuffix( String secondaryThumbSuffix ) {
        this.secondaryThumbSuffix = secondaryThumbSuffix;
    }


}
