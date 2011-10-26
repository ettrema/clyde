package com.bradmcevoy.web.recent;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.Folder;

/**
 * Not to be used with decorator pattern. To be used with MultipleResourceFactory
 *
 */
public class RssResourceFactory implements ResourceFactory{

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( RssResourceFactory.class );

    private final String rssName;

    private final ResourceFactory wrapped;

    public RssResourceFactory(String rssName, ResourceFactory wrapped) {
        this.rssName = rssName;
        this.wrapped = wrapped;
    }


    public Resource getResource(String host, String sPath) {
        Path path = Path.path(sPath);
        if( path.getName().equals(rssName)) {
            Path parentPath = path.getParent();
            Resource parent = wrapped.getResource(host, parentPath.toString());
            if( parent instanceof Folder ) {
                Folder folder = (Folder) parent;
                return new RssResource(folder, rssName);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
