
package com.bradmcevoy.web;

import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.vfs.VfsCommon;

public abstract class CommonResourceFactory extends VfsCommon implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommonResourceFactory.class);

    private HostFinder hostFinder = new HostFinder();

    protected Host getHost(String hostName) {
        Host h = hostFinder.getHost( hostName );
        if( h == null) {
            if( !hostName.startsWith( "www.")) {
                String h2 = "www." + hostName;
                h = hostFinder.getHost( h2 );
            } else {
                String h2 = hostName.substring( 4 );
                h = hostFinder.getHost( h2 );
            }
        }
        return h;
    }
}
