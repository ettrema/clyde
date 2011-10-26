package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * Looks for a special url (eg suffixed with _thumb.jpg) and returns the thumb of the first
 * image in the folder 
 *
 * @author brad
 */
public class ThumbResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ThumbResourceFactory.class );
    private final ResourceFactory next;
    private final ResourceFactory noImgResourceFactory;
    private String thumbName;
    private String thumbType;

    public ThumbResourceFactory( ResourceFactory next, ResourceFactory noImgResourceFactory ) {
        this.next = next;
        this.noImgResourceFactory = noImgResourceFactory;
        thumbName = "_thumb.jpg";
        thumbType = "thumb";
    }

    public Resource getResource( String host, String url ) {
        log.trace("getResource");
        Path path = Path.path( url );
        if( thumbName.equals( path.getName() ) ) {
            String nextPath = path.getParent().toString();
            // Get the parent resource
            Resource parent = next.getResource( host, nextPath );
            if( parent == null ) {
                return null;
            } else if( parent instanceof Folder ) {
                Folder parentCol = (Folder) parent;
                ImageFile firstImage = parentCol.findFirst( ImageFile.class );
                if( firstImage == null ) {
                    log.trace( "no image in folder" );
                    return getNoImageResource( host );
                } else {
                    HtmlImage thumb = firstImage.thumb( thumbType );
                    if( thumb instanceof GetableResource ) {
                        log.trace( "got a thumb" );
                        return (Resource) thumb;
                    } else {
                        String href = thumb.getHref();
                        log.warn( "thumb isnt a GetableResource, redirect to: " + href );
                        return new RedirectResource( href, parentCol.getRealm() );
                    }
                }
            } else {
                log.trace( "return no image resource" );
                return getNoImageResource( host );
            }
        } else {
            return next.getResource( host, url );
        }
    }

    private Resource getNoImageResource( String host ) {
        NoImageResource noimg = new NoImageResource();
        Resource r = noImgResourceFactory.getResource( host, noimg.getHref() );
        if( r == null ) {
            log.warn( "getNoImageResource: not found: " + noimg.getHref() );
        }
        return r;
    }

    public String getThumbName() {
        return thumbName;
    }

    public void setThumbName( String thumbName ) {
        this.thumbName = thumbName;
    }

    public String getThumbType() {
        return thumbType;
    }

    public void setThumbType( String thumbType ) {
        this.thumbType = thumbType;
    }
}
