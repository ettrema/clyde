
package com.bradmcevoy.web;

import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.vfs.VfsCommon;

public abstract class CommonResourceFactory extends VfsCommon implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommonResourceFactory.class);

    private HostFinder hostFinder = new HostFinder();

    protected Host getHost(String hostName) {
        Host h = hostFinder.getHost( hostName );
        if( h == null) {
            throw new RuntimeException( "Unknown host: " + hostName);
        }
        return h;
    }
}
