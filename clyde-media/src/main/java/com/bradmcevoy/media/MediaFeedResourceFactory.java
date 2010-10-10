package com.bradmcevoy.media;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.Folder;

/**
 *
 * @author brad
 */
public class MediaFeedResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MediaFeedResourceFactory.class );
    private final String feedName;
    private final ResourceFactory wrapped;
    private final MediaLogService logService;
    private Long cacheSeconds;

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
            if( parent instanceof Folder ) {
                Folder folder = (Folder) parent;
                log.trace( "got media feed resource" );
                return new MediaFeedResource( logService, feedName, folder.getHost(), cacheSeconds );
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
}
