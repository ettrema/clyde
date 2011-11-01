package com.ettrema.media;

import com.bradmcevoy.common.Path;
import com.ettrema.media.MediaLogService.MediaType;
import org.apache.commons.lang.StringUtils;

/**
 * Just returns the parent's parent folder of the media file (assuming that the thumb
 * is in a thumbs folder), optionally suffixed
 * with a page name (defaults to index.html);
 *
 * @author brad
 */
public class DefaultMediaLinkGenerator implements MediaFeedLinkGenerator {

    private String pageName = "index.html";

	@Override
    public String generateLink( MediaType mediaType, String mainContentPath ) {
        Path p = Path.path( mainContentPath );
        if( p.getParent() == null || p.getParent().getParent() == null ) {
            return "/";
        } else {
            Path parent = p.getParent().getParent();
            if( StringUtils.isEmpty( pageName ) ) {
                return parent.toString();
            } else {
                return parent.child( pageName ).toString();
            }
        }
    }

    /**
     * Defaults to index.html
     *
     * @return
     */
    public String getPageName() {
        return pageName;
    }

    public void setPageName( String pageName ) {
        this.pageName = pageName;
    }
}
