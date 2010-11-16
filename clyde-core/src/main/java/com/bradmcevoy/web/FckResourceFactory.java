package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

public class FckResourceFactory extends AbstractClydeResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FckResourceFactory.class);

    private final ResourceFactory wrappedFactory;

    public FckResourceFactory(ResourceFactory wrappedFactory) {
        this.wrappedFactory = wrappedFactory;
    }

    @Override
    public Resource getResource(String host, String url) {
        Path path = Path.path(url);
        if (FckFileManagerResource.URL.equals(path)) {
            CollectionResource h = getParent(host, path.getParent());
            if( h == null ) return null;
            FckFileManagerResource fck = new FckFileManagerResource(h);
            return fck;
        } else if (FckQuickUploaderResource.URL.equals(path)) {
            CollectionResource h = getParent(host, path.getParent());
            if( h == null ) return null;
            FckQuickUploaderResource fck = new FckQuickUploaderResource(h);
            return fck;
        } else {
            return null;
        }
    }
    

    private CollectionResource getParent( String host, Path path ) {
        Resource r = wrappedFactory.getResource( host, path.toString() );
        if( r instanceof CollectionResource ) {
            return (CollectionResource) r;
        } else {
            log.warn( "Could not locate a CollectionResource at: http://" + host + "/" + path);
            return null;
        }
    }
        
}
