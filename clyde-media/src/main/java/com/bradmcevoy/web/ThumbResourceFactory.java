package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 *
 * @author brad
 */
public class ThumbResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ThumbResourceFactory.class );
    private final ResourceFactory next;
    private final String thumbName;
    private final String thumbType;

    public ThumbResourceFactory( ResourceFactory next ) {
        this.next = next;
        thumbName = "_thumb.jpg";
        thumbType = "thumb";
    }

    public Resource getResource( String host, String url ) {
        Path path = Path.path( url );
        if( thumbName.equals( path.getName() ) ) {
            String nextPath = path.getParent().toString();
            Resource r = next.getResource( host, nextPath );
            if( r == null ) {
                return null;
            } else if( r instanceof Folder ) {
                Folder col = (Folder) r;
                ImageFile firstImage = col.findFirst( ImageFile.class );
                if( firstImage == null ) {
                    log.warn( "no image in folder" );
                    return null;
                } else {
                    HtmlImage thumb = firstImage.thumb( thumbType );
                    if( thumb instanceof GetableResource ) {
                        return (Resource) thumb;
                    } else {
                        String href = thumb.getHref();
                        log.warn("thumb isnt a GetableResource, redirect to: " + href);
                        return new RedirectResource( href, col.getRealm() );
                    }
                }
            } else {
                return null;
            }
        } else {
            return next.getResource( host, url );
        }
    }
}
