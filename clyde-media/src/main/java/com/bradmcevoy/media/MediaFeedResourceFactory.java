package com.bradmcevoy.media;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.Host;

/**
 *
 * @author brad
 */
public class MediaFeedResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MediaFeedResourceFactory.class );
    private final String feedName;
    private final ResourceFactory wrapped;
    private MediaFeedLinkGenerator linkGenerator;
    private final MediaLogService logService;
    private Long cacheSeconds;
    private boolean secure;

    public MediaFeedResourceFactory( String rssName, ResourceFactory wrapped, MediaLogService logService ) {
        this.feedName = rssName;
        this.wrapped = wrapped;
        this.logService = logService;
    }

    public Resource getResource( String host, String sPath ) {
        log.trace( "getResource: " + sPath );
        Path path = Path.path( sPath );
        if( path.getName().equals( feedName ) ) {
            log.trace( "got media feed name" );
            Resource parent = wrapped.getResource( host, path.getParent().toString() );
            if( parent instanceof Host ) {
                Host folder = (Host) parent;
                String basePath = buildBasePath(host, path.getParent());
                log.trace( "got media feed resource" );
                return new MediaFeedResource( logService, linkGenerator, feedName, folder, cacheSeconds, basePath );
            } else {
                log.trace( "did not find: " + path.getParent() );
                return null;
            }
        } else {
            log.trace("not media feed name");
            return null;
        }
    }

    public Long getCacheSeconds() {
        return cacheSeconds;
    }

    public void setCacheSeconds( Long cacheSeconds ) {
        this.cacheSeconds = cacheSeconds;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure( boolean secure ) {
        this.secure = secure;
    }

    private String buildBasePath( String host, Path parent ) {
        String prot = secure ? "https" : "http";
        String s = prot + "://" + host + parent.toString();
        log.debug( "base path: " + s);
        return s;
    }

    public MediaFeedLinkGenerator getLinkGenerator() {
        return linkGenerator;
    }

    public void setLinkGenerator( MediaFeedLinkGenerator linkGenerator ) {
        this.linkGenerator = linkGenerator;
    }

    

}
